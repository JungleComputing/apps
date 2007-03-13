package org.sat4j.minisat.core;

import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

// import org.sat4j.minisat.core.ISimplifier;
import org.sat4j.minisat.core.ActivityComparator;
import org.sat4j.minisat.learning.MiniSATLearning;
import org.sat4j.minisat.learning.LimitedLearning;
import java.util.Properties;

import ibis.satin.SatinObject;
import ibis.satin.Inlet;
import java.util.Enumeration;
// for serialClone:
import java.io.*;
import java.util.Hashtable;
import org.sat4j.minisat.constraints.cnf.Lits2; // Hack
import org.sat4j.minisat.constraints.cnf.WLClause; // Hack

/**
 * @author Kees Verstoep (versto@cs.vu.nl)
 */
public class SolverSpawner
    extends SatinObject
    implements Serializable, Cloneable,
	solverSpawnerInterface
{
    public SolverSpawner()
    {
    }

    public boolean
    guard_spawn_recSearch(Solver solver, long nofConflicts, int satinDepth,
			  SolverState globalState, Vec<VecInt> reasons)
    {
        if (globalState.iter > solver.satinSearchIter) {
            System.err.println("OOPS: globalState.iter = " + globalState.iter
                + ", solver.satinSearchIter = " + solver.satinSearchIter);
            (new Throwable()).printStackTrace(System.err);
            throw new RuntimeException("OOPS");
        }

	return (globalState.iter == solver.satinSearchIter);
    }

    public SolverResult
    spawn_recSearch(Solver solver, long nofConflicts, int satinDepth,
		    SolverState globalState, Vec<VecInt> reasons)
    {
	SolverResult res;

	res = recSearch(solver,	nofConflicts, satinDepth,
			globalState, reasons);
	return res;
    }

    private final long initialFreeMem = Runtime.getRuntime().freeMemory();
    private final long bound1 = (initialFreeMem / 3);
    private final long bound2 = 64 * 1024 * 1024;
    private final long memorybound = (bound1 < bound2) ? bound2 : bound1;

    public SolverResult
    recSearch(Solver solver, long nofConflicts, int satinDepth,
	      SolverState globalState, Vec<VecInt> reasons)
    {
	SolverResult res;
	boolean addLevel = false;
	long begintime = System.currentTimeMillis();

	res = solver.satinDoRecSearch(nofConflicts, satinDepth,
				      globalState, reasons);
	if (!res.seqTimeOut) {
	    // Done
	    if (res.satin_res != Lbool.UNDEFINED) {
		// i.e., we actually completed our local search tree
	        globalState.addPercDone(Math.pow(2.0, (double) - satinDepth));
	    }
	    return res;
	}

	// Have to split up the problem state
	// Solver s1, s2;
	SolverResult res1, res2;
	int child_depth = satinDepth + 1;
	long child_nofConflicts = nofConflicts;

	// obtain new reasons:
	reasons = solver.reasonVec();

	// New begintime to account for combined subjobs
	begintime = System.currentTimeMillis();

	if (!solver.satinUseSharedObjects) {
	    // Divide the allowed number of conflicts evenly over children,
	    // plus some correction factor since the tree is probably
	    // unbalanced. Very hard to do this right (undershoot/overshoot)
	    // compared to the case where we have global knowledge.
	    child_nofConflicts =
		(nofConflicts - solver.lastConflicts) / 16 * 15;
	}

	// Pick up things we want to keep from the parent solver:
	SolverStats stats = solver.stats;
	String name = solver.name;

	assert solver.decisionLevel() > solver.rootLevel;

        // Split up the problem, see the GridSAT paper.
        // Have to modify the assignment stacks

	{
	    Solver s2 = solver.clone_for_satin();
	    if (addLevel) {
		s2.name = name + "-" + child_depth;
	    }
	    s2.name += "b";
	    s2.splitChild();
	    s2.stats.reset(); // start fresh stats for child
	    res2 = spawn_recSearch(s2, child_nofConflicts, child_depth,
				   globalState, reasons);
	}

	{
	    /* same for parent.  This one is scheduled first by Satin, thus
	     * honouring the same search pattern as for the non-split case
	     */
	    Solver s1;

	    if ((solver.satinFixes & 0x8) != 0) {
 		// TODO: does not seem to work yet!!
	        s1 = solver;
	    } else {
	    	s1 = solver.clone_for_satin();
	        s1.stats.reset(); // TODO: needed?
	    }
	    if (addLevel) {
		s1.name = name  + "-" + child_depth;
	    }
	    s1.name += "a";
	    s1.splitParent();
	    res1 = spawn_recSearch(s1, child_nofConflicts, child_depth,
				   globalState, reasons);
        }

	long freeMem = Runtime.getRuntime().freeMemory();
	if (freeMem < memorybound) {
	    Runtime.getRuntime().gc();
            System.out.println("c " + solver.getStatus(globalState) +
			       " " + name + " GC");
	}

	sync();
	
	solver.name = name; // restore, got updated for parent search above

	double secs = (System.currentTimeMillis() - begintime) / 1000.0;
	if (satinDepth < 6 && secs > 10.0) {
	    System.out.println("c " + solver.getStatus(globalState) +
 				" " + name + " tottime " + secs);
	}

	if ((solver.satinFixes & 0x8) == 0) {
	    stats.addStats(res1.stats);
	}
	stats.addStats(res2.stats);

	double [] activities = res2.activities;
	if ((solver.satinFixes & 0x8) == 0 &&
	    res1.satin_res != Lbool.TRUE && res2.satin_res != Lbool.TRUE) {
	    // Incorporate variable statistics from parent sub solver.
	    // It might help future iterations in picking better variables
	    // TODO: only works if they haven't been rescaled in the mean time;
	    // otherwise we will have to scale back to a common measure.
	    // But currently rescaling hardly happens, so skip for now.
	    // 
	    // Simply add the activities; they will again be added by parent
	    for (int i = 0; i < res1.activities.length; i++) {
		activities[i] += res1.activities[i];
	    }
	}


	if (res1.satin_res == Lbool.TRUE ||
	    res2.satin_res == Lbool.TRUE)
	{
	    // model found
	    int [] model;

	    System.out.println(name + ": SATISFIABLE");
	    if (res1.satin_res == Lbool.TRUE) {
		model = res1.model.clone();
	    } else {
		model = res2.model.clone();
	    }
	    return new SolverResult(Lbool.TRUE, model, stats, activities);
	} else if (res1.satin_res == Lbool.UNDEFINED ||
		   res2.satin_res == Lbool.UNDEFINED)
	{
	    // true timeout
	    return new SolverResult(Lbool.UNDEFINED, null, stats, activities);
	} else {
	    // Unsat
	    return new SolverResult(Lbool.FALSE, null, stats, activities);
	}
    }
}

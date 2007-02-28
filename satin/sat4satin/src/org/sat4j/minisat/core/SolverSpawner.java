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

	if (solver.decisionLevel() == solver.rootLevel) {
	    Solver s1, s2;

	    // Easy split
	    s1 = solver.clone_for_satin();
	    if (addLevel) {
		s1.name = name  + "-" + child_depth;
	    }
	    s1.name += "d";
	    res1 = spawn_recSearch(s1, child_nofConflicts, child_depth,
				   globalState, reasons);
	    s1 = null;		// for GC

	    if (solver.rootLevel != 0 && (solver.satinFixes & 0x8) != 0) {
		// No need to clone both!
		s2 = solver;
		// Just create new stats so that the can be added back below
		s2.stats = new SolverStats();
		s2.fixupAfterClone();
	    } else {
		s2 = solver.clone_for_satin();
	    }

	    if (addLevel) {
		s2.name = name + "-" + child_depth;
	    }
	    s2.name += "c";
	    res2 = spawn_recSearch(s2, child_nofConflicts, child_depth,
				   globalState, reasons);
	    s2 = null;		// for GC
	} else {
	    boolean spawnParentFirst = false; // SATIN runs last job first!

	    // More difficult split, see GridSAT paper.
	    // Have to modify the assignment stacks:

	    if (spawnParentFirst) {
		Solver s1 = solver.clone_for_satin();
		if (addLevel) {
		    s1.name = name  + "-" + child_depth;
		}
		s1.name += "a";
		int splitvar = s1.splitParent();
		res1 = spawn_recSearch(s1, child_nofConflicts, child_depth,
				       globalState, reasons);
	    } else {
		res1 = null;
	    }

	    {
		Solver s2;

		if (solver.rootLevel != 0 && spawnParentFirst &&
		    (solver.satinFixes & 0x8) != 0)
		{
		    // No need to clone both!
		    s2 = solver;
		    // Just create new stats so that they can be added below
		    s2.stats = new SolverStats();
		    s2.fixupAfterClone();
		} else {
		    s2 = solver.clone_for_satin();
		}
		if (addLevel) {
		    s2.name = name + "-" + child_depth;
		}
		s2.name += "b";
		s2.splitChild();
		res2 = spawn_recSearch(s2, child_nofConflicts, child_depth,
				       globalState, reasons);
	    }

	    if (!spawnParentFirst) {
		/* same code as above */
		Solver s1 = solver.clone_for_satin();
		if (addLevel) {
		    s1.name = name  + "-" + child_depth;
		}
		s1.name += "a";
		int splitvar = s1.splitParent();
		res1 = spawn_recSearch(s1, child_nofConflicts, child_depth,
				       globalState, reasons);
	    }
	}

	long freeMem = Runtime.getRuntime().freeMemory();
	if (freeMem < memorybound) {
	    Runtime.getRuntime().gc(); // TODO, looking at memory leaks
	    System.out.println("c " + name + " gc: " +
		 + (int) (100.0 * (double) Runtime.getRuntime().freeMemory() /
			  (double) initialFreeMem)
		 + "% free mem, was "
		 + (int) (100.0 * (double) freeMem / (double) initialFreeMem)
		 + "%");
	}

	solver = null;		// for GC

	sync();
	
	double secs = (System.currentTimeMillis() - begintime) / 1000.0;
	if (satinDepth < 6 && secs > 10.0) {
	    System.out.println("c " + name + " children took " + 
			       secs + " sec");
	}

	stats.addStats(res1.stats);
	stats.addStats(res2.stats);

	double [] activities = res1.activities;
	if (res1.satin_res != Lbool.TRUE && res2.satin_res != Lbool.TRUE) {
	    // Incorporate variable statistics from childs;
	    // it might help future iterations in picking better variables
	    // TODO: only works if they haven't been rescaled in the mean time;
	    // otherwise we will have to scale back to a common measure.
	    // But currently rescaling hardly happens, so skip for now.
	    // 
	    // Simply add the activities; they will again be added by parent
	    for (int i = 0; i < res2.activities.length; i++) {
		activities[i] += res2.activities[i];
		// activities[i] /= 2.0;  // No, they are added by parent
	    }
	    // System.out.println(name + ": added child activities " +
	    //		       " maxSpawnDepth " + stats.maxSpawnDepth);
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
	    if (false) {
		System.out.println(name + ": UNDEFINED");
	    }
	    return new SolverResult(Lbool.UNDEFINED, null, stats, activities);
	} else {
	    // Unsat
	    if (false) {
		System.out.println(name + ": UNSATISFIABLE");
	    }
	    return new SolverResult(Lbool.FALSE, null, stats, activities);
	}
    }
}

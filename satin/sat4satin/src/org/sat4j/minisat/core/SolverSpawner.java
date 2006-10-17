package org.sat4j.minisat.core;

import ibis.satin.SatinObject;

import java.io.Serializable;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;

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

    public SolverResult
    recSearch(Solver solver, long nofConflicts, int satinDepth,
	      SolverState globalState, Vec<VecInt> reasons)
    {
	SolverResult res;
	long begintime = System.currentTimeMillis();

	// System.out.println("recSearch: solver iter " +
	//                    solver.satinSearchIter +
        // 		      " globalState iter " + globalState.iter);

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

	if (solver.satinUseSharedObjects) {
	    // New begintime to account for combined subjobs
	    begintime = System.currentTimeMillis();
	} else {
	    // Divide the allowed number of conflicts evenly
	    // over children, plus some correction factor
	    // since the tree is probably unbalanced??
	    // Very hard to do this right (undershoot/overshoot)
	    // compared to the case where we have global knowledge.
	    child_nofConflicts =
		(nofConflicts - solver.lastConflicts) / 16 * 15;
	}

	// Pick up things we want to keep from the parent solver:
	SolverStats stats = solver.stats;
	String name = solver.name;

	// Solver s1, s2;

	if (solver.decisionLevel() == solver.rootLevel) {
	    Solver s1, s2;

	    // Easy split
	    s1 = solver.clone_for_satin();
	    s1.name = s1.name  + "-" + child_depth + "a";
	    res1 = spawn_recSearch(s1,
				   child_nofConflicts,
				   child_depth,
				   globalState,
				   reasons);
	    // allow solver to be gc-ed:
	    s1 = null;

	    if (solver.rootLevel != 0 && (solver.satinFixes & 0x8) != 0) {
		// No need to clone both!
		s2 = solver;
		// Just create new stats so that the can be added back below
		s2.stats = new SolverStats();
		s2.fixupAfterClone();
	    } else {
		s2 = solver.clone_for_satin();
	    }

	    s2.name = s2.name + "-" + child_depth + "b";
	    res2 = spawn_recSearch(s2,
				   child_nofConflicts,
				   child_depth,
				   globalState,
				   reasons);
	    // allow solver to be gc-ed:
	    s2 = null;
	} else {
	    Solver s1, s2;

	    // More difficult split, see GridSAT paper.
	    // Have to modify the assignment stacks:
	    s1 = solver.clone_for_satin();
	    s1.name = s1.name  + "-" + child_depth + "c";
	    int splitvar = s1.splitParent();
	    res1 = spawn_recSearch(s1,
				   child_nofConflicts,
				   child_depth,
				   globalState,
				   reasons);
	    // allow solver to be gc-ed:
	    s1 = null;

	    if (solver.rootLevel != 0 && (solver.satinFixes & 0x8) != 0) {
		// No need to clone both!
		s2 = solver;
		// Just create new stats so that the can be added back below
		s2.stats = new SolverStats();
		s2.fixupAfterClone();
	    } else {
		s2 = solver.clone_for_satin();
	    }
	    s2.name = s2.name + "-" + child_depth + "d";
	    s2.splitChild(splitvar);
	    res2 = spawn_recSearch(s2,
				   child_nofConflicts,
				   child_depth,
				   globalState,
				   reasons);
	    // allow solver to be gc-ed:
	    s2 = null;
	}

	// clear away references to this solver so it can be gc-ed:
	solver = null;

	sync();

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
	    // Simply take the average of the activities; should be
	    // fine since we are only interested in relative differences?
	    for (int i = 0; i < res2.activities.length; i++) {
		activities[i] += res2.activities[i];
		activities[i] /= 2.0;
	    }
	}

	// Since we may have reused a parent solver: restore name
	//s2.name = name;
	//s2.stats = stats;

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
	    // if (s2.satinUseSharedObjects) {
	    //	s2.updateTimingStats(globalState, begintime, 0);
	    // }
	    return new SolverResult(Lbool.FALSE, null, stats, activities);
	}
    }
}

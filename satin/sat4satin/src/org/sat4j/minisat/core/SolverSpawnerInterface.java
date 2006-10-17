package org.sat4j.minisat.core;

import ibis.satin.Spawnable;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;

interface SolverSpawnerInterface extends Spawnable {
    SolverResult spawn_recSearch(Solver solver, long nofConflicts,
        int satinDepth, SolverState globalState, Vec<VecInt> reasons);
}

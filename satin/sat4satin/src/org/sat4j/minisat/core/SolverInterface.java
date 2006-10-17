package org.sat4j.minisat.core;
import ibis.satin.Spawnable;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;

interface solverSatinInterface extends Spawnable {
    SolverResult spawn_satinRecSearch(Solver solver, long nofConflicts,
				      int satinDepth,
				      SolverState globalState,
				      Vec<VecInt> reasons);
}

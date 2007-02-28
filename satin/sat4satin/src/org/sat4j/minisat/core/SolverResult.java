package org.sat4j.minisat.core;
import java.io.Serializable;

public final class SolverResult implements Serializable
{
    Lbool satin_res;
    int[] model = null;
    SolverStats stats;
    boolean seqTimeOut = false;
    int lastSelectedLit;
    int lastConflicts;
    double[] activities;

    public SolverResult(Lbool result, int[] model,
			SolverStats stats,
			double[] activities)
    {
	this.satin_res = result;
	this.model = model;
	this.stats = stats;
	this.activities = activities;
    }

    public SolverResult(Lbool result, int[] model,
			SolverStats stats,
			ILits lits, IOrder order)
    {
	this(result, model, stats, null);

	int nvars = lits.nVars();
	double[] activities = new double[nvars + 1];
	for (int i = 1; i <= nvars; i++) {
	    int lit = i << 1;
	    activities[i] = order.varActivity(lit);
	}
	this.activities = activities;
    }
}

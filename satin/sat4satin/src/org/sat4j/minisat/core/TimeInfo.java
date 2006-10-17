package org.sat4j.minisat.core;

import java.io.Serializable;

public class TimeInfo implements Serializable {
    public String solver;

    public long endtime;

    public long seqtime;

    public int iters;

    public TimeInfo(String solver, long endtime, long seqtime, int iters) {
        this.solver = solver;
        this.endtime = endtime;
        this.seqtime = seqtime;
        this.iters = iters;
    }
}

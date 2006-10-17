/*
 * SAT4J: a SATisfiability library for Java   
 * Copyright (C) 2004 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas Een and Niklas Serensson.
 * Proceedings of the Sixth International Conference on Theory 
 * and Applications of Satisfiability Testing, LNCS 2919, 
 * pp 502-518, 2003.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 */

package org.sat4j.minisat.core;

import java.io.PrintStream;
import java.io.Serializable;

/*
 * Created on 15 janv. 2004 To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

public class SolverStats implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    public int starts;

    public long decisions;

    public long propagations;

    public long inspects;

    public long conflicts;

    public long learnedliterals;

    public long learnedbinaryclauses;

    public long learnedternaryclauses;

    public long learnedclauses;

    public long rootSimplifications;

    public long reducedliterals;

    public long changedreason;

    public int reduceddb;
    
    // Satin/Global learning stats:
    public long publishglobalclauses;

    public long publishglobaljobs;

    public long learnedglobalclauses;

    public long learnedglobaljobs;

    public long removedglobaljobs;

    public long conflFired;

    public long learntLocalFired;

    public long learntGlobalFired;

    public int  maxSpawnDepth;

    public int  maxIter;

    public double cloneOverhead;

    public void reset() {
        starts = 0;
        decisions = 0;
        propagations = 0;
        inspects = 0;
        conflicts = 0;
        learnedliterals = 0;
        learnedclauses = 0;
        learnedbinaryclauses = 0;
        learnedternaryclauses = 0;
        rootSimplifications = 0;
        reducedliterals=0;
        changedreason=0;
        reduceddb=0;

	conflFired = 0;
	learntLocalFired = 0;
	learntGlobalFired = 0;
        
	publishglobalclauses = 0;
	publishglobaljobs = 0;
	learnedglobalclauses = 0;
	learnedglobaljobs = 0;
	removedglobaljobs = 0;
	maxSpawnDepth = 0;
	maxIter = 0;
	cloneOverhead = 0.0;
    }
    
    @Override
    public Object clone() {
        SolverStats clone;

	try {
	    clone = (SolverStats) super.clone();
	}
	catch (CloneNotSupportedException e) {
	    throw new InternalError(e.toString());
	}

	return clone;
    }

    public void addStats(SolverStats add)
    {
        starts                += add.starts;
        decisions             += add.decisions;
        propagations          += add.propagations;
        inspects              += add.inspects;
        conflicts             += add.conflicts;
        learnedliterals       += add.learnedliterals;
        learnedclauses        += add.learnedclauses;
        learnedbinaryclauses  += add.learnedbinaryclauses;
        learnedternaryclauses += add.learnedternaryclauses;
        rootSimplifications   += add.rootSimplifications;
        reducedliterals       += add.reducedliterals;
        changedreason         += add.changedreason;
        reduceddb             += add.reduceddb;

	conflFired            += add.conflFired;
	learntLocalFired      += add.learntLocalFired;
	learntGlobalFired     += add.learntGlobalFired;

	publishglobalclauses  += add.publishglobalclauses;
	publishglobaljobs     += add.publishglobaljobs;
	learnedglobalclauses  += add.learnedglobalclauses;
	learnedglobaljobs     += add.learnedglobaljobs;
	removedglobaljobs     += add.removedglobaljobs;

	if (add.maxSpawnDepth > maxSpawnDepth) {
	    maxSpawnDepth = add.maxSpawnDepth;
	}
	if (add.maxIter > maxIter) {
	    maxIter = add.maxIter;
	}
	cloneOverhead         += add.cloneOverhead;
    }

    public void printStat(PrintStream out, String prefix) {
        out.println(prefix + "starts\t\t: " + starts);
        out.println(prefix + "conflicts\t\t: " + conflicts);
        out.println(prefix + "confl fired\t\t: " + conflFired);
        out.println(prefix + "learnt local fired\t: " + learntLocalFired);
        out.println(prefix + "learnt global fired\t: " + learntGlobalFired);
        out.println(prefix + "decisions\t\t: " + decisions);
        out.println(prefix + "propagations\t\t: " + propagations);
        out.println(prefix + "inspects\t\t: " + inspects);
        out.println(prefix + "learnt literals\t: " + learnedliterals);
        out.println(prefix + "learnt binary clauses\t: "
                + learnedbinaryclauses);
        out.println(prefix + "learnt ternary clauses\t: "
                + learnedternaryclauses);
        out.println(prefix + "learnt clauses\t: " + learnedclauses);
        out.println(prefix + "root simplifications\t: "
                + rootSimplifications);
        out.println(prefix + "removed literals (reason simplification)\t: "
                + reducedliterals);
        out.println(prefix + "reason swapping (by a shorter reason)\t: "
                + changedreason);
        out.println(prefix + "Calls to reduceDB\t: "
                + reduceddb);
        out.println(prefix + "publish global clauses\t: "
                + publishglobalclauses);
        out.println(prefix + "publish global jobs\t: "
                + publishglobaljobs);
        out.println(prefix + "learnt global clauses\t: "
		+ learnedglobalclauses);
        out.println(prefix + "learnt global jobs\t: "
                + learnedglobaljobs);
        out.println(prefix + "removed global jobs\t: "
                + removedglobaljobs);
        out.println(prefix + "max spawn depth\t: "
                + maxSpawnDepth);
        out.println(prefix + "max iter\t\t: "
                + maxIter);
        out.println(prefix + "clone overhead\t: "
                + cloneOverhead);
    }
}

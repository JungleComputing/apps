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

package org.sat4j.minisat.learning;

import java.io.Serializable;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.core.VarActivityListener;

/**
 * Learn only clauses which size is smaller than a percentage of the number of
 * variables.
 * 
 * @author leberre
 */
public class LimitedLearning implements LearningStrategy, Serializable,
        Cloneable {

    private static final long serialVersionUID = 1L;

    // private final NoLearningButHeuristics none;
    private NoLearningButHeuristics none;

    // private final MiniSATLearning all;
    private MiniSATLearning all;

    private final int maxpercent;

    private ILits lits;

    private int bound;

    public LimitedLearning() {
        this(10);
    }

    public LimitedLearning(int percent) {

        maxpercent = percent;

        // For satin/cloning:
        none = new NoLearningButHeuristics();
        all = new MiniSATLearning();
    }

    {
        // none = new NoLearningButHeuristics();
        // all = new MiniSATLearning();

    }

    public void setSolver(Solver s) {
        this.lits = s.getVocabulary();
        setVarActivityListener(s);
        all.setDataStructureFactory(s.getDSFactory());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.LearningScheme#learns(org.sat4j.minisat.Solver,
     *      org.sat4j.minisat.datatype.Vec)
     */
    public void learns(Constr constr) {
        if (learningCondition(constr)) {
            all.learns(constr);
        } else {
            none.learns(constr);
        }
    }

    protected boolean learningCondition(Constr constr) {
        return constr.size() <= bound;
    }

    public void init() {
        setBound(lits.realnVars() * maxpercent / 100);
        all.init();
        none.init();
    }

    protected void setBound(int newbound) {
        bound = newbound;
    }

    @Override
    public String toString() {
        return "Limit learning to clauses of size smaller or equal to "
            + maxpercent + "% of the number of variables";
    }

    public void setVarActivityListener(VarActivityListener s) {
        none.setVarActivityListener(s);
        all.setVarActivityListener(s);
    }

    @Override
    public Object clone() {
        LimitedLearning clone;

        try {
            clone = (LimitedLearning) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }

        clone.none = (NoLearningButHeuristics) this.none.clone();
        clone.all = (MiniSATLearning) this.all.clone();

        return clone;
    }
}

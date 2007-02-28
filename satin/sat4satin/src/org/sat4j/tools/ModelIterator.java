/*
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2006 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas E?n and Niklas S?rensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.sat4j.tools;

import java.io.Serializable;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * That class allows to iterate through all the models (implicants) of a
 * formula.
 * 
 * <pre>
 * ISolver solver = new ModelIterator(SolverFactory.OneSolver());
 * boolean unsat = true;
 * while (solver.isSatisfiable()) {
 *     unsat = false;
 *     int[] model = solver.model();
 *     // do something with model
 * }
 * if (unsat) {
 *     // UNSAT case
 * }
 * </pre>
 * 
 * @author leberre
 */
public class ModelIterator extends SolverDecorator implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean trivialfalsity = false;

    /**
     * @param solver
     */
    public ModelIterator(ISolver solver) {
        super(solver);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#model()
     */
    @Override
    public int[] model() {
        int[] last = super.model();
        IVecInt clause = new VecInt(last.length);
        for (int q : last) {
            clause.push(-q);
        }
        try {
            // System.out.println("adding " + clause);
            addClause(clause);
        } catch (ContradictionException e) {
            trivialfalsity = true;
        }
        return last;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#isSatisfiable()
     */
    @Override
    public boolean isSatisfiable() throws TimeoutException {
        if (trivialfalsity) {
            return false;
        }
        trivialfalsity = false;
        return super.isSatisfiable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#isSatisfiable(org.sat4j.datatype.VecInt)
     */
    @Override
    public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
        if (trivialfalsity) {
            return false;
        }
        trivialfalsity = false;
        return super.isSatisfiable(assumps);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#reset()
     */
    @Override
    public void reset() {
        trivialfalsity = false;
        super.reset();
    }
}
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

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * Another solver decorator that counts the number of solutions.
 * 
 * Note that this approach is quite naive so do not expect it to work on large
 * examples.
 * 
 * @author leberre
 * 
 */
public class SolutionCounter extends SolverDecorator {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SolutionCounter(ISolver solver) {
        super(solver);
    }

    public long countSolutions() throws TimeoutException {
        long nbsols = 0;
        boolean trivialfalsity = false;

        while (!trivialfalsity && isSatisfiable()) {
            nbsols++;
            int[] last = model();
            IVecInt clause = new VecInt(last.length);
            for (int q : last) {
                clause.push(-q);
            }
            try {
                // System.out.println("Sol number "+nbsols+" adding " + clause);
                addClause(clause);
            } catch (ContradictionException e) {
                trivialfalsity = true;
            }
        }
        return nbsols;
    }
}

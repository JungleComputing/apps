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
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * This solver decorator allows to detect whether or not the set of constraints
 * available in the solver has only one solution or not.
 * 
 * NOTE THAT THIS DECORATOR CANNOT BE USED WITH SOLVERS USING SPECIFIC DATA
 * STRUCTURES FOR BINARY OR TERNARY CLAUSES!
 * 
 * <code>
 SingleSolutionDetector problem = 
 new SingleSolutionDetector(SolverFactory.newMiniSAT());
 // feed problem/solver as usual

 if (problem.isSatisfiable()) {
 if (problem.hasASingleSolution()) {
 // great, the instance has a unique solution
 int [] uniquesolution = problem.getModel();
 } else {
 // too bad, got more than one
 }
 }
 *  </code>
 * 
 * @author leberre
 * 
 */
public class SingleSolutionDetector extends SolverDecorator {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SingleSolutionDetector(ISolver solver) {
        super(solver);
    }

    /**
     * Please use that method only after a positive answer from isSatisfiable()
     * (else a runtime exception will be launched).
     * 
     * @return true iff there is only one way to satisfy all the constraints in
     *         the solver.
     * @throws TimeoutException
     */
    public boolean hasASingleSolution() throws TimeoutException {
        return hasASingleSolution(new VecInt());
    }

    /**
     * Please use that method only after a positive answer from
     * isSatisfiable(assumptions) (else a runtime exception will be launched).
     * 
     * @param assumptions
     *            a set of literals (dimacs numbering) that must be satisfied.
     * @return true iff there is only one way to satisfy all the constraints in
     *         the solver using the provided set of assumptions.
     * @throws TimeoutException
     */
    public boolean hasASingleSolution(IVecInt assumptions)
            throws TimeoutException {
        int[] firstmodel = model();
        assert firstmodel != null;
        IVecInt clause = new VecInt(firstmodel.length);
        for (int q : firstmodel) {
            clause.push(-q);
        }
        boolean result = false;
        try {
            IConstr added = addClause(clause);
            result = !isSatisfiable(assumptions);
            removeConstr(added);
        } catch (ContradictionException e) {
            result = true;
        }
        return result;
    }
}

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

package org.sat4j.opt;

import java.math.BigInteger;

import org.sat4j.reader.ObjectiveFunction;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SolverDecorator;

public class PseudoOptDecorator extends SolverDecorator implements
        IOptimizationProblem {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private ObjectiveFunction objfct;

    private int[] prevmodel;

    public PseudoOptDecorator(ISolver solver) {
        super(solver);
    }

    public void setObjectTiveFunction(ObjectiveFunction objf) {
        objfct = objf;
    }

    public boolean admitABetterSolution() throws TimeoutException {
        boolean result = super.isSatisfiable();
        if (result)
            prevmodel = super.model();
        return result;
    }

    public boolean hasNoObjectiveFunction() {
        return objfct == null;
    }

    public boolean nonOptimalMeansSatisfiable() {
        return true;
    }

    public Number calculateObjective() {
        return objfct.calculateDegree(prevmodel);
    }

    public void discard() throws ContradictionException {
        super.addPseudoBoolean(objfct.getVars(), objfct.getCoeffs(), false,
                objfct.calculateDegree(prevmodel).subtract(BigInteger.ONE));
    }

    @Override
    public int[] model() {
        return prevmodel;
    }

}

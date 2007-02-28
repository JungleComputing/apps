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

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SolverDecorator;

/**
 * Computes a solution with the smallest number of satisfied literals.
 * 
 * @author leberre
 */
public class MinOneDecorator extends SolverDecorator implements
        IOptimizationProblem {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int[] prevmodel;

    public MinOneDecorator(ISolver solver) {
        super(solver);
    }

    public boolean admitABetterSolution() throws TimeoutException {
        boolean result = isSatisfiable();
        if (result) {
            prevmodel = super.model();
        }
        return result;
    }

    public boolean hasNoObjectiveFunction() {
        return false;
    }

    public boolean nonOptimalMeansSatisfiable() {
        return true;
    }

    private int counter;

    public Number calculateObjective() {
        counter = 0;
        for (int p : prevmodel) {
            if (p > 0) {
                counter++;
            }
        }
        return counter;
    }

    private final IVecInt literals = new VecInt();

    public void discard() throws ContradictionException {
        if (literals.isEmpty()) {
            for (int i = 1; i <= nVars(); i++) {
                literals.push(i);
            }
        }
        addAtMost(literals, counter - 1);
    }

    @Override
    public int[] model() {
        return prevmodel;
    }

    @Override
    public void reset() {
        literals.clear();
        super.reset();
    }

}

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

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SolverDecorator;

public class MinCostDecorator extends SolverDecorator implements
        IOptimizationProblem {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int[] costs;

    private int[] prevmodel;

    private final IVecInt vars = new VecInt();

    private final IVec<BigInteger> coeffs = new Vec<BigInteger>();

    public MinCostDecorator(ISolver solver) {
        super(solver);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.tools.SolverDecorator#newVar()
     */
    @Override
    public int newVar() {
        throw new UnsupportedOperationException();
    }

    /**
     * Setup the number of variables to use inside the solver.
     * 
     * It is mandatory to call that method before setting the cost of the
     * variables.
     * 
     * @param howmany
     *            the maximum number of variables in the solver.
     */
    @Override
    public int newVar(int howmany) {
        costs = new int[howmany + 1];
        // Arrays.fill(costs, 1);
        vars.clear();
        coeffs.clear();
        for (int i = 1; i <= howmany; i++) {
            vars.push(i);
            coeffs.push(BigInteger.ZERO);
        }
        // should the default cost be 1????
        // here it is 0
        return super.newVar(howmany);
    }

    /**
     * to know the cost of a given var.
     * 
     * @param var
     *            a variable in dimacs format
     * @return the cost of that variable when assigned to true
     */
    public int costOf(int var) {
        return costs[var];
    }

    /**
     * to set the cost of a given var.
     * 
     * @param var
     *            a variable in dimacs format
     * @param cost
     *            the cost of var when assigned to true
     */
    public void setCost(int var, int cost) {
        costs[var] = cost;
        coeffs.set(var - 1, BigInteger.valueOf(cost));
    }

    public boolean admitABetterSolution() throws TimeoutException {
        boolean result = super.isSatisfiable();
        if (result)
            prevmodel = super.model();
        return result;
    }

    public boolean hasNoObjectiveFunction() {
        return false;
    }

    public boolean nonOptimalMeansSatisfiable() {
        return true;
    }

    public Number calculateObjective() {
        return calculateDegree(prevmodel);
    }

    private int calculateDegree(int[] prevmodel2) {
        int tmpcost = 0;
        for (int i = 1; i < costs.length; i++) {
            if (prevmodel2[i - 1] > 0) {
                tmpcost += costs[i];
            }
        }
        return tmpcost;
    }

    public void discard() throws ContradictionException {
        super.addPseudoBoolean(vars, coeffs, false, BigInteger
                .valueOf(calculateDegree(prevmodel) - 1));
    }

    @Override
    public int[] model() {
        return prevmodel;
    }

}

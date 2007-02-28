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

import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SolverDecorator;

public abstract class AbstractSelectorVariablesDecorator extends
        SolverDecorator {

    protected int nborigvars;

    private int nbexpectedclauses;

    protected int nbnewvar;

    protected int[] prevfullmodel;

    public AbstractSelectorVariablesDecorator(ISolver solver) {
        super(solver);
    }

    @Override
    public int[] model() {
        int end = nborigvars - 1;
        while (Math.abs(prevfullmodel[end]) > nborigvars)
            end--;
        int[] shortmodel = new int[end + 1];
        for (int i = 0; i <= end; i++) {
            shortmodel[i] = prevfullmodel[i];
        }
        return shortmodel;
    }

    @Override
    public int newVar(int howmany) {
        nborigvars = super.newVar(howmany);
        return nborigvars;
    }

    @Override
    public void setExpectedNumberOfClauses(int nb) {
        nbexpectedclauses = nb;
        super.setExpectedNumberOfClauses(nb);
        super.newVar(nborigvars + nbexpectedclauses);
    }

    @Override
    public void reset() {
        super.reset();
        nbnewvar = 0;
    }

    public boolean admitABetterSolution() throws TimeoutException {
        boolean result = super.isSatisfiable();
        if (result)
            prevfullmodel = super.model();
        return result;
    }

}
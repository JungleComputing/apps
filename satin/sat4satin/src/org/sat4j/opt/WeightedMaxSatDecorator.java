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
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class WeightedMaxSatDecorator extends AbstractSelectorVariablesDecorator
        implements IOptimizationProblem {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final IVec<BigInteger> coefs = new Vec<BigInteger>();

    public WeightedMaxSatDecorator(ISolver solver) {
        super(solver);
    }

    @Override
    public IConstr addClause(IVecInt literals) throws ContradictionException {

        coefs.push(BigInteger.valueOf(literals.get(0)));
        literals.set(0, nborigvars + ++nbnewvar);
        return super.addClause(literals);
    }

    @Override
    public void reset() {
        coefs.clear();
        vec.clear();
        super.reset();
    }

    public boolean hasNoObjectiveFunction() {
        return false;
    }

    public boolean nonOptimalMeansSatisfiable() {
        return false;
    }

    private BigInteger counter;

    public Number calculateObjective() {
        counter = BigInteger.ZERO;
        for (int q : prevfullmodel) {
            if (q > nborigvars) {
                counter = counter.add(coefs.get(q - nborigvars - 1));
            }
        }
        return counter;
    }

    private final IVecInt vec = new VecInt();

    // private IConstr prevconstr;

    public void discard() throws ContradictionException {
        if (vec.isEmpty()) {
            for (int i = nborigvars + 1; i <= nVars(); i++) {
                vec.push(i);
            }
        }
        // if (prevconstr!=null)
        // super.removeConstr(prevconstr);

        // prevconstr =
        super.addPseudoBoolean(vec, coefs, false, counter
                .subtract(BigInteger.ONE));
    }

}

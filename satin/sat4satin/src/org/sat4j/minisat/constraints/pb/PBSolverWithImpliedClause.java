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
package org.sat4j.minisat.constraints.pb;

import java.math.BigInteger;

import org.sat4j.minisat.core.AssertingClauseGenerator;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class PBSolverWithImpliedClause extends PBSolver {

    public PBSolverWithImpliedClause(AssertingClauseGenerator acg,
            LearningStrategy learner, DataStructureFactory dsf, IOrder order) {
        super(acg, learner, dsf, order);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public IConstr addPseudoBoolean(IVecInt literals, IVec<BigInteger> coeffs,
            boolean moreThan, BigInteger degree) throws ContradictionException {
        IVecInt vlits = dimacs2internal(literals);
        assert vlits.size() == literals.size();
        assert literals.size() == coeffs.size();
        PBConstr result = (PBConstr) dsfactory.createPseudoBooleanConstraint(
                vlits, coeffs, moreThan, degree);
        if (result != null) {
            IVecInt clits = result.computeAnImpliedClause();
            if (clits != null) {
                addConstr(dsfactory.createClause(clits));
            }
        }
        return addConstr(result);
    }

    @Override
    public String toString(String prefix) {
        return super.toString(prefix) + "\n" + prefix
                + "Add implied clauses in preprocessing";
    }
}

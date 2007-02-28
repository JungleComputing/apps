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

package org.sat4j.minisat.constraints;

import java.math.BigInteger;

import org.sat4j.minisat.constraints.card.AtLeast;
import org.sat4j.minisat.core.Constr;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
@SuppressWarnings("PMD")
public abstract class AbstractCardinalityDataStructure extends
        AbstractDataStructureFactory {

    /*
     * Create a Cardinality Constraint from a PB format. The coefs should all be
     * equal to 1.
     */
    @Override
    public Constr createPseudoBooleanConstraint(IVecInt literals,
            IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
            throws ContradictionException {
        BigInteger diff = reduceToCard(coefs, literals);
        degree = degree.add(diff);
        assert coefficientsEqualToOne(coefs);
        if (moreThan) {
            return AtLeast.atLeastNew(solver, getVocabulary(), literals, degree
                    .intValue());
        }
        for (int i = 0; i < literals.size(); i++) {
            literals.set(i, literals.get(i) ^ 1);
        }
        return AtLeast.atLeastNew(solver, getVocabulary(), literals, coefs
                .size()
                - degree.intValue());
    }

    public static boolean coefficientsEqualToOne(IVec<BigInteger> v) {
        for (int i = 0; i < v.size(); i++) {
            if (!v.get(i).equals(BigInteger.ONE))
                return false;
        }
        return true;
    }

    private BigInteger reduceToCard(IVec<BigInteger> coefs, IVecInt literals) {
        int diff = 0;
        for (int i = 0; i < coefs.size(); i++) {
            assert coefs.get(i).abs().equals(BigInteger.ONE);
            if (coefs.get(i).signum() < 0) {
                assert coefs.get(i).equals(BigInteger.ONE.negate());
                diff++;
                literals.set(i, literals.get(i) ^ 1);
                coefs.set(i, BigInteger.ONE);
            }
        }
        return BigInteger.valueOf(diff);
    }
}
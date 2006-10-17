/*
 * Created on 16 avr. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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
        assert allAtOne(coefs);
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

    private boolean allAtOne(IVec<BigInteger> v) {
        for (int i = 0; i < v.size(); i++) {
            if (!v.get(i).equals(BigInteger.ONE)) {
                return false;
            }
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

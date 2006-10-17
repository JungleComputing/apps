/*
 * Created on 21 juin 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sat4j.minisat.constraints;

import java.math.BigInteger;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.cnf.WLClause;
import org.sat4j.minisat.constraints.pb.IInternalPBConstraintCreator;
import org.sat4j.minisat.constraints.pb.WatchPb;
import org.sat4j.minisat.core.Constr;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class AbstractPBDataStructureFactory extends
        AbstractDataStructureFactory implements IInternalPBConstraintCreator {

    public Constr createClause(IVecInt literals) throws ContradictionException {
        IVecInt v = WLClause.sanityCheck(literals, getVocabulary(), solver);
        if (v == null)
            return null;
        IVecInt coefs = new VecInt(v.size(), 1);
        return constraintFactory(v, coefs, true, 1);
    }

    public Constr createUnregisteredClause(IVecInt literals) {
        return new WLClause(literals, getVocabulary());
    }

    @Override
    public Constr createCardinalityConstraint(IVecInt literals, int degree)
            throws ContradictionException {
        IVecInt coefs = new VecInt(literals.size(), 1);
        return constraintFactory(literals, coefs, true, degree);
    }

    @Override
    public Constr createPseudoBooleanConstraint(IVecInt literals,
            IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
            throws ContradictionException {
        return constraintFactory(literals, coefs, moreThan, degree);
    }

    /**
     * @param literals
     * @param coefs
     * @param moreThan
     * @param degree
     * @return
     */
    protected abstract WatchPb constraintFactory(IVecInt literals,
            IVecInt coefs, boolean moreThan, int degree)
            throws ContradictionException;

    protected abstract WatchPb constraintFactory(IVecInt literals,
            IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
            throws ContradictionException;

    @Override
    public void reset() {
    }

    @Override
    public Constr createUnregisteredPseudoBooleanConstraint(IVecInt literals,
            IVec<BigInteger> coefs, BigInteger degree) {
        return constraintFactory(literals, coefs, degree);
    }

    public IConstr createUnregisteredPseudoBooleanConstraint(IVecInt literals,
            IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
            throws ContradictionException {
        return constraintFactory(literals, coefs, moreThan, degree);
    }

    /**
     * @param literals
     * @param coefs
     * @param degree
     * @return
     */
    protected abstract WatchPb constraintFactory(IVecInt literals,
            IVecInt coefs, int degree);

    protected abstract WatchPb constraintFactory(IVecInt literals,
            IVec<BigInteger> coefs, BigInteger degree);

}

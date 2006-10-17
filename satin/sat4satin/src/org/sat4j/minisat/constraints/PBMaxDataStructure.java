/*
 * Created on 16 avr. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.constraints;

import java.math.BigInteger;

import org.sat4j.minisat.constraints.pb.MaxWatchPb;
import org.sat4j.minisat.constraints.pb.WatchPb;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PBMaxDataStructure extends AbstractPBDataStructureFactory {

    private static final long serialVersionUID = 1L;

    /**
     * @param literals
     * @param coefs
     * @param moreThan
     * @param degree
     * @return
     * @throws ContradictionException
     */
    @Override
    protected WatchPb constraintFactory(IVecInt literals, IVecInt coefs,
        boolean moreThan, int degree) throws ContradictionException {
        return MaxWatchPb.maxWatchPbNew(solver, getVocabulary(), literals,
            coefs, moreThan, degree);
    }

    /**
     * @param literals
     * @param coefs
     * @param degree
     * @return
     */
    @Override
    protected WatchPb constraintFactory(IVecInt literals, IVecInt coefs,
        int degree) {
        return MaxWatchPb.watchPbNew(getVocabulary(), literals, coefs, true,
            degree);
    }

    /**
     * @param literals
     * @param coefs
     * @param moreThan
     * @param degree
     * @return
     * @throws ContradictionException
     */
    @Override
    protected WatchPb constraintFactory(IVecInt literals,
        IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
        throws ContradictionException {
        return MaxWatchPb.maxWatchPbNew(solver, getVocabulary(), literals,
            coefs, moreThan, degree);
    }

    /**
     * @param literals
     * @param coefs
     * @param degree
     * @return
     */
    @Override
    protected WatchPb constraintFactory(IVecInt literals,
        IVec<BigInteger> coefs, BigInteger degree) {
        return MaxWatchPb.watchPbNew(getVocabulary(), literals, coefs, true,
            degree);
    }

}

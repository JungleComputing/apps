/*
 * Created on 16 avr. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.constraints;

import java.math.BigInteger;

import org.sat4j.minisat.constraints.pb.MinWatchPb;
import org.sat4j.minisat.constraints.pb.WatchPb;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PBMinDataStructure extends AbstractPBDataStructureFactory {

    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.AbstractPBDataStructureFactory#constraintFactory(org.sat4j.specs.VecInt,
     *      org.sat4j.specs.VecInt, boolean, int)
     */
    @Override
    protected WatchPb constraintFactory(IVecInt literals, IVecInt coefs,
            boolean moreThan, int degree) throws ContradictionException {
        return MinWatchPb.minWatchPbNew(solver, getVocabulary(), literals,
                coefs, moreThan, degree);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.AbstractPBDataStructureFactory#constraintFactory(org.sat4j.specs.VecInt,
     *      org.sat4j.specs.VecInt, int)
     */
    @Override
    protected WatchPb constraintFactory(IVecInt literals, IVecInt coefs,
            int degree) {
        return MinWatchPb.watchPbNew(getVocabulary(), literals, coefs, true,
                degree);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.AbstractPBDataStructureFactory#constraintFactory(org.sat4j.specs.VecInt,
     *      org.sat4j.specs.VecInt, boolean, int)
     */
    @Override
    protected WatchPb constraintFactory(IVecInt literals,
            IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
            throws ContradictionException {
        return MinWatchPb.minWatchPbNew(solver, getVocabulary(), literals,
                coefs, moreThan, degree);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.AbstractPBDataStructureFactory#constraintFactory(org.sat4j.specs.VecInt,
     *      org.sat4j.specs.VecInt, int)
     */
    @Override
    protected WatchPb constraintFactory(IVecInt literals,
            IVec<BigInteger> coefs, BigInteger degree) {
        return MinWatchPb.watchPbNew(getVocabulary(), literals, coefs, true,
                degree);
    }

}

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

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.cnf.WLClause;
import org.sat4j.minisat.constraints.pb.IDataStructurePB;
import org.sat4j.minisat.constraints.pb.PBConstr;
import org.sat4j.minisat.constraints.pb.WatchPb;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public abstract class AbstractPBClauseCardConstrDataStructure extends
        AbstractPBDataStructureFactory {

    private static final BigInteger MAX_INT_VALUE = BigInteger
            .valueOf(Integer.MAX_VALUE);

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.AbstractPBDataStructureFactory#constraintFactory(org.sat4j.specs.VecInt,
     *      org.sat4j.specs.VecInt, boolean, int)
     */
    @Override
    protected PBConstr constraintFactory(IVecInt literals, IVecInt coefs,
            boolean moreThan, int degree) throws ContradictionException {
        return constraintFactory(literals, WatchPb.toVecBigInt(coefs),
                moreThan, WatchPb.toBigInt(degree));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.AbstractPBDataStructureFactory#constraintFactory(org.sat4j.specs.VecInt,
     *      org.sat4j.specs.VecInt, int)
     */
    @Override
    protected PBConstr constraintFactory(IVecInt literals, IVecInt coefs,
            int degree) {
        return constraintFactory(literals, WatchPb.toVecBigInt(coefs), WatchPb
                .toBigInt(degree));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.AbstractPBDataStructureFactory#constraintFactory(org.sat4j.specs.VecInt,
     *      org.sat4j.specs.VecInt, boolean, int)
     */
    @Override
    protected PBConstr constraintFactory(IVecInt literals,
            IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
            throws ContradictionException {
        IDataStructurePB mpb = WatchPb.niceParameters(literals, coefs,
                moreThan, degree, getVocabulary());
        if (mpb == null)
            return null;
        int size = mpb.size();
        int[] lits = new int[size];
        BigInteger[] normCoefs = new BigInteger[size];
        mpb.buildConstraintFromMapPb(lits, normCoefs);
        if (mpb.getDegree().equals(BigInteger.ONE)) {
            IVecInt v = WLClause.sanityCheck(new VecInt(lits), getVocabulary(),
                    solver);
            if (v == null)
                return null;
            return constructClause(v);
        }
        if (coefficientsEqualToOne(new Vec<BigInteger>(normCoefs))) {
            assert mpb.getDegree().compareTo(MAX_INT_VALUE) < 0;
            return constructCard(new VecInt(lits), mpb.getDegree().intValue());
        }
        return constructPB(mpb);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.AbstractPBDataStructureFactory#constraintFactory(org.sat4j.specs.VecInt,
     *      org.sat4j.specs.VecInt, int)
     */
    @Override
    protected PBConstr constraintFactory(IVecInt literals,
            IVec<BigInteger> coefs, BigInteger degree) {
        if (degree.equals(BigInteger.ONE)) {
            return constructLearntClause(literals);
        }
        if (coefficientsEqualToOne(coefs)) {
            return constructLearntCard(literals, degree.intValue());
        }
        return constructLearntPB(literals, coefs, degree);
    }

    private static boolean coefficientsEqualToOne(IVec<BigInteger> coeffs) {
        for (int i = 0; i < coeffs.size(); i++)
            if (!coeffs.get(i).equals(BigInteger.ONE))
                return false;
        return true;
    }

    abstract protected PBConstr constructClause(IVecInt v);

    abstract protected PBConstr constructCard(IVecInt lits, int degree)
            throws ContradictionException;

    abstract protected PBConstr constructPB(IDataStructurePB mpb)
            throws ContradictionException;

    abstract protected PBConstr constructLearntClause(IVecInt literals);

    abstract protected PBConstr constructLearntCard(IVecInt literals, int degree);

    abstract protected PBConstr constructLearntPB(IVecInt literals,
            IVec<BigInteger> coefs, BigInteger degree);

}

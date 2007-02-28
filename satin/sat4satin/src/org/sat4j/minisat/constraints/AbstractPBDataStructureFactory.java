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

import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.cnf.LearntWLClause;
import org.sat4j.minisat.constraints.cnf.WLClause;
import org.sat4j.minisat.constraints.pb.IInternalPBConstraintCreator;
import org.sat4j.minisat.constraints.pb.PBConstr;
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
        return new LearntWLClause(literals, getVocabulary());
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
    protected abstract PBConstr constraintFactory(IVecInt literals,
            IVecInt coefs, boolean moreThan, int degree)
            throws ContradictionException;

    protected abstract PBConstr constraintFactory(IVecInt literals,
            IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
            throws ContradictionException;

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
    protected abstract PBConstr constraintFactory(IVecInt literals,
            IVecInt coefs, int degree);

    protected abstract PBConstr constraintFactory(IVecInt literals,
            IVec<BigInteger> coefs, BigInteger degree);

}

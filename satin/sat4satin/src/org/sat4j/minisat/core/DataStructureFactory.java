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

package org.sat4j.minisat.core;

import java.math.BigInteger;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * The aim of the factory is to provide a concrete implementation of clauses,
 * cardinality constraints and pseudo boolean consraints.
 * 
 * @author leberre
 */
public interface DataStructureFactory {

    /**
     * @param literals
     *            a set of literals using Dimacs format (signed non null
     *            integers).
     * @return null if the constraint is a tautology.
     * @throws ContradictionException
     *             the constraint is trivially unsatisfiable.
     * @throws UnsupportedOperationException
     *             there is no concrete implementation for that constraint.
     */
    Constr createClause(IVecInt literals) throws ContradictionException;

    Constr createUnregisteredClause(IVecInt literals);

    void learnConstraint(Constr constr);

    Constr createCardinalityConstraint(IVecInt literals, int degree)
            throws ContradictionException;

    Constr createPseudoBooleanConstraint(IVecInt literals,
            IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
            throws ContradictionException;

    Constr createUnregisteredPseudoBooleanConstraint(IVecInt literals,
            IVec<BigInteger> coefs, BigInteger degree);

    void setUnitPropagationListener(UnitPropagationListener s);

    void setLearner(Learner l);

    void reset();

    ILits getVocabulary();

    /**
     * @param p
     * @return a vector containing all the objects to be notified of the
     *         satisfaction of that literal.
     */
    IVec<Propagatable> getWatchesFor(int p);

    /**
     * @param p
     * @param i
     *            the index of the conflicting constraint
     */
    void conflictDetectedInWatchesFor(int p, int i);
}

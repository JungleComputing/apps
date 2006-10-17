/*
 * Created on 16 avr. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.core;

import java.math.BigInteger;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre The aim of the factory is to provide a concrete
 *         implementation of clauses, cardinality constraints and pseudo boolean
 *         consraints.
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
     * @return a vector containing all the objects to be notified of the satisfaction of that literal.
     */
    IVec<Propagatable> getWatchesFor(int p);

    /**
     * @param p
     * @param i
     *            the index of the conflicting constraint
     */
    void conflictDetectedInWatchesFor(int p, int i);

    Object clone();
}

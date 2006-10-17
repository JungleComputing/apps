/*
 * SAT4J: a SATisfiability library for Java   
 * Copyright (C) 2004 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas E?n and Niklas S?rensson.
 * Proceedings of the Sixth International Conference on Theory 
 * and Applications of Satisfiability Testing, LNCS 2919, 
 * pp 502-518, 2003.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 */
package org.sat4j.specs;

import java.io.PrintStream;
import java.math.BigInteger;

/**
 * That interface contains all the services available on a SAT
 * solver.
 *         
 * @author leberre 
 */
public interface ISolver extends IProblem {

    /**
     * Create a new variable in the solver (and thus in the vocabulary).
     * 
     * WE STRONGLY ENCOURAGE TO PRECOMPUTE THE NUMBER OF VARIABLES NEEDED
     * AND TO USE newVar(howmany) INSTEAD. IF YOU EXPERIENCE A PROBLEM OF 
     * EFFICIENCY WHEN READING/BUILDING YOUR SAT INSTANCE, PLEASE CHECK 
     * THAT YOU ARE NOT USING THAT METHOD.
     * 
     * @return the number of variables available in the vocabulary, which is the
     *         identifier of the new variable.
     */
    int newVar();

    /**
     * Create <code>howmany</code> variables in the solver (and thus in the
     * vocabulary).
     * 
     * @param howmany
     *            number of variables to create
     * @return the total number of variables available in the solver (the
     *         highest variable number)
     */
    int newVar(int howmany);

    /**
     * Create a clause from a set of literals The literals are represented by
     * non null integers such that opposite literals a represented by opposite
     * values. (clasical Dimacs way of representing literals).
     * 
     * @param literals
     *            a set of literals
     * @return a reference to the constraint added in the solver, to use in removeConstr().
     * @throws ContradictionException iff the vector of literals is empty or if it contains 
     *        only falsified literals after unit propagation 
     * @see #removeConstr(IConstr)
     */
    IConstr addClause(IVecInt literals) throws ContradictionException;

    /**
     * Remove a constraint returned by one of the add method from the solver.
     * All learnt clauses will be cleared.
     * 
     * @param c
     *            a constraint returned by one of the add method.
     * @return true if the constraint was sucessfully removed.
     */
    boolean removeConstr(IConstr c);

    /**
     * Create clauses from a set of set of literals. This is convenient to
     * create in a single call all the clauses (mandatory for the distributed
     * version of the solver). It is mainly a loop to addClause().
     * 
     * @param clauses
     *            a vector of set (VecInt) of literals in the dimacs format. 
     *            The vector can be reused since the solver is not supposed 
     *            to keep a reference to that vector.
     * @throws ContradictionException iff the vector of literals is empty or if it contains 
     *        only falsified literals after unit propagation 
     * @see #addClause(IVecInt)
     */
    void addAllClauses(IVec<IVecInt> clauses) throws ContradictionException;

    /**
     * Create a cardinality constraint of the type "at most n of those literals
     * must be satisfied"
     * 
     * @param literals
     *            a set of literals
     *            The vector can be reused since the solver is not supposed 
     *            to keep a reference to that vector.
     * @param degree
     *            the degree of the cardinality constraint
     * @return a reference to the constraint added in the solver, to use in removeConstr().
     * @throws ContradictionException iff the vector of literals is empty or if it contains 
     *        more than degree satisfied literals after unit propagation 
     * @see #removeConstr(IConstr)     
     */

    IConstr addAtMost(IVecInt literals, int degree)
            throws ContradictionException;

    /**
     * Create a cardinality constraint of the type "at least n of those literals
     * must be satisfied"
     * 
     * @param literals
     *            a set of literals.
     *            The vector can be reused since the solver is not supposed 
     *            to keep a reference to that vector.
     * @param degree
     *            the degree of the cardinality constraint
     * @return a reference to the constraint added in the solver, to use in removeConstr().
     * @throws ContradictionException iff the vector of literals is empty or if degree literals are not
     * remaining unfalsified after unit propagation 
     * @see #removeConstr(IConstr)
     */
    IConstr addAtLeast(IVecInt literals, int degree)
            throws ContradictionException;

    /**
     * Create a Pseudo-Boolean constraint of the type "at least n of those
     * literals must be satisfied"
     * 
     * @param lits
     *            a set of literals.
     *            The vector can be reused since the solver is not supposed 
     *            to keep a reference to that vector.
     * @param coeffs
     *            the coefficients of the literals.
     *            The vector can be reused since the solver is not supposed 
     *            to keep a reference to that vector.
     * @param moreThan
     *            true if it is a constraint >= degree
     * @param d
     *            the degree of the cardinality constraint
     * @return a reference to the constraint added in the solver, to use in removeConstr().
     * @throws ContradictionException iff the vector of literals is empty or if the constraint 
     *         is falsified after unit propagation 
     * @see #removeConstr(IConstr)
     */
    IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs, boolean moreThan,
            BigInteger d) throws ContradictionException;

    /**
     * To set the internal timeout of the solver. When the timeout is reached, a
     * timeout exception is launched by the solver.
     * 
     * @param t
     *            the timeout (in s)
     */
    void setTimeout(int t);

    /**
     * Useful to check the internal timeout of the solver.
     * 
     * @return the internal timeout of the solver (in second)
     */
    int getTimeout();

    /**
     * Clean up the internal state of the solver.
     */
    void reset();

    /**
     * Display statistics to the given output stream
     * 
     * @param out
     * @param prefix
     *            the prefix to put in front of each line
     */
    void printStat(PrintStream out, String prefix);

    /**
     * Display a textual representation of the solver configuration.
     * 
     * @param prefix
     *            the prefix to use on each line.
     * @return a textual description of the solver internals.
     */
    String toString(String prefix);

}
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

package org.sat4j.specs;

/**
 * Access to the information related to a given problem instance.
 * 
 * @author leberre
 */
public interface IProblem {
    /**
     * Provide a model (if any) for a satisfiable formula. That method should be
     * called AFTER isSatisfiable() or isSatisfiable(IVecInt) if the formula is
     * satisfiable. Else an exception UnsupportedOperationException is launched.
     * 
     * @return a model of the formula as an array of literals to satisfy.
     * @see #isSatisfiable()
     * @see #isSatisfiable(IVecInt)
     */
    int[] model();

    /**
     * Provide the truth value of a specific variable in the model. That method
     * should be called AFTER isSatisfiable() if the formula is satisfiable.
     * Else an exception UnsupportedOperationException is launched.
     * 
     * @param var
     *            the variable id in Dimacs format
     * @return the truth value of that variable in the model
     * @since 1.6
     * @see #model()
     */
    boolean model(int var);

    /**
     * Check the satisfiability of the set of constraints contained inside the
     * solver.
     * 
     * @return true if the set of constraints is satisfiable, else false.
     */
    boolean isSatisfiable() throws TimeoutException;

    /**
     * Check the satisfiability of the set of constraints contained inside the
     * solver.
     * 
     * @param assumps
     *            a set of literals (represented by usual non null integers in
     *            Dimacs format).
     * @return true if the set of constraints is satisfiable when literals are
     *         satisfied, else false.
     */
    boolean isSatisfiable(IVecInt assumps) throws TimeoutException;

    /**
     * Look for a model satisfying all the clauses available in the problem. It
     * is an alternative to isSatisfiable() and model() methods, as shown in the
     * pseudo-code: <code>
     if (isSatisfiable()) {
     return model();
     }
     return null; 
     </code>
     * 
     * @return a model of the formula as an array of literals to satisfy, or
     *         <code>null</code> if no model is found
     * @throws TimeoutException
     *             if a model cannot be found within the given timeout.
     * @since 1.7
     */
    int[] findModel() throws TimeoutException;

    /**
     * Look for a model satisfying all the clauses available in the problem. It
     * is an alternative to isSatisfiable(IVecInt) and model() methods, as shown
     * in the pseudo-code: <code>
     if (isSatisfiable(assumpt)) {
     return model();
     }
     return null; 
     </code>
     * 
     * @return a model of the formula as an array of literals to satisfy, or
     *         <code>null</code> if no model is found
     * @throws TimeoutException
     *             if a model cannot be found within the given timeout.
     * @since 1.7
     */
    int[] findModel(IVecInt assumps) throws TimeoutException;

    /**
     * To know the number of constraints currently available in the solver.
     * (without taking into account learnt constraints).
     * 
     * @return the number of contraints added to the solver
     */
    int nConstraints();

    /**
     * To know the number of variables used in the solver.
     * 
     * @return the number of variables created using newVar().
     */
    int nVars();

}
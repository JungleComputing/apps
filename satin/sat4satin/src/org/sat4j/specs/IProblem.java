/*
 * Created on 20 d�c. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
     * called AFTER isSatisfiable() if the formula is satisfiable. Else an
     * exception UnsupportedOperationException is launched.
     * 
     * @return a model of the formula as an array of literals to satisfy.
     */
    int[] model();

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
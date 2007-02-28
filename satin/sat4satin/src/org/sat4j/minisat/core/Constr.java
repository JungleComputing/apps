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

import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVecInt;

/*
 * Created on 16 oct. 2003
 */

/**
 * Basic constraint abstraction used in Solver.
 * 
 * Any new constraint type should implement that interface.
 * 
 * @author leberre
 */
public interface Constr extends Propagatable, IConstr {

    /**
     * Remove a constraint from the solver.
     * 
     */
    void remove();

    /**
     * Simplifies a constraint, by removing top level falsified literals for
     * instance.
     * 
     * @return true iff the constraint is satisfied.
     */
    boolean simplify();

    /**
     * Compute the reason for a given assignment.
     * 
     * If the constraint is a clause, it is supposed to be either a unit clause
     * or a falsified one.
     * 
     * @param p
     *            a satisfied literal (or Lit.UNDEFINED)
     * @param outReason
     *            the list of falsified literals whose negation is the reason of
     *            the assignment of p to true.
     */
    void calcReason(int p, IVecInt outReason);

    /**
     * Increase the constraint activity.
     * 
     * @param claInc
     *            the value to increase the activity with
     */
    void incActivity(double claInc);

    /**
     * To obtain the activity of the constraint.
     * 
     * @return the activity of the clause.
     */
    double getActivity();

    /**
     * Indicate wether a constraint is responsible from an assignment.
     * 
     * @return true if a constraint is a "reason" for an assignment.
     */
    boolean locked();

    /**
     * Mark a constraint as learnt.
     */

    void setLearnt();

    /**
     * Register the constraint to the solver.
     */
    void register();

    /**
     * Rescale the clause activity by a value.
     * 
     * @param d
     *            the value to rescale the clause activity with.
     */
    void rescaleBy(double d);

    /**
     * Method called when the constraint is to be asserted. It means that the
     * constraint was learnt during the search and it should now propagate some
     * truth values. In the clausal case, only one literal should be propagated.
     * In other cases, it might be different.
     * 
     * @param s
     *            a UnitPropagationListener to use for unit propagation.
     */
    void assertConstraint(UnitPropagationListener s);

    /**
     * SATIN: Mark a constraint as learnt from other threads
     */

    void setLearntGlobal();
}

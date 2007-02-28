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

import java.io.Serializable;

/**
 * Interface to the solver main steps. Useful for integrating search
 * visualization or debugging.
 * 
 * @author daniel
 * 
 */
public interface SearchListener extends Serializable {

    /**
     * decision variable
     * 
     * @param p
     */
    void assuming(int p);

    /**
     * Unit propagation
     * 
     * @param p
     */
    void propagating(int p);

    /**
     * backtrack on a decision variable
     * 
     * @param p
     */
    void backtracking(int p);

    /**
     * adding forced variable (conflict driven assignment)
     */
    void adding(int p);

    /**
     * learning a new clause
     * 
     * @param c
     */
    void learn(Constr c);

    /**
     * delete a clause
     */
    void delete(int[] clause);

    /**
     * a conflict has been found.
     * 
     */
    void conflictFound();

    /**
     * a solution is found.
     * 
     */
    void solutionFound();

    /**
     * starts a propagation
     */
    void beginLoop();

    /**
     * Start the search.
     * 
     */
    void start();

    /**
     * End the search.
     * 
     * @param result
     *            the result of the search.
     */
    void end(Lbool result);
}

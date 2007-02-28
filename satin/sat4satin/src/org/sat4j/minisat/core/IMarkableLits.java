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

import java.util.Set;

import org.sat4j.specs.IVecInt;

/**
 * Vocabulary in which literals can be marked.
 * 
 * @author daniel
 * 
 */
public interface IMarkableLits extends ILits {
    int MARKLESS = 0;

    /**
     * Mark a given literal with a given mark.
     * 
     * @param p
     *            the literal
     * @param mark
     *            an integer used to mark the literal. The specific mark
     *            MARKLESS is used to denote that the literal is not marked. The
     *            marks are supposed to be positive in the most common cases.
     */
    void setMark(int p, int mark);

    /**
     * Mark a given literal.
     * 
     * @param p
     *            a literal
     */
    void setMark(int p);

    /**
     * To get the mark for a given literal.
     * 
     * @param p
     *            a literal
     * @return the mark associated with that literal, or MARKLESS if the literal
     *         is not marked.
     */
    int getMark(int p);

    /**
     * To know if a given literal is marked, i.e. has a mark different from
     * MARKLESS.
     * 
     * @param p
     *            a literal
     * @return true iif the literal is marked.
     */
    boolean isMarked(int p);

    /**
     * Set the mark of a given literal to MARKLESS.
     * 
     * @param p
     *            a literal
     */
    void resetMark(int p);

    /**
     * Set all the literal marks to MARKLESS
     * 
     */
    void resetAllMarks();

    /**
     * Returns the set of all marked literals.
     * 
     * @return a set of literals whose mark is different from MARKLESS.
     */
    IVecInt getMarkedLiterals();

    /**
     * Returns that set of all the literals having a specific mark.
     * 
     * @param mark
     *            a mark
     * @return a set of literals whose mark is mark
     */
    IVecInt getMarkedLiterals(int mark);

    /**
     * Returns the set of all marked variables. A variable is marked iff at
     * least one of its literal is marked.
     * 
     * @return a set of variables whose mark is different from MARKLESS.
     */
    IVecInt getMarkedVariables();

    /**
     * Returns the set of all variables having a specific mark. A variable is
     * marked iff at least one of its literal is marked.
     * 
     * @param mark
     *            a mark.
     * @return a set of variables whose mark is mark.
     */
    IVecInt getMarkedVariables(int mark);

    /**
     * 
     * @return a list of marks used to mark the literals.
     */
    Set<Integer> getMarks();
}

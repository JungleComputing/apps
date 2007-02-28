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

/**
 * Specific vocabulary taking special care of binary clauses.
 * 
 * @author leberre
 */
public interface ILits2 extends ILits {

    /**
     * To know the number of binary clauses in which the literal occurs. Please
     * note that this method should only be used in conjunction with the
     * BinaryClauses data structure.
     * 
     * @param p
     * @return the number of binary clauses.
     */
    int nBinaryClauses(int p);

    /**
     * Method to create a binary clause.
     * 
     * @param lit1
     *            the first literal of the clause
     * @param lit2
     *            the second literal of the clause
     */
    void binaryClauses(int lit1, int lit2);

}
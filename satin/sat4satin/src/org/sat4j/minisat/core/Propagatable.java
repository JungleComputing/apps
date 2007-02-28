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
 * This interface is to be implemented by the classes wanted to be notified of
 * the falsification of a literal.
 * 
 * @author leberre
 */
public interface Propagatable {

    /**
     * Propagate the truth value of a literal in constraints in which that
     * literal is falsified.
     * 
     * @param s
     *            something able to perform unit propagation
     * @param p
     *            the literal being propagated. Its negation must appear in the
     *            constraint.
     * @return false iff an inconsistency (a contradiction) is detected.
     */
    boolean propagate(UnitPropagationListener s, int p);

}
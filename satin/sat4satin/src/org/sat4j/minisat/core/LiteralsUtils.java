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
 * Utility methods to avoid using bit manipulation inside code. One should use
 * Java 1.5 import static feature to use it without class qualification inside
 * the code.
 * 
 * In the DIMACS format, the literals are represented by signed integers, 0
 * denoting the end of the clause. In the solver, the literals are represented
 * by positive integers, in order to use them as index in arrays for instance.
 * 
 * <pre>
 *  int p : a literal (p&gt;1)
 *  p &circ; 1 : the negation of the literal
 *  p &gt;&gt; 1 : the DIMACS number reresenting the variable.
 *  int v : a DIMACS variable (v&gt;0)
 *  v &lt;&lt; 1 : a positive literal for that variable in the solver.
 *  v &lt;&lt; 1 &circ; 1 : a negative literal for that variable. 
 * </pre>
 * 
 * @author leberre
 * 
 */
public final class LiteralsUtils {

    private LiteralsUtils() {
        // no instance supposed to be created.
    }

    public static int var(int p) {
        assert p > 1;
        return p >> 1;
    }

    public static int neg(int p) {
        return p ^ 1;
    }
}

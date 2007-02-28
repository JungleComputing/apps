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
package org.sat4j;

/**
 * Enumeration allowing to manage easily exit code for the SAT and PB
 * Competitions.
 * 
 * @author leberre
 * 
 */
public enum ExitCode {
    OPTIMUM_FOUND(30, "OPTIMUM FOUND"), SATISFIABLE(10), UNKNOWN(0), UNSATISFIABLE( //$NON-NLS-1$
            20);

    /** value of the exit code. */
    private final int value;

    /** alternative textual representation of the exit code. */
    private final String str;

    /**
     * creates an exit code with a given value.
     * 
     * @param i
     *            the value of the exit code
     */
    ExitCode(final int value) {
        this.value = value;
        str = null;
    }

    /**
     * creates an exit code with a given value and an alternative textual
     * representation.
     * 
     * @param i
     *            the value of the exit code
     * @param str
     *            the alternative textual representation
     */
    ExitCode(final int i, final String str) {
        this.value = i;
        this.str = str;
    }

    /**
     * @return the exit code value
     */
    public int value() {
        return value;
    }

    /**
     * @return the name of the enum or the alternative textual representation if
     *         any.
     */
    @Override
    public String toString() {
        final String result;
        if (str == null) {
            result = super.toString();
        } else {
            result = str;
        }
        return result;
    }
}
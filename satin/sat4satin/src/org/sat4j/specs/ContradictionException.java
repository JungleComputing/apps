/*
 * SAT4J: a SATisfiability library for Java
 * Copyright (C) 2004 Daniel Le Berre
 *
 * Based on the original minisat specification from:
 *
 * An extensible SAT solver. Niklas Een and Niklas Serensson.
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

/*
 * Created on 15 nov. 2003
 */

/**
 * That exception is launched whenever a trivial contradiction
 * is found (e.g. null clause).
 *         
 * @author leberre 
 */
public class ContradictionException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    public ContradictionException() {
        super();
    }

    /**
     * @param message
     *            un message
     */
    public ContradictionException(final String message) {
        super(message);
    }

    /**
     * @param cause
     *            la cause de l'exception
     */
    public ContradictionException(final Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     *            un message
     * @param cause
     *            une cause
     */
    public ContradictionException(final String message, final Throwable cause) {
        super(message, cause);
    }

}

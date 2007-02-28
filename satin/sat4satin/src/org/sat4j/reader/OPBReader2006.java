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

package org.sat4j.reader;

import java.io.IOException;

import org.sat4j.specs.ISolver;

/**
 * Reader complying to the PB06 input format.
 * 
 * @author leberre
 * 
 */
public class OPBReader2006 extends OPBReader2005 {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public OPBReader2006(ISolver solver) {
        super(solver);
    }

    /**
     * read a term into coeff and var
     * 
     * @param coeff:
     *            the coefficient of the variable
     * @param var:
     *            the indentifier we read
     * @throws IOException
     * @throws ParseException
     */
    @Override
    protected void readTerm(StringBuffer coeff, StringBuffer var)
            throws IOException, ParseFormatException {
        readInteger(coeff);

        skipSpaces();

        if (!readIdentifier(var))
            throw new ParseFormatException("identifier expected");
    }
}

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
import java.io.LineNumberReader;
import java.util.StringTokenizer;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 * A reader for cardinality contraints.
 * 
 * @author leberre
 */
@Deprecated
public class CardDimacsReader extends DimacsReader {

    /**
     * 
     */
    private static final long serialVersionUID = 3258130241376368435L;

    public CardDimacsReader(ISolver solver) {
        super(solver);
    }

    /**
     * @param in
     *            the input stream
     * @throws IOException
     *             iff an IO problems occurs
     * @throws ParseFormatException
     *             if the input stream does not comply with the DIMACS format.
     * @throws ContradictionException
     *             si le probl?me est trivialement inconsistant.
     */
    @Override
    protected void readConstrs(LineNumberReader in) throws IOException,
            ParseFormatException, ContradictionException {
        int lit;
        String line;
        StringTokenizer stk;

        int realNbOfClauses = 0;

        IVecInt literals = new VecInt();

        while (true) {
            line = in.readLine();

            if (line == null) {
                // end of file
                if (literals.size() > 0) {
                    // no 0 end the last clause
                    solver.addClause(literals);
                    realNbOfClauses++;
                }

                break;
            }

            if (line.startsWith("c ")) {
                // skip commented line
                continue;
            }
            if (line.startsWith("%") && expectedNbOfConstr == realNbOfClauses) {
                System.out
                        .println("Ignoring the rest of the file (SATLIB format");
                break;
            }
            stk = new StringTokenizer(line);
            String token;

            while (stk.hasMoreTokens()) {
                // on lit le prochain token
                token = stk.nextToken();

                if ("<=".equals(token) || ">=".equals(token)) {
                    // on est sur une contrainte de cardinalit?
                    readCardinalityConstr(token, stk, literals);
                    literals.clear();
                    realNbOfClauses++;
                } else {
                    lit = Integer.parseInt(token);
                    if (lit == 0) {
                        if (literals.size() > 0) {
                            solver.addClause(literals);
                            literals.clear();
                            realNbOfClauses++;
                        }
                    } else {
                        literals.push(lit);
                    }
                }
            }
        }
        if (expectedNbOfConstr != realNbOfClauses) {
            throw new ParseFormatException("wrong nbclauses parameter. Found "
                    + realNbOfClauses + ", " + expectedNbOfConstr + " expected");
        }
    }

    private void readCardinalityConstr(String token, StringTokenizer stk,
            IVecInt literals) throws ContradictionException,
            ParseFormatException {
        int card = Integer.parseInt(stk.nextToken());
        int lit = Integer.parseInt(stk.nextToken());
        if (lit == 0) {
            if ("<=".equals(token)) {
                solver.addAtMost(literals, card);
            } else if (">=".equals(token)) {
                solver.addAtLeast(literals, card);
            }
        } else
            throw new ParseFormatException();
    }

}

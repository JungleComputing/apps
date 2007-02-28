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
package org.sat4j.tools;

import java.io.Serializable;

import org.sat4j.specs.ContradictionException;

/**
 * Converts Dimacs problems in array format (without the terminating 0) to
 * Dimacs Strings.
 * 
 * Adaptation of org.sat4j.reader.DimacsReader.
 * 
 * @author dlb
 * @author or
 * @author fuhs
 */
public class DimacsArrayToDimacsConverter implements Serializable {

    private static final long serialVersionUID = 1L;

    // counter for the number of clauses that occur in the SAT instance
    protected int clauses;

    protected StringBuilder dimacs; // stores the dimacs string while under
                                    // construction

    private final int bufSize; // initial capacity of dimacs

    public DimacsArrayToDimacsConverter(int bufSize) {
        this.bufSize = bufSize;
    }

    protected boolean handleConstr(int gateType, int output, int[] inputs)
            throws ContradictionException {
        for (int var : inputs) {
            this.dimacs.append(var);
            this.dimacs.append(" ");
        }
        this.dimacs.append("0\n");
        ++this.clauses;
        return true;
    }

    /**
     * @param gateType
     *            gateType[i] is the type of gate i according to the Extended
     *            Dimacs specs; ignored in DimacsArrayReader, but important for
     *            inheriting classes
     * @param outputs
     *            outputs[i] is the number of the output; ignored in
     *            DimacsArrayReader
     * @param inputs
     *            inputs[i] contains the clauses in DimacsArrayReader; an
     *            overriding class might have it contain the inputs of the
     *            current gate
     * @param maxVar
     *            the maximum number of assigned ids
     * @throws ContradictionException
     *             si le probleme est trivialement inconsitant
     */
    public String parseInstance(int[] gateType, int[] outputs, int[][] inputs,
            int maxVar) throws ContradictionException {
        init();
        this.dimacs.append(maxVar);
        this.dimacs.append(" ");

        // the first character to be replaced is saved
        // (7 = "p cnf ".length() + " ".length())
        int firstCharPos = 7 + Integer.toString(maxVar).length();

        this.dimacs.append("                    ");
        // 20 blanks; if the number of clauses ever exceeds 10^21-1, this needs
        // to be altered. But that would require BigIntegers anyway.

        this.dimacs.append("\n");
        for (int i = 0; i < outputs.length; ++i) {
            handleConstr(gateType[i], outputs[i], inputs[i]);
        }
        String numClauses = Integer.toString(this.clauses);
        int numClausesLength = numClauses.length();
        for (int i = 0; i < numClausesLength; ++i) {
            this.dimacs.setCharAt(firstCharPos + i, numClauses.charAt(i));
        }
        String result = this.dimacs.toString();
        this.dimacs = null; // let the garbage collector at it
        return result;
    }

    protected void init() {
        this.dimacs = new StringBuilder(this.bufSize);
        this.dimacs.append("p cnf ");
        this.clauses = 0;
    }

    public String decode(int[] model) {
        StringBuilder stb = new StringBuilder();
        for (int i = 0; i < model.length; i++) {
            stb.append(model[i]);
            stb.append(" ");
        }
        stb.append("0");
        return stb.toString();
    }
}

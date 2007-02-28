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
import java.io.PrintWriter;
import java.util.Scanner;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

public class AAGReader extends Reader {

    private final static int FALSE = 0;

    private final static int TRUE = 1;

    private final ISolver solver;

    private int maxvarid;

    private int nbinputs;

    AAGReader(ISolver s) {
        solver = s;
    }

    @Override
    public String decode(int[] model) {
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < nbinputs; i++) {
            stb.append(model[i]>0?1:0);
        }
        return stb.toString();
    }

    @Override
    public void decode(int[] model, PrintWriter out) {
        for (int i = 0; i < nbinputs; i++) {
            out.print(model[i]>0?1:0);
        }
    }

    @Override
    public IProblem parseInstance(java.io.Reader in)
            throws ParseFormatException, ContradictionException, IOException {
        Scanner scanner = new Scanner(in);
        String prefix = scanner.next();
        if (!"aag".equals(prefix)) {
            throw new ParseFormatException("AAG format only!");
        }
        maxvarid = scanner.nextInt();
        nbinputs = scanner.nextInt();
        int nblatches = scanner.nextInt();
        int nboutputs = scanner.nextInt();
        if (nboutputs > 1) {
            throw new ParseFormatException(
                    "CNF conversion allowed for single output circuit only!");
        }
        int nbands = scanner.nextInt();
        solver.newVar(maxvarid + 1);
        solver.setExpectedNumberOfClauses(3 * nbands + 2);
        readInput(nbinputs, scanner);
        readLatches(nblatches, scanner);
        if (nboutputs > 0) {
            int output0 = readOutput(nboutputs, scanner);
            readAnd(nbands, output0, scanner);
        }
        readInputSymbols(scanner);
        skipComments(scanner);
        return solver;
    }

    private void skipComments(Scanner scanner) {
        // TODO Auto-generated method stub

    }

    private void readInputSymbols(Scanner scanner) {
        // TODO Auto-generated method stub

    }

    private void readAnd(int nbands, int output0, Scanner scanner)
            throws ContradictionException {
        IVecInt clause = new VecInt();
        for (int i = 0; i < nbands; i++) {
            int lhs = scanner.nextInt();
            int rhs0 = scanner.nextInt();
            int rhs1 = scanner.nextInt();
            clause.push(-toDimacs(lhs));
            clause.push(toDimacs(rhs0));
            solver.addClause(clause);
            clause.clear();
            clause.push(-toDimacs(lhs));
            clause.push(toDimacs(rhs1));
            solver.addClause(clause);
            clause.clear();
            clause.push(toDimacs(lhs));
            clause.push(-toDimacs(rhs0));
            clause.push(-toDimacs(rhs1));
            solver.addClause(clause);
            clause.clear();
        }
        clause.push(maxvarid + 1);
        solver.addClause(clause);
        clause.clear();
        clause.push(toDimacs(output0));
        solver.addClause(clause);
    }

    private int toDimacs(int v) {
        if (v == FALSE) {
            return -(maxvarid + 1);
        }
        if (v == TRUE) {
            return maxvarid + 1;
        }
        int var = v >> 1;
        if ((v & 1) == 0) {
            return var;
        }
        return -var;
    }

    private int readOutput(int nboutputs, Scanner scanner) {
        IVecInt outputs = new VecInt(nboutputs);
        for (int i = 0; i < nboutputs; i++) {
            outputs.push(scanner.nextInt());
        }
        return outputs.get(0);
    }

    private void readLatches(int nblatches, Scanner scanner) {
        // TODO Auto-generated method stub

    }

    private IVecInt readInput(int nbinputs, Scanner scanner) {
        IVecInt inputs = new VecInt(nbinputs);
        for (int i = 0; i < nbinputs; i++) {
            inputs.push(scanner.nextInt());
        }
        return inputs;
    }

}

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
import java.io.InputStream;
import java.io.PrintWriter;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

public class AIGReader extends Reader {

    private final static int FALSE = 0;

    private final static int TRUE = 1;

    private final ISolver solver;

    private int maxvarid;

    private int nbinputs;

    AIGReader(ISolver s) {
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

    int parseInt(InputStream in, char expected) throws IOException,
            ParseFormatException {
        int res, ch;
        ch = in.read();

        if (ch < '0' || ch > '9')
            throw new ParseFormatException("expected digit");
        res = ch - '0';

        while ((ch = in.read()) >= '0' && ch <= '9')
            res = 10 * res + (ch - '0');

        if (ch != expected)
            throw new ParseFormatException("unexpected character");

        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.reader.Reader#parseInstance(java.io.InputStream)
     */
    @Override
    public IProblem parseInstance(InputStream in) throws ParseFormatException,
            ContradictionException, IOException {
        if (in.read() != 'a' || in.read() != 'i' || in.read() != 'g'
                || in.read() != ' ') {
            throw new ParseFormatException("AIG format only!");
        }
        maxvarid = parseInt(in, ' ');
        nbinputs = parseInt(in, ' ');
        int nblatches = parseInt(in, ' ');
        if (nblatches > 0) {
            throw new ParseFormatException(
                    "CNF conversion cannot handle latches!");
        }
        int nboutputs = parseInt(in, ' ');
        if (nboutputs > 1) {
            throw new ParseFormatException(
                    "CNF conversion allowed for single output circuit only!");
        }
        int nbands = parseInt(in, '\n');
        solver.newVar(maxvarid + 1);
        solver.setExpectedNumberOfClauses(3 * nbands + 2);
        if (nboutputs > 0) {
            assert nboutputs == 1;
            int output0 = parseInt(in, '\n');
            readAnd(nbands, output0, in, 2 * (nbinputs + 1));
        }
        readInputSymbols(in);
        skipComments(in);
        return solver;
    }

    private void skipComments(InputStream in) {
        // TODO Auto-generated method stub

    }

    private void readInputSymbols(InputStream in) {
        // TODO Auto-generated method stub

    }

    static int safeGet(InputStream in) throws IOException, ParseFormatException {
        int ch = in.read();
        if (ch == -1) {
            throw new ParseFormatException("AIG Error, EOF met too early");
        }
        return ch;
    }

    static int decode(InputStream in) throws IOException, ParseFormatException {
        int x = 0, i = 0;
        int ch;

        while (((ch = safeGet(in)) & 0x80) > 0) {
            System.out.println("=>" + ch);
            x |= (ch & 0x7f) << (7 * i++);
        }
        return x | (ch << (7 * i));
    }

    private void readAnd(int nbands, int output0, InputStream in, int startid)
            throws ContradictionException, IOException, ParseFormatException {
        IVecInt clause = new VecInt();
        int lhs = startid;
        for (int i = 0; i < nbands; i++) {
            int delta0 = decode(in);
            int delta1 = decode(in);
            int rhs0 = lhs - delta0;
            int rhs1 = rhs0 - delta1;
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
            lhs += 2;
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

    @Override
    public IProblem parseInstance(java.io.Reader in)
            throws ParseFormatException, ContradictionException, IOException {
        throw new UnsupportedOperationException();
    }

}

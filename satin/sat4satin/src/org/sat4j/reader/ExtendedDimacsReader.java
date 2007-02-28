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
import java.util.Scanner;
import java.util.StringTokenizer;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 * Reader for the Extended Dimacs format proposed by Fahiem Bacchus and Toby
 * Walsh.
 * 
 * @author leberre
 * 
 */
public class ExtendedDimacsReader extends DimacsReader {

    public static final int FALSE = 1;

    public static final int TRUE = 2;

    public static final int NOT = 3;

    public static final int AND = 4;

    public static final int NAND = 5;

    public static final int OR = 6;

    public static final int NOR = 7;

    public static final int XOR = 8;

    public static final int XNOR = 9;

    public static final int IMPLIES = 10;

    public static final int IFF = 11;

    public static final int IFTHENELSE = 12;

    public static final int ATLEAST = 13;

    public static final int ATMOST = 14;

    public static final int COUNT = 15;

    /**
     * 
     * 
     */
    private static final long serialVersionUID = 1L;

    public ExtendedDimacsReader(ISolver solver) {
        super(solver);
    }

    /**
     * @param in
     *            the input stream
     * @throws IOException
     *             iff an IO occurs
     * @throws ParseFormatException
     *             if the input stream does not comply with the DIMACS format.
     */
    @Override
    protected void readProblemLine(LineNumberReader in) throws IOException,
            ParseFormatException {

        String line = in.readLine();

        if (line == null) {
            throw new ParseFormatException(
                    "premature end of file: <p noncnf ...> expected  on line "
                            + in.getLineNumber());
        }
        StringTokenizer stk = new StringTokenizer(line);

        if (!(stk.hasMoreTokens() && stk.nextToken().equals("p")
                && stk.hasMoreTokens() && stk.nextToken().equals("noncnf"))) {
            throw new ParseFormatException(
                    "problem line expected (p noncnf ...) on line "
                            + in.getLineNumber());
        }

        int vars;

        // reads the max var id
        vars = Integer.parseInt(stk.nextToken());
        assert vars > 0;
        solver.newVar(vars);
        try {
            processClause(new VecInt().push(vars));
        } catch (ContradictionException e) {
            assert false;
            System.err.println("Contradiction when asserting root variable?");
        }
        disableNumberOfConstraintCheck();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.reader.DimacsReader#handleConstr(java.lang.String,
     *      org.sat4j.specs.IVecInt)
     */
    @Override
    protected boolean handleConstr(String line, IVecInt literals)
            throws ContradictionException {
        boolean added = true;
        assert literals.size() == 0;
        Scanner scan = new Scanner(line);
        while (scan.hasNext()) {
            int gateType = scan.nextInt();
            assert gateType > 0;
            int nbparam = scan.nextInt();
            assert nbparam != 0;
            assert nbparam == -1 || gateType >= ATLEAST;
            int k = -1;
            for (int i = 0; i < nbparam; i++) {
                k = scan.nextInt();
            }
            // readI/O until reaching ending 0
            int y = scan.nextInt();
            int x;
            while ((x = scan.nextInt()) != 0) {
                literals.push(x);
            }
            assert literals.size() == k;
            switch (gateType) {
            case FALSE:
                gateFalse(y, literals);
                break;
            case TRUE:
                gateTrue(y, literals);
                break;
            case OR:
                or(y, literals);
                break;
            case NOT:
                not(y, literals);
                break;
            case AND:
                and(y, literals);
                break;
            case XOR:
                xor(y, literals);
                break;
            case IFF:
                iff(y, literals);
                break;
            case IFTHENELSE:
                ite(y, literals);
                break;
            default:
                throw new UnsupportedOperationException("Gate type " + gateType
                        + " not handled yet");
            }
        }
        literals.clear();
        return added;
    }

    private void gateFalse(int y, IVecInt literals)
            throws ContradictionException {
        assert literals.size() == 0;
        IVecInt clause = new VecInt(1);
        clause.push(-y);
        processClause(clause);
    }

    private void gateTrue(int y, IVecInt literals)
            throws ContradictionException {
        assert literals.size() == 0;
        IVecInt clause = new VecInt(1);
        clause.push(y);
        processClause(clause);
    }

    private void ite(int y, IVecInt literals) throws ContradictionException {
        assert literals.size() == 3;
        IVecInt clause = new VecInt(2);
        // y <=> (x1 -> x2) and (not x1 -> x3)
        // y -> (x1 -> x2) and (not x1 -> x3)
        clause.push(-y).push(-literals.get(0)).push(literals.get(1));
        processClause(clause);
        clause.clear();
        clause.push(-y).push(literals.get(0)).push(literals.get(2));
        processClause(clause);
        // y <- (x1 -> x2) and (not x1 -> x3)
        // not(x1 -> x2) or not(not x1 -> x3) or y
        // x1 and not x2 or not x1 and not x3 or y
        // (x1 and not x2) or ((not x1 or y) and (not x3 or y))
        // (x1 or not x1 or y) and (not x2 or not x1 or y) and (x1 or not x3 or
        // y) and (not x2 or not x3 or y)
        // not x1 or not x2 or y and x1 or not x3 or y and not x2 or not x3 or y
        clause.clear();
        clause.push(-literals.get(0)).push(-literals.get(1)).push(y);
        processClause(clause);
        clause.clear();
        clause.push(literals.get(0)).push(-literals.get(2)).push(y);
        processClause(clause);
        clause.clear();
        clause.push(-literals.get(1)).push(-literals.get(2)).push(y);
        processClause(clause);
    }

    private void and(int y, IVecInt literals) throws ContradictionException {
        // y <=> AND x1 ... xn

        // y <= x1 .. xn
        IVecInt clause = new VecInt(literals.size() + 1);
        clause.push(y);
        for (int i = 0; i < literals.size(); i++) {
            clause.push(-literals.get(i));
        }
        processClause(clause);
        clause.clear();
        for (int i = 0; i < literals.size(); i++) {
            // y => xi
            clause.clear();
            clause.push(-y);
            clause.push(literals.get(i));
            processClause(clause);
        }
    }

    private void or(int y, IVecInt literals) throws ContradictionException {
        // y <=> OR x1 x2 ...xn
        // y => x1 x2 ... xn
        IVecInt clause = new VecInt(literals.size() + 1);
        literals.copyTo(clause);
        clause.push(-y);
        processClause(clause);
        clause.clear();
        for (int i = 0; i < literals.size(); i++) {
            // xi => y
            clause.clear();
            clause.push(y);
            clause.push(-literals.get(i));
            processClause(clause);
        }
    }

    private void processClause(IVecInt clause) throws ContradictionException {
        solver.addClause(clause);
    }

    private void not(int y, IVecInt literals) throws ContradictionException {
        assert literals.size() == 1;
        IVecInt clause = new VecInt(2);
        // y <=> not x
        // y => not x = not y or not x
        clause.push(-y).push(-literals.get(0));
        processClause(clause);
        // y <= not x = y or x
        clause.clear();
        clause.push(y).push(literals.get(0));
        processClause(clause);
    }

    void xor(int y, IVecInt literals) throws ContradictionException {
        literals.push(-y);
        int[] f = new int[literals.size()];
        literals.copyTo(f);
        xor2Clause(f, 0, false);
    }

    void iff(int y, IVecInt literals) throws ContradictionException {
        literals.push(y);
        int[] f = new int[literals.size()];
        literals.copyTo(f);
        iff2Clause(f, 0, false);
    }

    void xor2Clause(int[] f, int prefix, boolean negation)
            throws ContradictionException {
        if (prefix == f.length - 1) {
            IVecInt clause = new VecInt(f.length);
            for (int i = 0; i < f.length - 1; ++i) {
                clause.push(f[i]);
            }
            clause.push(f[f.length - 1] * (negation ? -1 : 1));
            processClause(clause);
            return;
        }

        if (negation) {
            f[prefix] = -f[prefix];
            xor2Clause(f, prefix + 1, false);
            f[prefix] = -f[prefix];

            xor2Clause(f, prefix + 1, true);
        } else {
            xor2Clause(f, prefix + 1, false);

            f[prefix] = -f[prefix];
            xor2Clause(f, prefix + 1, true);
            f[prefix] = -f[prefix];
        }
    }

    void iff2Clause(int[] f, int prefix, boolean negation)
            throws ContradictionException {
        if (prefix == f.length - 1) {
            IVecInt clause = new VecInt(f.length);
            for (int i = 0; i < f.length - 1; ++i) {
                clause.push(f[i]);
            }
            clause.push(f[f.length - 1] * (negation ? -1 : 1));
            processClause(clause);
            return;
        }

        if (negation) {
            iff2Clause(f, prefix + 1, false);
            f[prefix] = -f[prefix];
            iff2Clause(f, prefix + 1, true);
            f[prefix] = -f[prefix];
        } else {
            f[prefix] = -f[prefix];
            iff2Clause(f, prefix + 1, false);
            f[prefix] = -f[prefix];
            iff2Clause(f, prefix + 1, true);
        }
    }

}

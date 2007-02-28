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

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

/**
 * Converter from the Extended Dimacs format proposed by Fahiem Bacchus and Toby
 * Walsh in array representation (without the terminating 0) to the Dimacs
 * format.
 * 
 * Adaptation of org.sat4j.reader.ExtendedDimacsReader.
 * 
 * @author leberre
 * @author fuhs
 */
public class ExtendedDimacsArrayToDimacsConverter extends
        DimacsArrayToDimacsConverter {

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

    private static final long serialVersionUID = 1L;

    public ExtendedDimacsArrayToDimacsConverter(int bufSize) {
        super(bufSize);
    }

    /**
     * Handles a single constraint (constraint == Extended Dimacs circuit gate).
     * 
     * @param gateType
     *            the type of the gate in question
     * @param output
     *            the number of the output of the gate in question
     * @param inputs
     *            the numbers of the inputs of the gates in question
     * @return true
     */
    @Override
    protected boolean handleConstr(int gateType, int output, int[] inputs)
            throws ContradictionException {
        IVecInt literals = new VecInt(inputs);
        switch (gateType) {
        case FALSE:
            gateFalse(output, literals);
            break;
        case TRUE:
            gateTrue(output, literals);
            break;
        case OR:
            or(output, literals);
            break;
        case NOT:
            not(output, literals);
            break;
        case AND:
            and(output, literals);
            break;
        case XOR:
            xor(output, literals);
            break;
        case IFF:
            iff(output, literals);
            break;
        case IFTHENELSE:
            ite(output, literals);
            break;
        default:
            throw new UnsupportedOperationException("Gate type " + gateType
                    + " not handled yet");
        }
        return true;
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
        final int length = clause.size();
        for (int i = 0; i < length; ++i) {
            this.dimacs.append(clause.get(i));
            this.dimacs.append(" ");
        }
        this.dimacs.append("0\n");
        ++this.clauses;
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

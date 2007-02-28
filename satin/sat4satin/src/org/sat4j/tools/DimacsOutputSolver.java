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

import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Map;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * Solver used to display in a writer the CNF instance in Dimacs format.
 * 
 * That solver is useful to produce CNF files to be used by third party solvers.
 * 
 * @author leberre
 * 
 */
public class DimacsOutputSolver implements ISolver {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private transient PrintWriter out;

    private int nbvars;

    private int nbclauses;

    public DimacsOutputSolver() {
        this(new PrintWriter(System.out, true));
    }

    public DimacsOutputSolver(PrintWriter pw) {
        out = pw;
    }

    private void readObject(ObjectInputStream stream) {
        out = new PrintWriter(System.out, true);
    }
    
    public int newVar() {
        return 0;
    }

    public int newVar(int howmany) {
        out.print("p cnf " + howmany);
        nbvars = howmany;
        return 0;
    }

    public void setExpectedNumberOfClauses(int nb) {
        out.println(" " + nb);
        nbclauses = nb;
    }

    public IConstr addClause(IVecInt literals) throws ContradictionException {
        for (int i : literals)
            out.print(i + " ");
        out.println("0");
        return null;
    }

    public boolean removeConstr(IConstr c) {
        throw new UnsupportedOperationException();
    }

    public void addAllClauses(IVec<IVecInt> clauses)
            throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    public IConstr addAtMost(IVecInt literals, int degree)
            throws ContradictionException {
        out.println(literals.toString() + " <= " + degree);
        return null;
    }

    public IConstr addAtLeast(IVecInt literals, int degree)
            throws ContradictionException {
        out.println(literals.toString() + " >= " + degree);
        return null;
    }

    public IConstr addPseudoBoolean(IVecInt lits, IVec<BigInteger> coeffs,
            boolean moreThan, BigInteger d) throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    public void setTimeout(int t) {
        // TODO Auto-generated method stub

    }

    public void setTimeoutMs(long t) {
        // TODO Auto-generated method stub
    }
    
    public int getTimeout() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void reset() {
        // TODO Auto-generated method stub

    }

    public void printStat(PrintStream out, String prefix) {
        // TODO Auto-generated method stub

    }

    public void printStat(PrintWriter out, String prefix) {
        // TODO Auto-generated method stub

    }

    public Map<String, Number> getStat() {
        // TODO Auto-generated method stub
        return null;
    }

    public String toString(String prefix) {
        return "Dimacs output solver";
    }

    public void clearLearntClauses() {
        // TODO Auto-generated method stub

    }

    public int[] model() {
        throw new UnsupportedOperationException();
    }

    public boolean model(int var) {
        throw new UnsupportedOperationException();
    }

    public boolean isSatisfiable() throws TimeoutException {
        throw new TimeoutException("There is no real solver behind!");
    }

    public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
        throw new TimeoutException("There is no real solver behind!");
    }

    public int[] findModel() throws TimeoutException {
        throw new UnsupportedOperationException();
    }

    public int[] findModel(IVecInt assumps) throws TimeoutException {
        throw new UnsupportedOperationException();
    }

    public int nConstraints() {
        return nbclauses;
    }

    public int nVars() {
        // TODO Auto-generated method stub
        return nbvars;
    }

}

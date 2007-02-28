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

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Map;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * The aim of that class is to allow adding dynamic responsabilities to SAT
 * solvers using the Decorator design pattern. The class is abstract because it
 * does not makes sense to use it "as is".
 * 
 * @author leberre
 */
public abstract class SolverDecorator implements ISolver, Serializable {

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.specs.ISolver#clearLearntClauses()
     */
    public void clearLearntClauses() {
        solver.clearLearntClauses();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.specs.IProblem#findModel()
     */
    public int[] findModel() throws TimeoutException {
        return solver.findModel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.specs.IProblem#findModel(org.sat4j.specs.IVecInt)
     */
    public int[] findModel(IVecInt assumps) throws TimeoutException {
        return solver.findModel(assumps);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.specs.IProblem#model(int)
     */
    public boolean model(int var) {
        return solver.model(var);
    }

    public void setExpectedNumberOfClauses(int nb) {
        solver.setExpectedNumberOfClauses(nb);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.specs.ISolver#getTimeout()
     */
    public int getTimeout() {
        return solver.getTimeout();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.specs.ISolver#toString(java.lang.String)
     */
    public String toString(String prefix) {
        return solver.toString(prefix);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.specs.ISolver#printStat(java.io.PrintStream,
     *      java.lang.String)
     */
    @Deprecated
    public void printStat(PrintStream out, String prefix) {
        solver.printStat(out, prefix);
    }

    public void printStat(PrintWriter out, String prefix) {
        solver.printStat(out, prefix);
    }

    private final ISolver solver;

    /**
     * 
     */
    public SolverDecorator(ISolver solver) {
        this.solver = solver;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#newVar()
     */
    public int newVar() {
        return solver.newVar();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#newVar(int)
     */
    public int newVar(int howmany) {
        return solver.newVar(howmany);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#addClause(org.sat4j.datatype.VecInt)
     */
    public IConstr addClause(IVecInt literals) throws ContradictionException {
        return solver.addClause(literals);
    }

    public void addAllClauses(IVec<IVecInt> clauses)
            throws ContradictionException {
        solver.addAllClauses(clauses);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#addAtMost(org.sat4j.datatype.VecInt, int)
     */
    public IConstr addAtMost(IVecInt literals, int degree)
            throws ContradictionException {
        return solver.addAtMost(literals, degree);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#addAtLeast(org.sat4j.datatype.VecInt, int)
     */
    public IConstr addAtLeast(IVecInt literals, int degree)
            throws ContradictionException {
        return solver.addAtLeast(literals, degree);
    }

    public IConstr addPseudoBoolean(IVecInt literals, IVec<BigInteger> coeffs,
            boolean moreThan, BigInteger degree) throws ContradictionException {
        return solver.addPseudoBoolean(literals, coeffs, moreThan, degree);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#model()
     */
    public int[] model() {
        return solver.model();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#isSatisfiable()
     */
    public boolean isSatisfiable() throws TimeoutException {
        return solver.isSatisfiable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#isSatisfiable(org.sat4j.datatype.VecInt)
     */
    public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
        return solver.isSatisfiable(assumps);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#setTimeout(int)
     */
    public void setTimeout(int t) {
        solver.setTimeout(t);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#setTimeoutMs(int)
     */
    public void setTimeoutMs(long t) {
        solver.setTimeoutMs(t);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#nConstraints()
     */
    public int nConstraints() {
        return solver.nConstraints();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#nVars()
     */
    public int nVars() {
        return solver.nVars();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#reset()
     */
    public void reset() {
        solver.reset();
    }

    public ISolver decorated() {
        return solver;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.specs.ISolver#removeConstr(org.sat4j.minisat.core.Constr)
     */
    public boolean removeConstr(IConstr c) {
        return solver.removeConstr(c);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.specs.ISolver#getStat()
     */
    public Map<String, Number> getStat() {
        return solver.getStat();
    }
}

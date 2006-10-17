/*
 * Created on 22 mars 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sat4j.tools;

import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigInteger;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * The aim of that class is to allow adding dynamic
 * responsabilities to SAT solvers using the Decorator design pattern.
 * The class is abstract because it does not makes sense to use it "as
 * is".
 *         
 * @author leberre 
 */
public abstract class SolverDecorator implements ISolver, Serializable {

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
    public void printStat(PrintStream out, String prefix) {
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
}

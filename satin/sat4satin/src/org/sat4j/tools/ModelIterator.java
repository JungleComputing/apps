/*
 * Created on 22 mars 2004
 * 
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sat4j.tools;

import java.io.Serializable;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * That class allows to iterate through all the models
 * (implicants) of a formula.
 * 
 * <pre>
 * ISolver solver = new ModelIterator(SolverFactory.OneSolver());
 * boolean unsat = true;
 * while (solver.isSatisfiable()) {
 *     unsat = false;
 *     int[] model = solver.model();
 *     // do something with model
 * }
 * if (unsat) {
 *     // UNSAT case
 * }
 * </pre>
 * 
 * @author leberre 
 */
public class ModelIterator extends SolverDecorator implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean trivialfalsity = false;

    /**
     * @param solver
     */
    public ModelIterator(ISolver solver) {
        super(solver);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#model()
     */
    @Override
    public int[] model() {
        int[] last = super.model();
        IVecInt clause = new VecInt(last.length);
        for (int i = 0; i < last.length; i++) {
            clause.push(-last[i]);
        }
        try {
            // System.out.println("adding " + clause);
            addClause(clause);
        } catch (ContradictionException e) {
            trivialfalsity = true;
        }
        return last;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#isSatisfiable()
     */
    @Override
    public boolean isSatisfiable() throws TimeoutException {
        if (trivialfalsity) {
            return false;
        }
        trivialfalsity = false;
        return super.isSatisfiable();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#isSatisfiable(org.sat4j.datatype.VecInt)
     */
    @Override
    public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
        if (trivialfalsity) {
            return false;
        }
        trivialfalsity = false;
        return super.isSatisfiable(assumps);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#reset()
     */
    @Override
    public void reset() {
        trivialfalsity = false;
        super.reset();
    }
}

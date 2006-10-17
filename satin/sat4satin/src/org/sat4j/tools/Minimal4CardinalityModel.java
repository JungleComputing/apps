/*
 * Created on 31 mars 2004
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
 * Computes models with a minimal number (with respect to
 * cardinality) of negative literals. This is done be adding a
 * constraint on the number of negative literals each time a model if
 * found (the number of negative literals occuring in the model minus
 * one).
 *         
 * @author leberre 
 * @see org.sat4j.specs.ISolver#addAtMost(IVecInt, int)
 */
public class Minimal4CardinalityModel extends SolverDecorator implements
        Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * @param solver
     */
    public Minimal4CardinalityModel(ISolver solver) {
        super(solver);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.ISolver#model()
     */
    @Override
    public int[] model() {
        int[] prevmodel = null;
        IVecInt vec = new VecInt();
        // backUp();
        try {
            do {
                prevmodel = super.model();
                vec.clear();
                for (int i = 1; i <= nVars(); i++) {
                    vec.push(-i);
                }
                int counter = 0;
                for (int i = 0; i < prevmodel.length; i++) {
                    if (prevmodel[i] < 0) {
                        counter++;
                    }
                }
                addAtMost(vec, counter - 1);
                System.err.println(counter);
            } while (isSatisfiable());
        } catch (TimeoutException e) {
            System.err.println("Solver timed out");
        } catch (ContradictionException e) {
            System.err.println("added trivial unsat clauses?" + vec);
        }
        // restore();
        return prevmodel;
    }
}

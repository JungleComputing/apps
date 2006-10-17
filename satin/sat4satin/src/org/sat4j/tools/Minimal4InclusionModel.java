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
 * Computes models with a minimal subset (with respect to set
 * inclusion) of negative literals. This is done be adding a clause
 * containing the negation of the negative literals appearing in the
 * model found (which prevents any interpretation containing that subset
 * of negative literals to be a model of the formula).
 * 
 * Computes only one model minimal for inclusion, since there is currently no
 * way to save the state of the solver.
 * 
 * @author leberre 
 * 
 * @see org.sat4j.specs.ISolver#addClause(IVecInt)
 */
public class Minimal4InclusionModel extends SolverDecorator implements
        Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * @param solver
     */
    public Minimal4InclusionModel(ISolver solver) {
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
        IVecInt cube = new VecInt();
        // backUp();
        try {
            do {
                prevmodel = super.model();
                vec.clear();
                cube.clear();
                for (int i = 0; i < prevmodel.length; i++) {
                    if (prevmodel[i] < 0) {
                        vec.push(-prevmodel[i]);
                    } else {
                        cube.push(prevmodel[i]);
                    }
                }
                // System.out.println("minimizing " + vec + "/" + cube);
                addClause(vec);
            } while (isSatisfiable(cube));
        } catch (TimeoutException e) {
            // System.err.println("Solver timed out");
        } catch (ContradictionException e) {
            // System.out.println("added trivial unsat clauses?" + vec);
        }
        // restore();
        int[] newmodel = new int[vec.size()];
        for (int i = 0, j = 0; i < prevmodel.length; i++) {
            if (prevmodel[i] < 0) {
                newmodel[j++] = prevmodel[i];
            }
        }

        return newmodel;
    }
}
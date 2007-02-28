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

import java.io.Serializable;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * Computes models with a minimal subset (with respect to set inclusion) of
 * negative literals. This is done be adding a clause containing the negation of
 * the negative literals appearing in the model found (which prevents any
 * interpretation containing that subset of negative literals to be a model of
 * the formula).
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
                for (int q : prevmodel) {
                    if (q < 0) {
                        vec.push(-q);
                    } else {
                        cube.push(q);
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
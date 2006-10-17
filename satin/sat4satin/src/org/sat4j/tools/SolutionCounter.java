package org.sat4j.tools;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * Another solver decorator that counts the number of solutions.
 * 
 * Note that this approach is quite naive so do not expect it to work on large examples.
 * 
 * @author leberre
 *
 */
public class SolutionCounter extends SolverDecorator {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SolutionCounter(ISolver solver) {
        super(solver);
    }

    public long countSolutions() throws TimeoutException {
        long nbsols = 0;
        boolean trivialfalsity = false;

        while (!trivialfalsity && isSatisfiable()) {
            nbsols++;
            int[] last = model();
            IVecInt clause = new VecInt(last.length);
            for (int i = 0; i < last.length; i++) {
                clause.push(-last[i]);
            }
            try {
                // System.out.println("Sol number "+nbsols+" adding " + clause);
                addClause(clause);
            } catch (ContradictionException e) {
                trivialfalsity = true;
            }
        }
        return nbsols;
    }
}

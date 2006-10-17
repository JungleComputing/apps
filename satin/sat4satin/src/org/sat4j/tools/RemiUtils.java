package org.sat4j.tools;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

/**
 * Class dedicated to Remi Coletta utility methods :-)
 * 
 * @author leberre
 * 
 */
public class RemiUtils {

    /**
     * Compute the set of literals common to all models of the formula.
     * 
     * @param s
     *            a solver already feeded
     * @return the set of literals common to all models of the formula contained
     *         in the solver, in dimacs format.
     * @throws TimeoutException
     */
    public static IVecInt backbone(ISolver s) throws TimeoutException {
        IVecInt backbone = new VecInt();
        int nvars = s.nVars();
        for (int i = 1; i <= nvars; i++) {
            backbone.push(i);
            if (!s.isSatisfiable(backbone)) {
                // -i is in the backbone
                backbone.pop().push(-i);
            } else {
                backbone.pop().push(-i);
                if (!s.isSatisfiable(backbone)) {
                    // -i is in the backbone
                    backbone.pop().push(i);
                } else {
                    backbone.pop();
                }
            }
        }
        return backbone;
    }

}

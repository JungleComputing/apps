/*
 * Created on 16 avr. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.constraints;

import org.sat4j.minisat.constraints.card.AtLeast;
import org.sat4j.minisat.core.Constr;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MixedDataStructureDaniel extends ClausalDataStructureWL {

    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.DataStructureFactory#createCardinalityConstraint(org.sat4j.datatype.VecInt,
     *      int)
     */
    @Override
    public Constr createCardinalityConstraint(IVecInt literals, int degree)
        throws ContradictionException {
        return AtLeast.atLeastNew(solver, getVocabulary(), literals, degree);
    }

    @Override
    public Object clone() {
        MixedDataStructureDaniel clone;

        clone = (MixedDataStructureDaniel) super.clone();
        if (false) {
            System.out.println("MDSD " + this + " clone " + clone);
        }

        return clone;
    }

}

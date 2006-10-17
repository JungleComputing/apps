/*
 * Created on 16 avr. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.constraints;

import org.sat4j.minisat.constraints.card.MaxWatchCard;
import org.sat4j.minisat.constraints.card.MinWatchCard;
import org.sat4j.minisat.constraints.cnf.WLClause;
import org.sat4j.minisat.core.Constr;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CardinalityDataStructureYanMax extends
        AbstractCardinalityDataStructure {

    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.DataStructureFactory#createClause(org.sat4j.datatype.VecInt)
     */
    public Constr createClause(IVecInt literals) throws ContradictionException {
        return MaxWatchCard.maxWatchCardNew(solver, getVocabulary(), literals,
            MinWatchCard.ATLEAST, 1);
    }

    public Constr createUnregisteredClause(IVecInt literals) {
        return new WLClause(literals, getVocabulary());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.DataStructureFactory#createCardinalityConstraint(org.sat4j.datatype.VecInt,
     *      int)
     */
    @Override
    public Constr createCardinalityConstraint(IVecInt literals, int degree)
        throws ContradictionException {
        return MaxWatchCard.maxWatchCardNew(solver, getVocabulary(), literals,
            MinWatchCard.ATLEAST, degree);
    }

}

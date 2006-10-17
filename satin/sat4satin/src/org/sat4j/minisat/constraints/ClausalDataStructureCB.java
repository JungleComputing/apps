/*
 * Created on Jul 5, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.constraints;

import org.sat4j.minisat.constraints.cnf.CBClause;
import org.sat4j.minisat.constraints.cnf.WLClause;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.Propagatable;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author parrain To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ClausalDataStructureCB extends AbstractDataStructureFactory {

    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.DataStructureFactory#conflictDetectedInWatchesFor(int,
     *      int)
     */
    @Override
    public void conflictDetectedInWatchesFor(int p, int i) {
        // to nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.DataStructureFactory#getWatchesFor(int)
     */
    @Override
    public IVec<Propagatable> getWatchesFor(int p) {
        return getVocabulary().watches(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.DataStructureFactory#createClause(org.sat4j.specs.VecInt)
     */
    public Constr createClause(IVecInt literals) throws ContradictionException {
        IVecInt v = WLClause.sanityCheck(literals, getVocabulary(), solver);
        if (v == null) {
            return null;
        }
        return CBClause.brandNewClause(solver, getVocabulary(), v);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.DataStructureFactory#createUnregisteredClause(org.sat4j.specs.VecInt)
     */
    public Constr createUnregisteredClause(IVecInt literals) {
        return new CBClause(literals, getVocabulary());
    }

    @Override
    public Object clone() {
        ClausalDataStructureCB clone;

	clone = (ClausalDataStructureCB) super.clone();

        return clone;
    }

}

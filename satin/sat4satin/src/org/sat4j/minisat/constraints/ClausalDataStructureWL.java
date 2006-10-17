/*
 * Created on 16 avr. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.constraints;

import org.sat4j.core.Vec;
import org.sat4j.minisat.constraints.cnf.WLClause;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Propagatable;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ClausalDataStructureWL extends AbstractDataStructureFactory
    implements Cloneable {

    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.DataStructureFactory#createClause(org.sat4j.datatype.VecInt)
     */
    public Constr createClause(IVecInt literals) throws ContradictionException {
        IVecInt v = WLClause.sanityCheck(literals, getVocabulary(), solver);
        if (v == null) {
            return null;
        }
        return WLClause.brandNewClause(solver, getVocabulary(), v);
    }

    public Constr createUnregisteredClause(IVecInt literals) {
        return new WLClause(literals, getVocabulary());
    }

    @Override
    public Object clone() {
        ClausalDataStructureWL clone;

	clone = (ClausalDataStructureWL) super.clone();
	clone.lits = (ILits) clone.lits.clone();
	// need private version of the tmp array in case we are multithreading:
	clone.tmp = new Vec<Propagatable>();

	if (false) {
	    System.out.println("CDSWL " + this + " clone " + clone +
			       " lits " + this.lits + " clone " + clone.lits);
	}

	return clone;
    }
}

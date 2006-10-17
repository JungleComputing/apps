/*
 * Created on 1 juin 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sat4j.minisat.constraints;

import org.sat4j.minisat.constraints.cnf.Lits23;
import org.sat4j.minisat.constraints.cnf.WLClause;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.ILits23;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MixedDataStructureWithBinaryAndTernary extends
        MixedDataStructureDaniel {

    private static final long serialVersionUID = 1L;

    // private final ILits23 mlits = new Lits23();
    private ILits23 mlits = new Lits23();

    {
        lits = mlits;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.DataStructureFactory#createVocabulary()
     */
    @Override
    public ILits getVocabulary() {
        return lits;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.DataStructureFactory#createClause(org.sat4j.datatype.VecInt)
     */
    @Override
    public Constr createClause(IVecInt literals) throws ContradictionException {
        IVecInt v = WLClause.sanityCheck(literals, lits, solver);
        if (v == null) {
            return null;
        }
        if (v.size() == 2) {
            mlits.binaryClauses(v.get(0), v.get(1));
            return null;
        }
        if (v.size() == 3) {
            mlits.ternaryClauses(v.get(0), v.get(1), v.get(2));
            return null;
        }
        return WLClause.brandNewClause(solver, lits, v);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.DataStructureFactory#learnContraint(org.sat4j.minisat.Constr)
     */
    @Override
    public void learnConstraint(Constr constr) {
        if (constr.size() == 2) {
            mlits.binaryClauses(constr.get(0), constr.get(1));
            // solver.getStats().learnedbinaryclauses++;
        } else if (constr.size() == 3) {
            mlits.ternaryClauses(constr.get(0), constr.get(1), constr.get(2));
            // solver.getStats().learnedternaryclauses++;
        } else {
            super.learnConstraint(constr);
        }
    }

    @Override
    public Object clone() {
	MixedDataStructureWithBinaryAndTernary clone;

	clone = (MixedDataStructureWithBinaryAndTernary) super.clone();
	clone.mlits = (ILits23) clone.mlits.clone();
	clone.lits = clone.mlits;

	return clone;
    }
}

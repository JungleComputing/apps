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

package org.sat4j.minisat.constraints;

import org.sat4j.minisat.constraints.cnf.Lits2;
import org.sat4j.minisat.constraints.cnf.WLClause;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.ILits2;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MixedDataStructureWithBinary extends MixedDataStructureDaniel {

    private static final long serialVersionUID = 1L;

    private final ILits2 mlits = new Lits2();

    public MixedDataStructureWithBinary() {
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
        if (v == null)
            return null;
        if (v.size() == 2) {
            mlits.binaryClauses(v.get(0), v.get(1));
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
        } else {
            super.learnConstraint(constr);
        }
    }
}

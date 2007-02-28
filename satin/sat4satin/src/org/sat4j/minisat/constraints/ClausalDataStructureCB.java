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
        if (v == null)
            return null;
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

}

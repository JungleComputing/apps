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
package org.sat4j.minisat.constraints.cnf;

import org.sat4j.minisat.core.ILits;
import org.sat4j.specs.IVecInt;

public class DefaultWLClause extends WLClause {

    private boolean learnt;

    public DefaultWLClause(IVecInt ps, ILits voc) {
        super(ps, voc);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * declares this clause as learnt
     * 
     */
    public void setLearnt() {
        learnt = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.datatype.Constr#learnt()
     */
    public boolean learnt() {
        return learnt;
    }

    /**
     * Register this clause which means watching the necessary literals If the
     * clause is learnt, setLearnt() must be called before a call to register()
     * 
     * @see #setLearnt()
     */
    public void register() {
        assert lits.length > 1;
        if (learnt) {
            // prendre un deuxieme litt???ral ??? surveiller
            int maxi = 1;
            int maxlevel = voc.getLevel(lits[1]);
            for (int i = 2; i < lits.length; i++) {
                int level = voc.getLevel(lits[i]);
                if (level > maxlevel) {
                    maxi = i;
                    maxlevel = level;
                }
            }
            int l = lits[1];
            lits[1] = lits[maxi];
            lits[maxi] = l;
        }

        // ajoute la clause a la liste des clauses control???es.
        voc.watch(lits[0] ^ 1, this);
        voc.watch(lits[1] ^ 1, this);
    }

    // SATIN
    private boolean learntGlobal;

    public void setLearntGlobal() {
	learntGlobal = true;
    }

    public boolean learntGlobal() {
        return learntGlobal;
    }
}

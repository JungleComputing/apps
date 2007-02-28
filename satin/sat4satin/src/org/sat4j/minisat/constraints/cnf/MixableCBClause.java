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
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.IVecInt;

/**
 * Counter Based clauses that can be mixed with WLCLauses
 * 
 * @author leberre
 */
public class MixableCBClause extends CBClause {

    /**
     * Commentaire pour <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param ps
     * @param voc
     */
    public MixableCBClause(IVecInt ps, ILits voc) {
        super(ps, voc);
        // TODO Raccord de constructeur auto-généré
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Propagatable#propagate(org.sat4j.minisat.core.UnitPropagationListener,
     *      int)
     */
    @Override
    public boolean propagate(UnitPropagationListener s, int p) {
        voc.watch(p, this);
        return super.propagate(s, p);
    }

    /**
     * @param ps
     * @param voc
     * @param learnt
     */
    public MixableCBClause(IVecInt ps, ILits voc, boolean learnt) {
        super(ps, voc, learnt);
        // TODO Raccord de constructeur auto-généré
    }

    public static CBClause brandNewClause(UnitPropagationListener s, ILits voc,
            IVecInt literals) {
        CBClause c = new MixableCBClause(literals, voc);
        c.register();
        return c;
    }
}

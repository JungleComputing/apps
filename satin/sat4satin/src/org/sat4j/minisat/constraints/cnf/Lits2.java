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

import org.sat4j.minisat.core.ILits2;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Lits2 extends Lits implements ILits2 {

    private static final long serialVersionUID = 1L;

    private BinaryClauses[] binclauses = null;

    /**
     * 
     */
    public Lits2() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * To know the number of binary clauses in which the literal occurs. Please
     * note that this method should only be used in conjunction with the
     * BinaryClauses data structure.
     * 
     * @param p
     * @return the number of binary clauses in which the literal occurs.
     */
    public int nBinaryClauses(int p) {
        if (binclauses == null) {
            return 0;
        }
        if (binclauses[p] == null) {
            return 0;
        }
        return binclauses[p].size();
    }

    public void binaryClauses(int lit1, int lit2) {
        register(lit1, lit2);
        register(lit2, lit1);
    }

    private void register(int p, int q) {
        if (binclauses == null) {
            binclauses = new BinaryClauses[2 * nVars() + 2];
        }
        if (binclauses[p] == null) {
            binclauses[p] = new BinaryClauses(this, p);
            watches[p ^ 1].insertFirstWithShifting(binclauses[p]);
        }
        binclauses[p].addBinaryClause(q);
    }

}

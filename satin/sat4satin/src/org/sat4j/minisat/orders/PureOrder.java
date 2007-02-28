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

package org.sat4j.minisat.orders;

/**
 * @author leberre TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public class PureOrder extends VarOrder {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    private int period;

    private int cpt;

    public PureOrder() {
        this(20);
    }

    public PureOrder(int p) {
        setPeriod(p);
    }

    public final void setPeriod(int p) {
        period = p;
        cpt = period;
    }

    public int getPeriod() {
        return period;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.VarOrder#select()
     */
    @Override
    public int select() {
        // wait period branching
        if (cpt < period) {
            cpt++;
        } else {
            // try to find a pure literal
            cpt = 0;
            int nblits = 2 * lits.nVars();
            for (int i = 2; i <= nblits; i++) {
                if (lits.isUnassigned(i) && lits.watches(i).size() > 0
                        && lits.watches(i ^ 1).size() == 0) {
                    return i;
                }
            }
        }
        // not found: using normal order
        return super.select();
    }

    @Override
    public String toString() {
        return "tries to first branch on a single phase watched unassigned variable (pure literal if using a CB data structure) else VSIDS from MiniSAT"; //$NON-NLS-1$
    }
}

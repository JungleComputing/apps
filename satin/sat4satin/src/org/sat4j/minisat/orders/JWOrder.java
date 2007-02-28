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

import java.util.ArrayList;
import java.util.Collections;

import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.ILits23;

/*
 * Created on 16 oct. 2003
 */

/**
 * @author leberre Heuristique du prouveur. Changement par rapport au MiniSAT
 *         original : la gestion activity est faite ici et non plus dans Solver.
 */
public class JWOrder extends VarOrder {

    private static final long serialVersionUID = 1L;

    private ILits23 lits;

    private int computeWeight(int var) {
        final int p = (var << 1);
        int pos2 = lits.nBinaryClauses(p);
        int neg2 = lits.nBinaryClauses(p ^ 1);
        int pos3 = lits.nTernaryClauses(p);
        int neg3 = lits.nTernaryClauses(p ^ 1);
        long weight = (pos2 * neg2 * 100 + pos2 + neg2) * 5 + pos3 * neg3 * 10
                + pos3 + neg3;
        assert weight <= Integer.MAX_VALUE;
        if (weight == 0) {
            int pos = lits.watches(p).size();
            int neg = lits.watches(p ^ 1).size();
            weight = pos + neg;
        }
        return (int) weight;
    }

    class Temp implements Comparable<Temp> {
        private final int id;

        private final int count;

        Temp(int id) {
            this.id = id;
            count = computeWeight(id >> 1);
        }

        public int compareTo(Temp t) {
            if (count == 0) {
                return Integer.MAX_VALUE;
            }
            if (t.count == 0) {
                return -1;
            }
            return t.count - count;
        }

        @Override
        public String toString() {
            return "" + id + "(" + count + ")"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.VarOrder#setLits(org.sat4j.minisat.Lits)
     */
    @Override
    public void setLits(ILits lits) {
        super.setLits(lits);
        this.lits = (ILits23) lits;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.IHeuristics#init()
     */
    @Override
    public void init() {
        super.init();
        ArrayList<Temp> v = new ArrayList<Temp>(order.length);

        for (int i = 1; i < order.length; i++) {
            Temp t = new Temp(order[i]);
            v.add(t);
        }
        Collections.sort(v);
        // System.out.println(v);
        for (int i = 0; i < v.size(); i++) {
            Temp t = v.get(i);
            order[i + 1] = t.id;
            int index = t.id >> 1;
            varpos[index] = i + 1;
            activity[t.id >> 1] = t.count;
        }
        lastVar = 1;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.VarOrder#updateActivity(int)
     */
    @Override
    protected void updateActivity(int var) {
        activity[var] = computeWeight(var);
    }

    @Override
    public String toString() {
        return "Jeroslow-Wang static like heuristics updated when new clauses are learnt"; //$NON-NLS-1$
    }
}

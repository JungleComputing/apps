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

import static org.sat4j.minisat.core.LiteralsUtils.neg;
import static org.sat4j.minisat.core.LiteralsUtils.var;

import java.math.BigInteger;

import org.sat4j.reader.ObjectiveFunction;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class VarOrderHeapObjective extends VarOrderHeap {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private ObjectiveFunction obj;

    public void setObjectiveFunction(ObjectiveFunction obj) {
        this.obj = obj;
    }

    @Override
    public void init() {
        super.init();
        if (obj != null) {
            IVecInt vars = obj.getVars();
            IVec<BigInteger> coefs = obj.getCoeffs();
            for (int i = 0; i < vars.size(); i++) {
                int p = lits.getFromPool(vars.get(i));
                BigInteger c = coefs.get(i);
                if (c.signum() < 0) {
                    p = neg(p);
                }
                int var = var(p);
                activity[var] = c.abs().doubleValue();
                if (heap.inHeap(var))
                    heap.increase(var);
                phase[var] = neg(p);
            }
        }
    }
}

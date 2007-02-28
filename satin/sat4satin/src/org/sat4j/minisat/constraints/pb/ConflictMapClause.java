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
package org.sat4j.minisat.constraints.pb;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.sat4j.minisat.core.ILits;

public class ConflictMapClause extends ConflictMap {

    public ConflictMapClause(Map<Integer, BigInteger> m, BigInteger d,
            ILits voc, int level) {
        super(m, d, voc, level);
    }

    public static IConflict createConflict(PBConstr cpb, int level) {
        Map<Integer, BigInteger> m = new HashMap<Integer, BigInteger>();
        int lit;
        BigInteger coefLit;
        for (int i = 0; i < cpb.size(); i++) {
            lit = cpb.get(i);
            coefLit = cpb.getCoef(i);
            assert cpb.get(i) != 0;
            assert cpb.getCoef(i).signum() > 0;
            m.put(lit, coefLit);
        }
        return new ConflictMapClause(m, cpb.getDegree(), cpb.getVocabulary(),
                level);
    }

    /**
     * reduces the constraint defined by wpb until the result of the cutting
     * plane is a conflict. this reduction returns a clause.
     * 
     * @param litImplied
     * @param ind
     * @param reducedCoefs
     * @param wpb
     * @return
     */
    @Override
    protected BigInteger reduceUntilConflict(int litImplied, int ind,
            BigInteger[] reducedCoefs, WatchPb wpb) {
        for (int i = 0; i < reducedCoefs.length; i++)
            if (i == ind || wpb.getVocabulary().isFalsified(wpb.get(i)))
                reducedCoefs[i] = BigInteger.ONE;
            else
                reducedCoefs[i] = BigInteger.ZERO;
        coefMultCons = coefs.get(litImplied ^ 1);
        coefMult = BigInteger.ONE;
        return BigInteger.ONE;
    }
}

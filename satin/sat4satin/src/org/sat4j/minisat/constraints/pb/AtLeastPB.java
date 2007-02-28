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

import org.sat4j.minisat.constraints.card.AtLeast;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

public class AtLeastPB extends AtLeast implements PBConstr {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final BigInteger degree;

    private AtLeastPB(ILits voc, IVecInt ps, int degree) {
        super(voc, ps, degree);
        this.degree = BigInteger.valueOf(degree);
    }

    public static AtLeastPB atLeastNew(UnitPropagationListener s, ILits voc,
            IVecInt ps, int n) throws ContradictionException {
        int degree = niceParameters(s, voc, ps, n);
        if (degree == 0)
            return null;
        return new AtLeastPB(voc, ps, degree);
    }

    public static AtLeastPB atLeastNew(ILits voc, IVecInt ps, int n) {
        return new AtLeastPB(voc, ps, n);
    }

    public BigInteger getCoef(int literal) {
        return BigInteger.ONE;
    }

    public BigInteger getDegree() {
        return degree;
    }

    public ILits getVocabulary() {
        return voc;
    }

    public int[] getLits() {
        int[] tmp = new int[size()];
        System.arraycopy(lits, 0, tmp, 0, size());
        return tmp;
    }

    public BigInteger[] getCoefs() {
        BigInteger[] tmp = new BigInteger[size()];
        for (int i = 0; i < tmp.length; i++)
            tmp[i] = BigInteger.ONE;
        return tmp;
    }

    /**
     * 
     */
    private boolean learnt = false;

    /**
     * D?termine si la contrainte est apprise
     * 
     * @return true si la contrainte est apprise, false sinon
     * @see Constr#learnt()
     */
    @Override
    public boolean learnt() {
        return learnt;
    }

    @Override
    public void setLearnt() {
        learnt = true;
    }

    @Override
    public void register() {
        assert learnt;
        // countFalsified();
    }

    @Override
    public void assertConstraint(UnitPropagationListener s) {
        for (int i = 0; i < size(); i++) {
            if (getVocabulary().isUnassigned(get(i))) {
                boolean ret = s.enqueue(get(i), this);
                assert ret;
            }
        }
    }

    public IVecInt computeAnImpliedClause() {
        return null;
    }

}

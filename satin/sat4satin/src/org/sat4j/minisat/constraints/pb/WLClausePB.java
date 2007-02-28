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

import org.sat4j.minisat.constraints.cnf.DefaultWLClause;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.IVecInt;

public class WLClausePB extends DefaultWLClause implements PBConstr {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public WLClausePB(IVecInt ps, ILits voc) {
        super(ps, voc);
        // TODO Auto-generated constructor stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.pb.PBConstr#getCoefficient(int)
     */
    public BigInteger getCoef(int literal) {
        return BigInteger.ONE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.pb.PBConstr#getDegree()
     */
    public BigInteger getDegree() {
        return BigInteger.ONE;
    }

    public BigInteger[] getCoefs() {
        BigInteger[] tmp = new BigInteger[size()];
        for (int i = 0; i < tmp.length; i++)
            tmp[i] = BigInteger.ONE;
        return tmp;
    }

    /**
     * Creates a brand new clause, presumably from external data. Performs all
     * sanity checks.
     * 
     * @param s
     *            the object responsible for unit propagation
     * @param voc
     *            the vocabulary
     * @param literals
     *            the literals to store in the clause
     * @return the created clause or null if the clause should be ignored
     *         (tautology for example)
     */
    public static WLClausePB brandNewClause(UnitPropagationListener s,
            ILits voc, IVecInt literals) {
        WLClausePB c = new WLClausePB(literals, voc);
        c.register();
        return c;
    }

    @Override
    public void assertConstraint(UnitPropagationListener s) {
        for (int i = 0; i < size(); i++)
            if (getVocabulary().isUnassigned(get(i))) {
                boolean ret = s.enqueue(get(i), this);
                assert ret;
                break;
            }
    }

    public IVecInt computeAnImpliedClause() {
        return null;
    }

}

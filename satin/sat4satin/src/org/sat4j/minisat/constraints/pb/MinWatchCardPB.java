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

import org.sat4j.minisat.constraints.card.MinWatchCard;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

public class MinWatchCardPB extends MinWatchCard implements PBConstr {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final BigInteger degree;

    public MinWatchCardPB(ILits voc, IVecInt ps, boolean moreThan, int degree) {
        super(voc, ps, moreThan, degree);
        this.degree = BigInteger.valueOf(degree);
    }

    public MinWatchCardPB(ILits voc, IVecInt ps, int degree) {
        super(voc, ps, degree);
        this.degree = BigInteger.valueOf(degree);
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
        return degree;
    }

    public BigInteger[] getCoefs() {
        BigInteger[] tmp = new BigInteger[size()];
        for (int i = 0; i < tmp.length; i++)
            tmp[i] = BigInteger.ONE;
        return tmp;
    }

    /**
     * Permet la cr?ation de contrainte de cardinalit? ? observation minimale
     * 
     * @param s
     *            outil pour la propagation des litt?raux
     * @param voc
     *            vocabulaire utilis? par la contrainte
     * @param ps
     *            liste des litt?raux de la nouvelle contrainte
     * @param degree
     *            fournit le degr? de la contrainte
     * @return une nouvelle clause si tout va bien, null sinon
     * @throws ContradictionException
     */
    public static MinWatchCardPB normalizedMinWatchCardPBNew(
            UnitPropagationListener s, ILits voc, IVecInt ps, int degree)
            throws ContradictionException {
        return minWatchCardPBNew(s, voc, ps, ATLEAST, degree, true);
    }

    /**
     * Permet la cr?ation de contrainte de cardinalit? ? observation minimale
     * 
     * @param s
     *            outil pour la propagation des litt?raux
     * @param voc
     *            vocabulaire utilis? par la contrainte
     * @param ps
     *            liste des litt?raux de la nouvelle contrainte
     * @param moreThan
     *            d?termine si c'est une sup?rieure ou ?gal ? l'origine
     * @param degree
     *            fournit le degr? de la contrainte
     * @return une nouvelle clause si tout va bien, null sinon
     * @throws ContradictionException
     */
    public static MinWatchCardPB minWatchCardPBNew(UnitPropagationListener s,
            ILits voc, IVecInt ps, boolean moreThan, int degree)
            throws ContradictionException {
        return minWatchCardPBNew(s, voc, ps, moreThan, degree, false);
    }

    private static MinWatchCardPB minWatchCardPBNew(UnitPropagationListener s,
            ILits voc, IVecInt ps, boolean moreThan, int degree,
            boolean normalized) throws ContradictionException {
        int mydegree = degree + linearisation(voc, ps);

        if (ps.size() == 0 && mydegree > 0) {
            throw new ContradictionException();
        } else if (ps.size() == mydegree || ps.size() <= 0) {
            for (int i = 0; i < ps.size(); i++)
                if (!s.enqueue(ps.get(i))) {
                    throw new ContradictionException();
                }
            return null;
        }

        // constraint is now instanciated
        MinWatchCardPB retour = null;
        if (normalized)
            retour = new MinWatchCardPB(voc, ps, mydegree);
        else
            retour = new MinWatchCardPB(voc, ps, moreThan, mydegree);

        if (retour.degree.signum() <= 0)
            return null;

        retour.computeWatches();

        return ((MinWatchCardPB) retour.computePropagation(s));
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
        computeWatches();
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

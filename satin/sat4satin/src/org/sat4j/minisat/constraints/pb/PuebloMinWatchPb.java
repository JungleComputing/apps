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

import java.io.Serializable;
import java.math.BigInteger;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class PuebloMinWatchPb extends MinWatchPb implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Constructeur de base des contraintes
     * 
     * @param voc
     *            Informations sur le vocabulaire employ???
     * @param ps
     *            Liste des litt???raux
     * @param coefs
     *            Liste des coefficients
     * @param moreThan
     *            Indication sur le comparateur
     * @param degree
     *            Stockage du degr??? de la contrainte
     */
    private PuebloMinWatchPb(ILits voc, IDataStructurePB mpb) {

        super(voc, mpb);
    }

    /**
     * @param s
     *            outil pour la propagation des litt???raux
     * @param ps
     *            liste des litt???raux de la nouvelle contrainte
     * @param coefs
     *            liste des coefficients des litt???raux de la contrainte
     * @param moreThan
     *            d???termine si c'est une sup???rieure ou ???gal ??? l'origine
     * @param degree
     *            fournit le degr??? de la contrainte
     * @return une nouvelle clause si tout va bien, ou null si un conflit est
     *         d???tect???
     */
    public static PuebloMinWatchPb minWatchPbNew(UnitPropagationListener s,
            ILits voc, IVecInt ps, IVecInt coefs, boolean moreThan, int degree)
            throws ContradictionException {
        return minWatchPbNew(s, voc, ps, toVecBigInt(coefs), moreThan,
                toBigInt(degree));
    }

    /**
     * @param s
     *            outil pour la propagation des litt???raux
     * @param ps
     *            liste des litt???raux de la nouvelle contrainte
     * @param coefs
     *            liste des coefficients des litt???raux de la contrainte
     * @param moreThan
     *            d???termine si c'est une sup???rieure ou ???gal ??? l'origine
     * @param degree
     *            fournit le degr??? de la contrainte
     * @return une nouvelle clause si tout va bien, ou null si un conflit est
     *         d???tect???
     */
    public static PuebloMinWatchPb minWatchPbNew(UnitPropagationListener s,
            ILits voc, IVecInt ps, IVec<BigInteger> coefs, boolean moreThan,
            BigInteger degree) throws ContradictionException {
        // Il ne faut pas modifier les param?tres
        VecInt litsVec = new VecInt(ps.size());
        IVec<BigInteger> coefsVec = new Vec<BigInteger>(coefs.size());
        ps.copyTo(litsVec);
        coefs.copyTo(coefsVec);

        // Ajouter les simplifications quand la structure sera d???finitive
        IDataStructurePB mpb = niceParameters(litsVec, coefsVec, moreThan,
                degree, voc);
        PuebloMinWatchPb outclause = new PuebloMinWatchPb(voc, mpb);

        if (outclause.degree.signum() <= 0) {
            return null;
        }

        outclause.computeWatches();

        outclause.computePropagation(s);

        return outclause;

    }

    public static PuebloMinWatchPb minWatchPbNew(UnitPropagationListener s,
            ILits voc, IDataStructurePB mpb) throws ContradictionException {
        PuebloMinWatchPb outclause = new PuebloMinWatchPb(voc, mpb);

        if (outclause.degree.signum() <= 0) {
            return null;
        }

        outclause.computeWatches();

        outclause.computePropagation(s);

        return outclause;

    }

    /**
     * 
     */
    public static WatchPb watchPbNew(ILits voc, IVecInt lits, IVecInt coefs,
            boolean moreThan, int degree) {
        return watchPbNew(voc, lits, toVecBigInt(coefs), moreThan,
                toBigInt(degree));
    }

    /**
     * 
     */
    public static WatchPb watchPbNew(ILits voc, IVecInt lits,
            IVec<BigInteger> coefs, boolean moreThan, BigInteger degree) {
        IDataStructurePB mpb = null;
        mpb = niceCheckedParameters(lits, coefs, moreThan, degree, voc);
        return new PuebloMinWatchPb(voc, mpb);
    }

    public static WatchPb normalizedWatchPbNew(ILits voc, IDataStructurePB mpb) {
        return new PuebloMinWatchPb(voc, mpb);
    }

    @Override
    protected BigInteger maximalCoefficient(int pIndice) {
        return coefs[0];
    }

    @Override
    protected BigInteger updateWatched(BigInteger mc, int pIndice) {
        BigInteger maxCoef = mc;
        if (watchingCount < size()) {
            BigInteger upWatchCumul = watchCumul.subtract(coefs[pIndice]);
            BigInteger borneSup = degree.add(maxCoef);
            for (int ind = 0; ind < lits.length
                    && upWatchCumul.compareTo(borneSup) < 0; ind++) {
                if (!voc.isFalsified(lits[ind]) && !watched[ind]) {
                    upWatchCumul = upWatchCumul.add(coefs[ind]);
                    watched[ind] = true;
                    assert watchingCount < size();
                    watching[watchingCount++] = ind;
                    voc.watch(lits[ind] ^ 1, this);
                }
            }
            watchCumul = upWatchCumul.add(coefs[pIndice]);
        }
        return maxCoef;
    }

}
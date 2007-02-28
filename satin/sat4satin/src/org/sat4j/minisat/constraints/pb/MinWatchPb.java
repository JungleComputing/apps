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

public class MinWatchPb extends WatchPb implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Liste des indices des litt???raux regardant la contrainte
     */
    protected boolean[] watched;

    /**
     * Sert ??? d???terminer si la clause est watched par le litt???ral
     */
    protected int[] watching;

    /**
     * Liste des indices des litt???raux regardant la contrainte
     */
    protected int watchingCount = 0;

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
    protected MinWatchPb(ILits voc, IDataStructurePB mpb) {

        super(mpb);
        this.voc = voc;

        watching = new int[this.coefs.length];
        watched = new boolean[this.coefs.length];
        activity = 0;
        watchCumul = BigInteger.ZERO;
        locked = false;

        watchingCount = 0;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.WatchPb#computeWatches()
     */
    @Override
    protected void computeWatches() throws ContradictionException {
        assert watchCumul.signum() == 0;
        assert watchingCount == 0;
        for (int i = 0; i < lits.length
                && watchCumul.subtract(coefs[0]).compareTo(degree) < 0; i++) {
            if (!voc.isFalsified(lits[i])) {
                voc.watch(lits[i] ^ 1, this);
                watching[watchingCount++] = i;
                watched[i] = true;
                // Mise ??? jour de la possibilit??? initiale
                watchCumul = watchCumul.add(coefs[i]);
            }
        }

        if (learnt)
            watchMoreForLearntConstraint();

        if (watchCumul.compareTo(degree) < 0) {
            throw new ContradictionException("non satisfiable constraint");
        }
        assert nbOfWatched() == watchingCount;
    }

    private void watchMoreForLearntConstraint() {
        // chercher tous les litteraux a regarder
        // par ordre de niveau decroissant
        int free = 1;
        int maxlevel, maxi, level;

        while ((watchCumul.subtract(coefs[0]).compareTo(degree) < 0)
                && (free > 0)) {
            free = 0;
            // regarder le litteral falsifie au plus bas niveau
            maxlevel = -1;
            maxi = -1;
            for (int i = 0; i < lits.length; i++) {
                if (voc.isFalsified(lits[i]) && !watched[i]) {
                    free++;
                    level = voc.getLevel(lits[i]);
                    if (level > maxlevel) {
                        maxi = i;
                        maxlevel = level;
                    }
                }
            }

            if (free > 0) {
                assert maxi >= 0;
                voc.watch(lits[maxi] ^ 1, this);
                watching[watchingCount++] = maxi;
                watched[maxi] = true;
                // Mise ??? jour de la possibilit??? initiale
                watchCumul = watchCumul.add(coefs[maxi]);
                free--;
                assert free >= 0;
            }
        }
        assert lits.length == 1 || watchingCount > 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.WatchPb#computePropagation(org.sat4j.minisat.UnitPropagationListener)
     */
    @Override
    protected void computePropagation(UnitPropagationListener s)
            throws ContradictionException {
        // On propage si n???cessaire
        int ind = 0;
        while (ind < lits.length
                && watchCumul.subtract(coefs[watching[ind]]).compareTo(degree) < 0) {
            if (voc.isUnassigned(lits[ind]) && !s.enqueue(lits[ind], this)) {
                throw new ContradictionException("non satisfiable constraint");
            }
            ind++;
        }
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
    public static MinWatchPb minWatchPbNew(UnitPropagationListener s,
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
    public static MinWatchPb minWatchPbNew(UnitPropagationListener s,
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

        if (mpb == null)
            return null;

        MinWatchPb outclause = new MinWatchPb(voc, mpb);

        if (outclause.degree.signum() <= 0) {
            return null;
        }

        outclause.computeWatches();

        outclause.computePropagation(s);

        return outclause;

    }

    /**
     * @param s
     *            a unit propagation listener
     * @param voc
     *            the vocabulary
     * @param mpb
     *            the PB constraint to normalize.
     * @return a new PB contraint or null if a trivial inconsistency is
     *         detected.
     */
    public static MinWatchPb normalizedMinWatchPbNew(UnitPropagationListener s,
            ILits voc, IDataStructurePB mpb) throws ContradictionException {
        // Il ne faut pas modifier les param?tres
        MinWatchPb outclause = new MinWatchPb(voc, mpb);

        if (outclause.degree.signum() <= 0) {
            return null;
        }

        outclause.computeWatches();

        outclause.computePropagation(s);

        return outclause;

    }

    /**
     * Nombre de litt???raux actuellement observ???
     * 
     * @return nombre de litt???raux regard???s
     */
    protected int nbOfWatched() {
        int retour = 0;
        for (int ind = 0; ind < this.watched.length; ind++) {
            for (int i = 0; i < watchingCount; i++)
                if (watching[i] == ind)
                    assert watched[ind];
            retour += (this.watched[ind]) ? 1 : 0;
        }
        return retour;
    }

    /**
     * Propagation de la valeur de v???rit??? d'un litt???ral falsifi???
     * 
     * @param s
     *            un prouveur
     * @param p
     *            le litt???ral propag??? (il doit etre falsifie)
     * @return false ssi une inconsistance est d???t???ct???e
     */
    public boolean propagate(UnitPropagationListener s, int p) {
        assert nbOfWatched() == watchingCount;
        assert watchingCount > 1;

        // Recherche de l'indice du litt???ral p
        int pIndiceWatching = 0;
        while (pIndiceWatching < watchingCount
                && (lits[watching[pIndiceWatching]] ^ 1) != p)
            pIndiceWatching++;
        int pIndice = watching[pIndiceWatching];

        assert p == (lits[pIndice] ^ 1);
        assert watched[pIndice];

        // Recherche du coefficient maximal parmi ceux des litt???raux
        // observ???s
        BigInteger maxCoef = maximalCoefficient(pIndice);

        // Recherche de la compensation
        maxCoef = updateWatched(maxCoef, pIndice);

        BigInteger upWatchCumul = watchCumul.subtract(coefs[pIndice]);
        assert nbOfWatched() == watchingCount;

        // Effectuer les propagations, return si l'une est impossible
        if (upWatchCumul.compareTo(degree) < 0) {
            // conflit
            voc.watch(p, this);
            assert watched[pIndice];
            assert !isSatisfiable();
            return false;
        } else if (upWatchCumul.compareTo(degree.add(maxCoef)) < 0) {

            assert watchingCount != 0;
            BigInteger limit = upWatchCumul.subtract(degree);
            for (int i = 0; i < watchingCount; i++) {
                if (limit.compareTo(coefs[watching[i]]) < 0
                        && i != pIndiceWatching
                        && !voc.isSatisfied(lits[watching[i]])
                        && !s.enqueue(lits[watching[i]], this)) {
                    voc.watch(p, this);
                    assert !isSatisfiable();
                    return false;
                }
            }
            // Si propagation ajoute la contrainte aux undos de p, conserver p
            voc.undos(p).push(this);
        }

        // sinon p peut sortir de la liste de watched
        watched[pIndice] = false;
        watchCumul = upWatchCumul;
        watching[pIndiceWatching] = watching[--watchingCount];

        assert watchingCount != 0;
        assert nbOfWatched() == watchingCount;

        return true;
    }

    /**
     * Enl???ve une contrainte du prouveur
     */
    public void remove() {
        for (int i = 0; i < watchingCount; i++) {
            voc.watches(lits[watching[i]] ^ 1).remove(this);
            this.watched[this.watching[i]] = false;
        }
        watchingCount = 0;
        assert nbOfWatched() == watchingCount;
    }

    /**
     * M???thode appel???e lors du backtrack
     * 
     * @param p
     *            un litt???ral d???saffect???
     */
    public void undo(int p) {
        voc.watch(p, this);
        int pIndice = 0;
        while ((lits[pIndice] ^ 1) != p)
            pIndice++;

        assert pIndice < lits.length;

        watchCumul = watchCumul.add(coefs[pIndice]);

        assert watchingCount == nbOfWatched();

        watched[pIndice] = true;
        watching[watchingCount++] = pIndice;

        assert watchingCount == nbOfWatched();
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
        return new MinWatchPb(voc, mpb);
    }

    /**
     * 
     */
    public static WatchPb normalizedWatchPbNew(ILits voc, IDataStructurePB mpb)
            throws ContradictionException {
        return new MinWatchPb(voc, mpb);
    }

    /**
     * returns the maximal coefficient for the watched literals
     * 
     * @param pIndice
     *            propagated literal : its coefficient is excluded from the
     *            search of the maximal coefficient
     * @return
     */
    protected BigInteger maximalCoefficient(int pIndice) {
        BigInteger maxCoef = BigInteger.ZERO;
        for (int i = 0; i < watchingCount; i++)
            if (coefs[watching[i]].compareTo(maxCoef) > 0
                    && watching[i] != pIndice) {
                maxCoef = coefs[watching[i]];
            }

        assert learnt || maxCoef.signum() != 0;
        // DLB assert maxCoef!=0;
        return maxCoef;
    }

    protected BigInteger updateWatched(BigInteger mc, int pIndice) {
        BigInteger maxCoef = mc;
        if (watchingCount < size()) {
            BigInteger upWatchCumul = watchCumul.subtract(coefs[pIndice]);

            BigInteger degreePlusMaxCoef = degree.add(maxCoef); // dvh
            for (int ind = 0; ind < lits.length; ind++) {
                if (upWatchCumul.compareTo(degreePlusMaxCoef) >= 0) {
                    // note: logic negated to old version // dvh
                    break;
                }

                if (!voc.isFalsified(lits[ind]) && !watched[ind]) {
                    upWatchCumul = upWatchCumul.add(coefs[ind]);
                    watched[ind] = true;
                    assert watchingCount < size();
                    watching[watchingCount++] = ind;
                    voc.watch(lits[ind] ^ 1, this);
                    // Si on obtient un nouveau coefficient maximum
                    if (coefs[ind].compareTo(maxCoef) > 0) {
                        maxCoef = coefs[ind];
                        degreePlusMaxCoef = degree.add(maxCoef); // update
                        // that one
                        // too
                    }
                }
            }
            watchCumul = upWatchCumul.add(coefs[pIndice]);
        }
        return maxCoef;
    }

}
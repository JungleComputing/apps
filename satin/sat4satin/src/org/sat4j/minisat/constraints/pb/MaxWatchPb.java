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

public class MaxWatchPb extends WatchPb implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Constructeur de base cr?ant des contraintes vides
     * 
     * @param voc
     *            Informations sur le vocabulaire employ?
     * @param ps
     *            Liste des litt?raux
     * @param coefs
     *            Liste des coefficients
     * @param moreThan
     *            Indication sur le comparateur
     * @param degree
     *            Stockage du degr? de la contrainte
     */
    private MaxWatchPb(ILits voc, IDataStructurePB mpb) {

        super(mpb);
        this.voc = voc;

        activity = 0;
        watchCumul = BigInteger.ZERO;
        locked = false;
    }

    /**
     * Permet l'observation de tous les litt???raux
     * 
     * @see org.sat4j.minisat.constraints.WatchPb#computeWatches()
     */
    @Override
    protected void computeWatches() throws ContradictionException {
        assert watchCumul.equals(BigInteger.ZERO);
        for (int i = 0; i < lits.length; i++) {
            if (voc.isFalsified(lits[i])) {
                if (learnt)
                    voc.undos(lits[i] ^ 1).push(this);
            } else {
                // Mise ? jour de la possibilit? initiale
                voc.watch(lits[i] ^ 1, this);
                watchCumul = watchCumul.add(coefs[i]);
            }
        }

        assert watchCumul.compareTo(recalcLeftSide()) >= 0;
        if (!learnt && watchCumul.compareTo(degree) < 0) {
            throw new ContradictionException("non satisfiable constraint");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.constraints.WatchPb#computePropagation()
     */
    @Override
    protected void computePropagation(UnitPropagationListener s)
            throws ContradictionException {
        // On propage
        int ind = 0;
        while (ind < coefs.length
                && watchCumul.subtract(coefs[ind]).compareTo(degree) < 0) {
            if (voc.isUnassigned(lits[ind]) && !s.enqueue(lits[ind], this)) {
                throw new ContradictionException("non satisfiable constraint");
            }
            ind++;
        }
        assert watchCumul.compareTo(recalcLeftSide()) >= 0;
    }

    /**
     * @param s
     *            outil pour la propagation des litt?raux
     * @param ps
     *            liste des litt?raux de la nouvelle contrainte
     * @param coefs
     *            liste des coefficients des litt?raux de la contrainte
     * @param moreThan
     *            d?termine si c'est une sup?rieure ou ?gal ? l'origine
     * @param degree
     *            fournit le degr? de la contrainte
     * @return une nouvelle clause si tout va bien, ou null si un conflit est
     *         d?tect?
     */
    public static MaxWatchPb maxWatchPbNew(UnitPropagationListener s,
            ILits voc, IVecInt ps, IVecInt coefs, boolean moreThan, int degree)
            throws ContradictionException {
        return maxWatchPbNew(s, voc, ps, toVecBigInt(coefs), moreThan,
                toBigInt(degree));
    }

    /**
     * @param s
     *            outil pour la propagation des litt?raux
     * @param ps
     *            liste des litt?raux de la nouvelle contrainte
     * @param coefs
     *            liste des coefficients des litt?raux de la contrainte
     * @param moreThan
     *            d?termine si c'est une sup?rieure ou ?gal ? l'origine
     * @param degree
     *            fournit le degr? de la contrainte
     * @return une nouvelle clause si tout va bien, ou null si un conflit est
     *         d?tect?
     */
    public static MaxWatchPb maxWatchPbNew(UnitPropagationListener s,
            ILits voc, IVecInt ps, IVec<BigInteger> coefs, boolean moreThan,
            BigInteger degree) throws ContradictionException {

        // Il ne faut pas modifier les param?tres
        VecInt litsVec = new VecInt();
        IVec<BigInteger> coefsVec = new Vec<BigInteger>();
        ps.copyTo(litsVec);
        coefs.copyTo(coefsVec);

        IDataStructurePB mpb = niceParameters(litsVec, coefsVec, moreThan,
                degree, voc);

        if (mpb == null) {
            return null;
        }
        MaxWatchPb outclause = new MaxWatchPb(voc, mpb);

        if (outclause.degree.signum() <= 0)
            return null;
        outclause.computeWatches();
        outclause.computePropagation(s);

        return outclause;

    }

    /**
     * Propagation de la valeur de v?rit? d'un litt?ral falsifi?
     * 
     * @param s
     *            un prouveur
     * @param p
     *            le litt?ral propag? (il doit etre falsifie)
     * @return false ssi une inconsistance est d?tect?e
     */
    public boolean propagate(UnitPropagationListener s, int p) {
        voc.watch(p, this);

        assert watchCumul.compareTo(recalcLeftSide()) >= 0 : "" + watchCumul
                + "/" + recalcLeftSide() + ":" + learnt;

        // Si le litt?ral est impliqu? il y a un conflit
        int indiceP = 0;
        while ((lits[indiceP] ^ 1) != p)
            indiceP++;

        BigInteger coefP = coefs[indiceP];

        BigInteger newcumul = watchCumul.subtract(coefP);
        if (newcumul.compareTo(degree) < 0) {
            // System.out.println(this.analyse(new ConstrHandle()));

            assert !isSatisfiable();
            return false;
        }

        // On met en place la mise ? jour du compteur
        voc.undos(p).push(this);
        watchCumul = newcumul;

        // On propage
        int ind = 0;
        BigInteger limit = watchCumul.subtract(degree);
        while (ind < coefs.length && limit.compareTo(coefs[ind]) < 0) {
            if (voc.isUnassigned(lits[ind]) && (!s.enqueue(lits[ind], this))) {
                assert !isSatisfiable();
                return false;
            }
            ind++;
        }

        assert learnt || watchCumul.compareTo(recalcLeftSide()) >= 0;
        assert watchCumul.compareTo(recalcLeftSide()) >= 0;
        return true;
    }

    /**
     * Enl???ve une contrainte du prouveur
     */
    public void remove() {
        for (int i = 0; i < lits.length; i++) {
            if (!voc.isFalsified(lits[i]))
                voc.watches(lits[i] ^ 1).remove(this);
        }
    }

    /**
     * M?thode appel?e lors du backtrack
     * 
     * @param p
     *            un litt?ral d?saffect?
     */
    public void undo(int p) {
        int indiceP = 0;
        while ((lits[indiceP] ^ 1) != p)
            indiceP++;

        assert coefs[indiceP].signum() > 0;

        watchCumul = watchCumul.add(coefs[indiceP]);
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
        return new MaxWatchPb(voc, mpb);
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
    public static MaxWatchPb normalizedMaxWatchPbNew(UnitPropagationListener s,
            ILits voc, IDataStructurePB mpb) throws ContradictionException {
        // Il ne faut pas modifier les param?tres
        MaxWatchPb outclause = new MaxWatchPb(voc, mpb);

        if (outclause.degree.signum() <= 0) {
            return null;
        }

        outclause.computeWatches();

        outclause.computePropagation(s);

        return outclause;

    }

}
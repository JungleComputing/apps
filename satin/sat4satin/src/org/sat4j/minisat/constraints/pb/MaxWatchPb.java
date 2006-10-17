
/*
 * MiniSAT in Java, a Java based-SAT framework Copyright (C) 2004 Daniel Le
 * Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas Een and Niklas Serensson. Proceedings of the
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

    private long status = 0L;

    /**
     * Constructeur de base creant des contraintes vides
     * 
     * @param voc
     *            Informations sur le vocabulaire employe
     * @param ps
     *            Liste des litteraux
     * @param coefs
     *            Liste des coefficients
     * @param moreThan
     *            Indication sur le comparateur
     * @param degree
     *            Stockage du degre de la contrainte
     */
    private MaxWatchPb(ILits voc, IVecInt ps, IVec<BigInteger> bigCoefs,
        boolean moreThan, BigInteger bigDeg) {

        super(ps, bigCoefs, moreThan, bigDeg);
        this.voc = voc;

        activity = 0;
        watchCumul = BigInteger.ZERO;
        locked = false;
    }

    /**
     * Permet l'observation de tous les litteraux
     * 
     * @see org.sat4j.minisat.constraints.WatchPb#computeWatches()
     */
    @Override
    protected void computeWatches() throws ContradictionException {
        assert watchCumul.equals(BigInteger.ZERO);
        for (int i = 0; i < lits.length; i++) {
            // Mise e jour de la possibilite initiale
            voc.watch(lits[i] ^ 1, this);
            watchCumul = watchCumul.add(coefs[i]);
        }
        assert watchCumul.compareTo(recalcLeftSide()) >= 0;
        if (!learnt) {
            if (watchCumul.compareTo(degree) < 0) {
                throw new ContradictionException("non satisfiable constraint");
            }
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
            if (voc.isUnassigned(lits[ind])) {
                if (!s.enqueue(lits[ind], this)) {
                    throw new ContradictionException(
                        "non satisfiable constraint");
                }
            }
            ind++;
        }
        assert watchCumul.compareTo(recalcLeftSide()) >= 0;
    }

    /**
     * @param s
     *            outil pour la propagation des litteraux
     * @param ps
     *            liste des litteraux de la nouvelle contrainte
     * @param coefs
     *            liste des coefficients des litteraux de la contrainte
     * @param moreThan
     *            determine si c'est une superieure ou egal e l'origine
     * @param degree
     *            fournit le degre de la contrainte
     * @return une nouvelle clause si tout va bien, ou null si un conflit est
     *         detecte
     */
    public static MaxWatchPb maxWatchPbNew(UnitPropagationListener s,
        ILits voc, IVecInt ps, IVecInt coefs, boolean moreThan, int degree)
        throws ContradictionException {
        return maxWatchPbNew(s, voc, ps, toVecBigInt(coefs), moreThan,
            toBigInt(degree));
    }

    /**
     * @param s
     *            outil pour la propagation des litteraux
     * @param ps
     *            liste des litteraux de la nouvelle contrainte
     * @param coefs
     *            liste des coefficients des litteraux de la contrainte
     * @param moreThan
     *            determine si c'est une superieure ou egal e l'origine
     * @param degree
     *            fournit le degre de la contrainte
     * @return une nouvelle clause si tout va bien, ou null si un conflit est
     *         detecte
     */
    public static MaxWatchPb maxWatchPbNew(UnitPropagationListener s,
        ILits voc, IVecInt ps, IVec<BigInteger> coefs, boolean moreThan,
        BigInteger degree) throws ContradictionException {

        // Il ne faut pas modifier les parametres
        VecInt litsVec = new VecInt();
        IVec<BigInteger> coefsVec = new Vec<BigInteger>();
        ps.copyTo(litsVec);
        coefs.copyTo(coefsVec);

        niceParameter(litsVec, coefsVec);

        MaxWatchPb outclause = new MaxWatchPb(voc, litsVec, coefsVec, moreThan,
            degree);

        if (outclause.degree.signum() <= 0) return null;
        outclause.computeWatches();
        outclause.computePropagation(s);

        return outclause;

    }

    /**
     * Propagation de la valeur de verite d'un litteral falsifie
     * 
     * @param s
     *            un prouveur
     * @param p
     *            le litteral propage (il doit etre falsifie)
     * @return false ssi une inconsistance est detectee
     */
    public boolean propagate(UnitPropagationListener s, int p) {
        voc.watch(p, this);

        assert watchCumul.compareTo(recalcLeftSide()) >= 0;

        // Si le litteral est implique il y a un conflit
        int indiceP = 0;
        while ((lits[indiceP] ^ 1) != p)
            indiceP++;

        BigInteger coefP = coefs[indiceP];

        if (watchCumul.subtract(coefP).compareTo(degree) < 0) {
            // System.out.println(this.analyse(new ConstrHandle()));

            assert !isSatisfiable();
            return false;
        }

        // On met en place la mise e jour du compteur
        voc.undos(p).push(this);
        watchCumul = watchCumul.subtract(coefP);

        // On propage
        int ind = 0;
        while (ind < coefs.length
            && watchCumul.subtract(coefs[ind]).compareTo(degree) < 0) {
            if (voc.isUnassigned(lits[ind])) {
                if (!s.enqueue(lits[ind], this)) {
                    // lastConfl = p;
                    // System.out.println(this.analyse(new ConstrHandle()));
                    assert !isSatisfiable();
                    return false;
                }
                // logger.fine("litt\ufffd\ufffd\ufffdral impliqu\ufffd\ufffd\ufffd " +
                // Lits.toString(lits[ind]));
                // logger.fine(" par la contrainte " + this);
            }
            ind++;
        }

        assert learnt || watchCumul.compareTo(recalcLeftSide()) >= 0;
        assert watchCumul.compareTo(recalcLeftSide()) >= 0;
        return true;
    }

    /**
     * Enleeeve une contrainte du prouveur
     */
    public void remove() {
        for (int i = 0; i < lits.length; i++) {
            if (!voc.isFalsified(lits[i]))
                voc.watches(lits[i] ^ 1).remove(this);
        }
    }

    /**
     * Methode appelee lors du backtrack
     * 
     * @param p
     *            un litteral desaffecte
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
        return new MaxWatchPb(voc, lits, toVecBigInt(coefs), moreThan,
            toBigInt(degree));
    }

    /**
     * 
     */
    public static WatchPb watchPbNew(ILits voc, IVecInt lits,
        IVec<BigInteger> coefs, boolean moreThan, BigInteger degree) {
        return new MaxWatchPb(voc, lits, coefs, moreThan, degree);
    }

    public void setVoc(ILits newvoc) {
        voc = newvoc;
    }

    public void setStatus(long st) {
        status = st;
    }

    public long getStatus() {
        return status;
    }

    @Override
    public Object clone() {
        // TODO: deep copy
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
}

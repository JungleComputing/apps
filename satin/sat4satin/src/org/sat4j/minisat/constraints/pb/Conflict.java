/*
 * Created on Jan 17, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.sat4j.minisat.constraints.pb;

import java.math.BigInteger;
import java.util.Map;

import org.sat4j.minisat.core.ILits;

/**
 * @author parrain TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
class Conflict extends MapPb {

    private ILits voc;

    Conflict(Map<Integer, BigInteger> m, BigInteger d, ILits voc) {
        super(m, d);
        this.voc = voc;
    }

    private BigInteger coefMult = BigInteger.ZERO;

    private BigInteger coefMultCons = BigInteger.ZERO;

    /**
     * Effectue une resolution avec une contrainte PB. Met a jour le Conflict.
     * 
     * @param wpb
     *            contrainte avec laquelle on va faire la resolution
     * @param litImplied
     *            litteral devant etre resolu
     * @return la mise a jour du degre
     */
    BigInteger resolve(WatchPb wpb, int litImplied) {
        assert litImplied > 1;
        if (!coefs.containsKey(litImplied ^ 1)) {
            // logger.fine("pas de resolution");
            return degree;
        }

        assert slackConflict().signum() <= 0;
        assert degree.signum() >= 0;

        // copie des coeffs de la contrainte pour pouvoir effectuer
        // des operations de reductions
        BigInteger[] coefsCons = wpb.getCoefs();
        BigInteger degreeCons = wpb.getDegree();
        int[] litsCons = wpb.getLits();

        for (int i = 0; i < coefsCons.length; i++) {
            assert coefsCons[i].signum() > 0;
        }

        // Recherche de l'indice du litteral implique
        int ind = -1;
        while (litsCons[++ind] != litImplied) {
            ;
        }

        assert litsCons[ind] == litImplied;
        assert coefsCons[ind] != BigInteger.ZERO;
        // 1- Reduction de la contrainte
        // jusqu'a obtenir une resolvante conflictuelle
        degreeCons = reduceUntilConflict(litImplied, ind, litsCons, coefsCons,
                degreeCons, wpb);

        // 2- multiplication par coefMult des coefficients du conflit
        // mise-a-jour du degre du conflit
        degreeCons = (degreeCons.multiply(coefMultCons));
        degree = degree.multiply(coefMult);

        if (coefMult.compareTo(BigInteger.ONE) > 0) {
            for (Integer i : coefs.keySet()) {
                coefs.put(i, coefs.get(i).multiply(coefMult));
            }
        }

        // 3- On ajoute les informations de la contrainte courante
        // On effectue la resolution
        degree = addCoeffNewConstraint(litsCons, coefsCons, degreeCons,
                coefMultCons);
        assert !coefs.containsKey(litImplied);
        assert !coefs.containsKey(litImplied ^ 1);
        assert degree.signum() > 0;

        // 4- Saturation
        degree = saturation();
        assert slackConflict().signum() <= 0;

        return degree;
    }

    private BigInteger reduceUntilConflict(int litImplied, int ind, int[] lits,
            BigInteger[] coefsBis, BigInteger degreeBis, WatchPb wpb) {
        BigInteger slackResolve = BigInteger.ONE.negate();
        BigInteger slackThis = BigInteger.ZERO;
        BigInteger slackIndex = BigInteger.ZERO;
        BigInteger ppcm;

        int cpt = -1;

        do {
            cpt++;
            // logger.fine("Compteur de passages : "+cpt);
            if (slackResolve.signum() >= 0) {
                assert slackThis.signum() > 0;
                // r?duction de la contrainte
                BigInteger tmp = reduceInConstraint(coefsBis, lits, ind,
                        degreeBis);
                assert ((tmp.compareTo(degreeBis) < 0) && (tmp
                        .compareTo(BigInteger.ONE) >= 0));
                degreeBis = tmp;
            }
            // Recherche des coefficients multiplicateurs
            assert coefs.get(litImplied ^ 1).signum() > 0;
            assert coefsBis[ind].signum() > 0;

            ppcm = ppcm(coefsBis[ind], coefs.get(litImplied ^ 1));
            assert ppcm.signum() > 0;
            coefMult = ppcm.divide(coefs.get(litImplied ^ 1));
            coefMultCons = ppcm.divide(coefsBis[ind]);

            assert coefMultCons.signum() > 0;
            assert coefMult.signum() > 0;
            assert coefMult.multiply(coefs.get(litImplied ^ 1)).equals(
                    coefMultCons.multiply(coefsBis[ind]));

            // calcul des marges (poss) de chaque contrainte
            slackThis = wpb.slackConstraint(coefsBis, degreeBis).multiply(
                    coefMultCons);
            slackIndex = slackConflict().multiply(coefMult);

            assert slackIndex.signum() <= 0;

            // calcul (approximation) de la marge de la resolvante
            slackResolve = slackThis.add(slackIndex);
        } while (slackResolve.signum() >= 0);
        assert coefMult.multiply(coefs.get(litImplied ^ 1)).equals(
                coefMultCons.multiply(coefsBis[ind]));
        return degreeBis;

    }

    BigInteger slackConflict() {
        BigInteger poss = BigInteger.ZERO;
        // Pour chaque litteral
        for (Integer i : coefs.keySet()) {
            if (coefs.get(i).signum() != 0 && !voc.isFalsified(i)) {
                poss = poss.add(coefs.get(i));
            }
        }
        return poss.subtract(degree);
    }

    BigInteger getDegree() {
        return degree;
    }

    boolean isAssertive(int dl) {
        BigInteger slack = BigInteger.ZERO;
        for (Integer i : coefs.keySet()) {
            if ((coefs.get(i).signum() > 0)
                    && (((!voc.isFalsified(i)) || voc.getLevel(i) >= dl))) {
                slack = slack.add(coefs.get(i));
            }
        }
        slack = slack.subtract(degree);
        if (slack.signum() < 0) {
            return false;
        }
        for (Integer i : coefs.keySet()) {
            if ((coefs.get(i).signum() > 0)
                    && (voc.isUnassigned(i) || voc.getLevel(i) >= dl)
                    && (slack.subtract(coefs.get(i)).signum() < 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calcule le ppcm de deux nombres
     * 
     * @param a
     *            premier nombre de l'op?ration
     * @param b
     *            second nombre de l'op?ration
     * @return le ppcm en question
     */
    protected static BigInteger ppcm(BigInteger a, BigInteger b) {
        return a.divide(a.gcd(b)).multiply(b);
    }

    /**
     * Reduction d'une contrainte On supprime un litteral non assigne
     * prioritairement, vrai sinon. En aucun cas on ne supprime litImplied.
     * 
     * @param cycle
     *            on reduira par le cyclieme litteral
     * @return mise a jour du degre
     */
    public BigInteger reduceInConstraint(final BigInteger[] coefsBis,
            final int[] lits, final int indLitImplied,
            final BigInteger degreeBis) {
        // logger.entering(this.getClass().getName(),"reduceInConstraint");
        assert degreeBis.compareTo(BigInteger.ONE) > 0;
        // Recherche d'un litt?ral non assign?
        int lit = -1;
        for (int ind = 0; (ind < lits.length) && (lit == -1); ind++) {
            if (coefsBis[ind].signum() != 0 && voc.isUnassigned(lits[ind])) {
                assert coefsBis[ind].compareTo(degreeBis) < 0;
                lit = ind;
            }
        }

        // Sinon, recherche d'un litteral satisfait
        if (lit == -1) {
            for (int ind = 0; (ind < lits.length) && (lit == -1); ind++) {
                if ((coefsBis[ind].signum() != 0)
                        && (voc.isSatisfied(lits[ind]))
                        && (ind != indLitImplied)) {
                    lit = ind;
                }
            }
        }

        // on a trouve un litteral
        assert lit != -1;

        assert lit != indLitImplied;
        // logger.finer("Found literal "+Lits.toString(lits[lit]));
        // on reduit la contrainte
        BigInteger degUpdate = degreeBis.subtract(coefsBis[lit]);
        coefsBis[lit] = BigInteger.ZERO;

        // saturation de la contrainte
        assert degUpdate.signum() > 0;
        BigInteger minimum = degUpdate;
        for (int i = 0; i < coefsBis.length; i++) {
            if (coefsBis[i].signum() > 0) {
                minimum = minimum.min(coefsBis[i]);
            }
            coefsBis[i] = degUpdate.min(coefsBis[i]);
        }
        if (minimum.equals(degUpdate) && !degUpdate.equals(BigInteger.ONE)) {
            // on a obtenu une clause
            // plus de reduction possible
            degUpdate = BigInteger.ONE;
            for (int i = 0; i < coefsBis.length; i++) {
                if (coefsBis[i].signum() > 0) {
                    coefsBis[i] = degUpdate;
                }
            }
        }

        assert coefsBis[indLitImplied].signum() > 0;
        assert degreeBis.compareTo(degUpdate) > 0;
        return degUpdate;
    }

}

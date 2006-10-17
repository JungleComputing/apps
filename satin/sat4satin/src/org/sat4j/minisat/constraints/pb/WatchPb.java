/*
 * MiniSAT in Java, a Java based-SAT framework Copyright (C) 2004 Daniel Le
 * Berre
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
import java.util.Random;

import org.sat4j.core.Vec;
import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Undoable;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public abstract class WatchPb implements Constr, Undoable {

    /**
     * Constante pour le type d'in?galit?
     */
    public static final boolean ATMOST = false;

    public static final boolean ATLEAST = true;

    /**
     * Variable pour la g?n?ration de nombre al?atoire (sort)
     */
    private static final Random rand = new Random(91648253);

    /**
     * D???termine l'activit??? de la contrainte
     */
    protected double activity;

    /**
     * Liste des coefficients des litt???raux de la contrainte
     */
    protected BigInteger[] coefs;

    /**
     * Degr??? de la contrainte pseudo-bool???enne
     */
    protected BigInteger degree;

    /**
     * Liste des litt???raux de la contrainte
     */
    protected int[] lits;

    /**
     * D???termine si la contrainte est apprise
     */
    protected boolean learnt = false;

    /**
     * D???termine si la contrainte est la cause d'une propagation unitaire
     */
    protected boolean locked;

    /**
     * Est-ce une in???galit??? sup???rieure ou ???gale
     */
    protected boolean moreThan;

    /**
     * Possibilit??? pour la satisfaction de la contrainte
     */
    protected BigInteger watchCumul = BigInteger.ZERO;

    /**
     * Indice des litt???raux dans l'ordre des propagations
     */
    protected IVecInt undos;

    protected int backtrackLiteral;

    // protected Logger logger = Logger
    // .getLogger("org.sat4j.minisat.constraints.pb");

    /**
     * Vocabulaire de la contrainte
     */
    protected ILits voc;

    /**
     * This constructor is only available for the serialization.
     */
    WatchPb() {
    }

    WatchPb(IVecInt ps, IVec<BigInteger> bigCoefs, boolean moreThan,
            BigInteger bigDeg) {
        assert ps.size() > 0;
        assert ps.size() == bigCoefs.size();
        this.moreThan = moreThan;

        MapPb mpb = new MapPb();
        lits = new int[ps.size()];
        ps.copyTo(lits);
        BigInteger[] bc = new BigInteger[bigCoefs.size()];
        bigCoefs.copyTo(bc);
        BigInteger bigDegree = bigDeg;
        if (!moreThan) {
            for (int i = 0; i < lits.length; i++) {
                bc[i] = bc[i].negate();
            }
            bigDegree = bigDegree.negate();
        }
        for (int i = 0; i < bc.length; i++) {
            if (bc[i].signum() < 0) {
                lits[i] = lits[i] ^ 1;
                bc[i] = bc[i].negate();
                bigDegree = bigDegree.add(bc[i]);
            }
        }
        if (bigDegree.signum() > 0) {
            bigDegree = mpb.addCoeffNewConstraint(lits, bc, bigDegree);
        }
        if (bigDegree.signum() > 0) {
            bigDegree = mpb.saturation();
        }
        int size = mpb.size();
        lits = new int[size];
        this.coefs = new BigInteger[size];
        mpb.buildConstraintFromMapPb(lits, coefs);

        this.degree = bigDegree;

        // On peut trier suivant les coefficients
        sort();
    }

    /**
     * teste si la contrainte est assertive au niveau de d?cision > dl
     * 
     * @param dl
     * @return true iff the constraint is assertive at decision level dl.
     */
    public boolean isAssertive(int dl) {
        BigInteger slack = BigInteger.ZERO;
        for (int i = 0; i < lits.length; i++) {
            if ((coefs[i].signum() > 0)
                    && ((!voc.isFalsified(lits[i]) || voc.getLevel(lits[i]) >= dl))) {
                slack = slack.add(coefs[i]);
            }
        }
        slack = slack.subtract(degree);
        if (slack.signum() < 0) {
            return false;
        }
        for (int i = 0; i < lits.length; i++) {
            if ((coefs[i].signum() > 0)
                    && (voc.isUnassigned(lits[i]) || voc.getLevel(lits[i]) >= dl)
                    && (slack.subtract(coefs[i]).signum() < 0)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calcule la cause de l'affectation d'un litt???ral
     * 
     * @param p
     *            un litt???ral falsifi??? (ou Lit.UNDEFINED)
     * @param outReason
     *            la liste des litt???raux falsifi???s dont la n???gation
     *            correspond ??? la raison de l'affectation.
     * @see Constr#calcReason(int, IVecInt)
     */
    public void calcReason(int p, IVecInt outReason) {
        for (int q : lits) {
            if (voc.isFalsified(q)) {
                outReason.push(q ^ 1);
            }
        }
    }

    abstract protected void computeWatches() throws ContradictionException;

    abstract protected void computePropagation(UnitPropagationListener s)
            throws ContradictionException;

    /**
     * Permet d'obtenir le i-???me litt???ral de la contrainte
     * 
     * @param i
     *            indice du litt???ral recherch???
     * @return le litt???ral demand???
     */
    public int get(int i) {
        return lits[i];
    }

    /**
     * Permet d'obtenir le i-???me litt???ral de la contrainte
     * 
     * @param i
     *            indice du litt???ral recherch???
     * @return le litt???ral demand???
     */
    public BigInteger getCoef(int i) {
        return coefs[i];
    }

    /**
     * Obtenir la valeur de l'activit??? de la contrainte
     * 
     * @return la valeur de l'activit??? de la contrainte
     * @see Constr#getActivity()
     */
    public double getActivity() {
        return activity;
    }

    protected static void niceParameter(IVecInt lits, IVec<BigInteger> coefs)
            throws ContradictionException {
        // Ajouter les simplifications quand la structure sera d?finitive
        if (lits.size() == 0) {
            throw new ContradictionException("Creating Empty clause ?");
        } else if (lits.size() != coefs.size()) {
            throw new IllegalArgumentException(
                    "Contradiction dans la taille des tableaux ps="
                            + lits.size() + " coefs=" + coefs.size() + ".");
        }
    }

    /**
     * Incr???mente la valeur de l'activit??? de la contrainte
     * 
     * @see Constr#incActivity(double claInc)
     */
    public void incActivity(double claInc) {
        activity += claInc;
    }

    /**
     * Marge de la contrainte courante marge = poss - degre de la contrainte
     * 
     * @return la marge
     */
    public BigInteger slackConstraint() {
        return recalcLeftSide().subtract(this.degree);
    }

    /**
     * Marge de la contrainte courante marge = poss - degre de la contrainte
     * 
     * @param coefs
     *            le tableau des coefficients de la contrainte consideree
     * @param degree
     *            le degre de la contrainte consideree
     * @return la marge
     */
    public BigInteger slackConstraint(BigInteger[] coefs, BigInteger degree) {
        return recalcLeftSide(coefs).subtract(degree);
    }

    /**
     * somme des coefficients des litteraux satisfaits ou non assignes de la
     * resolvante
     * 
     * @param coefs
     *            le tableau des coefficients de la contrainte consid?r?e
     * @return cette somme (poss)
     */
    public BigInteger recalcLeftSide(BigInteger[] coefs) {
        BigInteger poss = BigInteger.ZERO;
        // Pour chaque litteral
        for (int i = 0; i < coefs.length; i++) {
            if ((coefs[i].signum() > 0) && (!voc.isFalsified(lits[i]))) {
                poss = poss.add(coefs[i]);
            }
        }
        return poss;
    }

    /**
     * somme des coefficients des litteraux satisfaits ou non assignes de la
     * resolvante
     * 
     * @return cette somme (poss)
     */
    public BigInteger recalcLeftSide() {
        return recalcLeftSide(this.coefs);
    }

    /**
     * D?termine si la contrainte est toujours satisfiable
     * 
     * @return la contrainte est encore satisfiable
     */
    protected boolean isSatisfiable() {
        BigInteger sum = BigInteger.ZERO;
        for (int i = 0; i < lits.length; i++) {
            if (!voc.isFalsified(lits[i])) {
                assert coefs[i].signum() > 0;
                sum = sum.add(coefs[i]);
            }
        }
        return sum.compareTo(degree) >= 0;
    }

    /**
     * Dit si la contrainte est apprise
     * 
     * @return true si la contrainte est apprise, false sinon
     * @see Constr#learnt()
     */
    public boolean learnt() {
        return learnt;
    }

    /**
     * La contrainte est la cause d'une propagation unitaire
     * 
     * @return true si c'est le cas, false sinon
     * @see Constr#locked()
     */
    public boolean locked() {
        return true;
    }

    /**
     * La contrainte est la cause d'une propagation unitaire
     * 
     * @return true si c'est le cas, false sinon
     * @see Constr#locked()
     */
    protected void normalize() {
        // logger.entering(this.getClass().getName(), "normalize");
        // logger.finer("Before normalizing " + this.toString());
        // Translate into >= form
        if (!moreThan) {
            for (int i = 0; i < lits.length; i++) {
                coefs[i] = coefs[i].negate();
            }
            degree = degree.negate();
            moreThan = true;
        }
        assert moreThan == true;

        // Replace negative coeff
        for (int indLit = 0; indLit < this.lits.length; indLit++) {
            if (coefs[indLit].signum() < 0) {
                lits[indLit] = lits[indLit] ^ 1;
                coefs[indLit] = coefs[indLit].negate();
                degree = degree.add(coefs[indLit]);
            }
            assert coefs[indLit].signum() >= 0;
        }
        // On peut trier suivant les coefficients
        sort();

        // Saturation
        int indLit = 0;
        while (coefs.length > indLit && coefs[indLit].compareTo(degree) > 0) {
            coefs[indLit++] = degree;
        }
        // si tous les coefficients ont la valeur degree (et degree > 0)
        // alors il s'agit d'une clause
        if (indLit == coefs.length && degree.signum() > 0) {
            degree = BigInteger.ONE;
            for (int i = 0; i < coefs.length; i++) {
                coefs[i] = degree;
            }
        }

        // logger.finer("After normalizing " + this.toString());
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
     * Permet le r??????chantillonage de l'activit??? de la contrainte
     * 
     * @param d
     *            facteur d'ajustement
     */
    public void rescaleBy(double d) {
        activity *= d;
    }

    void selectionSort(int from, int to) {
        int i, j, best_i;
        BigInteger tmp;
        int tmp2;

        for (i = from; i < to - 1; i++) {
            best_i = i;
            for (j = i + 1; j < to; j++) {
                if (coefs[j].compareTo(coefs[best_i]) > 0) {
                    best_i = j;
                }
            }
            tmp = coefs[i];
            coefs[i] = coefs[best_i];
            coefs[best_i] = tmp;
            tmp2 = lits[i];
            lits[i] = lits[best_i];
            lits[best_i] = tmp2;
        }
    }

    /**
     * La contrainte est apprise
     */
    public void setLearnt() {
        learnt = true;
    }

    /**
     * Simplifie la contrainte(l'all???ge)
     * 
     * @return true si la contrainte est satisfaite, false sinon
     */
    public boolean simplify() {
        BigInteger cumul = BigInteger.ZERO;

        int i = 0;
        while (i < lits.length && cumul.compareTo(degree) < 0) {
            if (voc.isSatisfied(lits[i])) {
                // Mesure pessimiste
                cumul = cumul.add(coefs[i]);
            }
            i++;
        }

        return (cumul.compareTo(degree) >= 0);
    }

    public int size() {
        return lits.length;
    }

    /**
     * Tri des tableaux
     */
    protected void sort() {
        assert this.lits != null;
        if (size() > 0) {
            this.sort(0, size());
            BigInteger buffInt = coefs[0];
            for (int i = 1; i < size(); i++) {
                assert buffInt.compareTo(coefs[i]) >= 0;
                buffInt = coefs[i];
            }

        }
    }

    /**
     * Tri d'une partie des tableaux
     * 
     * @param from
     *            indice du d???but du tri
     * @param to
     *            indice de fin du tri
     */
    protected void sort(int from, int to) {
        int width = to - from;
        if (to - from <= 15) {
            selectionSort(from, to);
        } else {
            BigInteger pivot = coefs[rand.nextInt(width) + from];
            BigInteger tmp;
            int i = from - 1;
            int j = to;
            int tmp2;

            for (;;) {
                do {
                    i++;
                } while (coefs[i].compareTo(pivot) > 0);
                do {
                    j--;
                } while (pivot.compareTo(coefs[j]) > 0);

                if (i >= j) {
                    break;
                }

                tmp = coefs[i];
                coefs[i] = coefs[j];
                coefs[j] = tmp;
                tmp2 = lits[i];
                lits[i] = lits[j];
                lits[j] = tmp2;
            }

            sort(from, i);
            sort(i, to);
        }

    }

    /**
     * Cha???ne repr???sentant la contrainte
     * 
     * @return Cha???ne repr???sentant la contrainte
     */
    @Override
    public String toString() {
        StringBuffer stb = new StringBuffer();

        if (lits.length > 0) {
            // if(voc.isUnassigned(lits[0])){
            stb.append(this.coefs[0]);
            stb.append(".");
            stb.append(Lits.toString(this.lits[0]));
            stb.append("[");
            stb.append(voc.valueToString(lits[0]));
            stb.append("@");
            stb.append(voc.getLevel(lits[0]));
            stb.append("]");
            stb.append(" ");
            // }
            for (int i = 1; i < lits.length; i++) {
                // if (voc.isUnassigned(lits[i])) {
                stb.append(" + ");
                stb.append(this.coefs[i]);
                stb.append(".");
                stb.append(Lits.toString(this.lits[i]));
                stb.append("[");
                stb.append(voc.valueToString(lits[i]));
                stb.append("@");
                stb.append(voc.getLevel(lits[i]));
                stb.append("]");
                stb.append(" ");
                // }
            }
            stb.append((this.moreThan) ? ">= " : "<= ");
            stb.append(this.degree);
        }
        return stb.toString();
    }

    /**
     * retourne le niveau de backtrack : c'est-?-dire le niveau le plus haut
     * pour lequel la contrainte est assertive
     * 
     * @param maxLevel 
     *            le plus bas niveau pour lequel la contrainte est assertive
     * @return the highest level (smaller int) for which the constraint is assertive.
     */
    public int getBacktrackLevel(int maxLevel) {
        int litLevel;
        int borneMax = maxLevel;
        // System.out.println(this);
        // System.out.println("assertive au niveau : " + maxLevel);
        assert isAssertive(borneMax);
        int borneMin = -1;
        // on calcule borneMax,
        // le niveau le plus haut dans l'arbre ou la contrainte est assertive
        for (int i = 0; i < lits.length; i++) {
            litLevel = voc.getLevel(lits[i]);
            if (litLevel < borneMax && litLevel > borneMin) {
                if (isAssertive(litLevel)) {
                    borneMax = litLevel;
                } else {
                    borneMin = litLevel;
                }
            }
        }
        // on retourne le niveau immediatement inferieur ? borneMax
        // pour lequel la contrainte possede un literal
        int retour = 0;
        for (int i = 0; i < lits.length; i++) {
            litLevel = voc.getLevel(lits[i]);
            if (litLevel > retour && litLevel < borneMax) {
                retour = litLevel;
            }
        }
        return retour;
    }

    public void assertConstraint(UnitPropagationListener s) {
        BigInteger tmp = slackConstraint();
        for (int i = 0; i < lits.length; i++) {
            if (voc.isUnassigned(lits[i])
                    && tmp.subtract(coefs[i]).signum() < 0) {
                boolean ret = s.enqueue(lits[i], this);
                assert ret;
            }
        }
    }

    // protected abstract WatchPb watchPbNew(Lits voc, VecInt lits, VecInt
    // coefs, boolean moreThan, int degree, int[] indexer);
    /**
     * @return Returns the degree.
     */
    public BigInteger getDegree() {
        return degree;
    }

    public void register() {
        assert learnt;
        try {
            computeWatches();
        } catch (ContradictionException e) {
            System.out.println(this);
            assert false;
        }
    }

    protected static IVec<BigInteger> toVecBigInt(IVecInt vec) {
        IVec<BigInteger> bigVec = new Vec<BigInteger>(vec.size());
        for (int i = 0; i < vec.size(); ++i) {
            bigVec.push(BigInteger.valueOf(vec.get(i)));
        }
        return bigVec;
    }

    protected static BigInteger toBigInt(int i) {
        return BigInteger.valueOf(i);
    }

    protected Conflict createConflict() {
        Map<Integer, BigInteger> m = new HashMap<Integer, BigInteger>();
        for (int i = 0; i < lits.length; i++) {
            assert lits[i] != 0;
            assert coefs[i].signum() > 0;
            m.put(lits[i], coefs[i]);
        }
        return new Conflict(m, degree, voc);
    }

    protected BigInteger[] getCoefs() {
        BigInteger[] coefsBis = new BigInteger[coefs.length];
        System.arraycopy(coefs, 0, coefsBis, 0, coefs.length);
        return coefsBis;
    }

    protected int[] getLits() {
        int[] litsBis = new int[lits.length];
        System.arraycopy(lits, 0, litsBis, 0, lits.length);
        return litsBis;
    }

}

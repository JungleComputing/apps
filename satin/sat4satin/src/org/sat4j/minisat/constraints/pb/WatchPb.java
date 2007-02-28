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
import java.util.Random;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Undoable;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public abstract class WatchPb implements PBConstr, Undoable, Serializable {

    /**
     * constant for the initial type of inequality less than or equal
     */
    public static final boolean ATMOST = false;

    /**
     * constant for the initial type of inequality more than or equal
     */
    public static final boolean ATLEAST = true;

    /**
     * variable needed for the sort method
     */
    private static final Random rand = new Random(91648253);

    /**
     * constraint activity
     */
    protected double activity;

    /**
     * coefficients of the literals of the constraint
     */
    protected BigInteger[] coefs;

    /**
     * degree of the pseudo-boolean constraint
     */
    protected BigInteger degree;

    /**
     * literals of the constraint
     */
    protected int[] lits;

    /**
     * true if the constraint is a learned constraint
     */
    protected boolean learnt = false;

    /**
     * true if the constraint is the origin of unit propagation
     */
    protected boolean locked;

    /**
     * sum of the coefficients of the literals satisfied or unvalued
     */
    protected BigInteger watchCumul = BigInteger.ZERO;

    /**
     * constraint's vocabulary
     */
    protected ILits voc;

    /**
     * This constructor is only available for the serialization.
     */
    WatchPb() {
    }

    WatchPb(IDataStructurePB mpb) {
        int size = mpb.size();
        lits = new int[size];
        this.coefs = new BigInteger[size];
        mpb.buildConstraintFromMapPb(lits, coefs);

        this.degree = mpb.getDegree();

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
                    && ((!voc.isFalsified(lits[i]) || voc.getLevel(lits[i]) >= dl)))
                slack = slack.add(coefs[i]);
        }
        slack = slack.subtract(degree);
        if (slack.signum() < 0)
            return false;
        for (int i = 0; i < lits.length; i++) {
            if ((coefs[i].signum() > 0)
                    && (voc.isUnassigned(lits[i]) || voc.getLevel(lits[i]) >= dl)
                    && (slack.compareTo(coefs[i]) < 0)) {
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

    public static IDataStructurePB niceParameters(IVecInt ps,
            IVec<BigInteger> bigCoefs, boolean moreThan, BigInteger bigDeg,
            ILits voc) throws ContradictionException {
        // Ajouter les simplifications quand la structure sera d?finitive
        if (ps.size() == 0) {
            throw new ContradictionException("Creating Empty clause ?");
        } else if (ps.size() != bigCoefs.size()) {
            throw new IllegalArgumentException(
                    "Contradiction dans la taille des tableaux ps=" + ps.size()
                            + " coefs=" + bigCoefs.size() + ".");
        }
        return niceCheckedParameters(ps, bigCoefs, moreThan, bigDeg, voc);
    }

    public static IDataStructurePB niceCheckedParameters(IVecInt ps,
            IVec<BigInteger> bigCoefs, boolean moreThan, BigInteger bigDeg,
            ILits voc) {
        assert ps.size() != 0 && ps.size() == bigCoefs.size();
        int[] lits = new int[ps.size()];
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

        for (int i = 0; i < bc.length; i++)
            if (bc[i].signum() < 0) {
                lits[i] = lits[i] ^ 1;
                bc[i] = bc[i].negate();
                bigDegree = bigDegree.add(bc[i]);
            }

        IDataStructurePB mpb = new MapPb(voc);
        if (bigDegree.signum() > 0)
            bigDegree = mpb.cuttingPlane(lits, bc, bigDegree);
        if (bigDegree.signum() > 0)
            bigDegree = mpb.saturation();
        if (bigDegree.signum() <= 0)
            return null;
        return mpb;
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
        for (int i = 0; i < lits.length; i++)
            if (!voc.isFalsified(lits[i])) {
                assert coefs[i].signum() >= 0;
                poss = poss.add(coefs[i]);
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
        return recalcLeftSide().compareTo(degree) >= 0;
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
                if (coefs[j].compareTo(coefs[best_i]) > 0)
                    best_i = j;
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
    final protected void sort() {
        assert this.lits != null;
        if (coefs.length > 0) {
            this.sort(0, size());
            BigInteger buffInt = coefs[0];
            for (int i = 1; i < coefs.length; i++) {
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
    final protected void sort(int from, int to) {
        int width = to - from;
        if (to - from <= 15)
            selectionSort(from, to);

        else {
            BigInteger pivot = coefs[rand.nextInt(width) + from];
            BigInteger tmp;
            int i = from - 1;
            int j = to;
            int tmp2;

            for (;;) {
                do
                    i++;
                while (coefs[i].compareTo(pivot) > 0);
                do
                    j--;
                while (pivot.compareTo(coefs[j]) > 0);

                if (i >= j)
                    break;

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
            for (int i = 0; i < lits.length; i++) {
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
            stb.append(">= ");
            stb.append(this.degree);
        }
        return stb.toString();
    }

    public void assertConstraint(UnitPropagationListener s) {
        BigInteger tmp = slackConstraint();
        for (int i = 0; i < lits.length; i++) {
            if (voc.isUnassigned(lits[i]) && tmp.compareTo(coefs[i]) < 0) {
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

    public static IVec<BigInteger> toVecBigInt(IVecInt vec) {
        IVec<BigInteger> bigVec = new Vec<BigInteger>(vec.size());
        for (int i = 0; i < vec.size(); ++i)
            bigVec.push(BigInteger.valueOf(vec.get(i)));
        return bigVec;
    }

    public static BigInteger toBigInt(int i) {
        return BigInteger.valueOf(i);
    }

    public BigInteger[] getCoefs() {
        BigInteger[] coefsBis = new BigInteger[coefs.length];
        System.arraycopy(coefs, 0, coefsBis, 0, coefs.length);
        return coefsBis;
    }

    public int[] getLits() {
        int[] litsBis = new int[lits.length];
        System.arraycopy(lits, 0, litsBis, 0, lits.length);
        return litsBis;
    }

    public ILits getVocabulary() {
        return voc;
    }

    /**
     * compute an implied clause on the literals with the greater coefficients
     */
    public IVecInt computeAnImpliedClause() {
        BigInteger cptCoefs = BigInteger.ZERO;
        int index = coefs.length;
        while ((cptCoefs.compareTo(degree) > 0) && (index > 0)) {
            cptCoefs = cptCoefs.add(coefs[--index]);
        }
        if (index > 0 && index < size() / 2) {
            // System.out.println(this);
            // System.out.println("index : "+index);
            IVecInt literals = new VecInt(index);
            for (int j = 0; j <= index; j++)
                literals.push(lits[j]);
            return literals;
        }
        return null;
    }

    public boolean coefficientsEqualToOne() {
        return false;
    }

    // SATIN:
    protected boolean learntGlobal = false;

    public boolean learntGlobal() {
        return learntGlobal;
    }

    public void setLearntGlobal() {
        learntGlobal = true;
    }
}

/*
 * MiniSAT in Java, a Java based-SAT framework
 * Copyright (C) 2004 Daniel Le Berre
 *
 * Based on the original minisat specification from:
 *
 * An extensible SAT solver. Niklas E?n and Niklas S?rensson.
 * Proceedings of the Sixth International Conference on Theory
 * and Applications of Satisfiability Testing, LNCS 2919,
 * pp 502-518, 2003.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.sat4j.minisat.constraints.card;

import java.io.Serializable;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Undoable;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

public class MaxWatchCard implements Constr, Undoable, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Degr? de la contrainte de cardinalit?
     */
    private int degree;

    /**
     * Liste des litt?raux de la contrainte
     */
    private int[] lits;

    /**
     * D?termine si c'est une in?galit? sup?rieure ou ?gale
     */
    private boolean moreThan;

    /**
     * Somme des coefficients des litt?raux observ?s
     */
    private int watchCumul;

    /**
     * Vocabulaire de la contrainte
     */
    private ILits voc;

    private long status = 0L;

    /**
     * Constructeur de base cr?ant des contraintes vides
     * 
     * @param size
     *            nombre de litt?raux de la contrainte
     * @param learnt
     *            indique si la contrainte est apprise
     */
    private MaxWatchCard(ILits voc, IVecInt ps, boolean moreThan, int degree) {

        // On met en place les valeurs
        this.voc = voc;
        this.degree = degree;
        this.moreThan = moreThan;

        // On simplifie ps
        int[] index = new int[voc.nVars() * 2 + 2];
        for (int i = 0; i < index.length; i++)
            index[i] = 0;
        // On repertorie les litt?raux utiles
        for (int i = 0; i < ps.size(); i++) {
            if (index[ps.get(i) ^ 1] != 0) {
                index[ps.get(i) ^ 1]--;
            } else {
                index[ps.get(i)]++;
            }
        }
        // On supprime les litt?raux inutiles
        int ind = 0;
        while (ind < ps.size()) {
            if (index[ps.get(ind)] > 0) {
                index[ps.get(ind)]--;
                ind++;
            } else {
                if ((ps.get(ind) & 1) != 0)
                    this.degree--;
                ps.set(ind, ps.last());
                ps.pop();
            }
        }

        // On copie les litt?raux de la contrainte
        lits = new int[ps.size()];
        ps.moveTo(lits);

        // On normalise la contrainte au sens de Barth
        normalize();

        // Mise en place de l'observation maximale
        watchCumul = 0;

        // On observe les litt?raux non falsifi?
        for (int i = 0; i < lits.length; i++) {
            // Rappel: les ?l?ments falsifi?s ne seront jamais d?pil?s
            if (!voc.isFalsified(lits[i])) {
                watchCumul++;
                voc.watch(lits[i] ^ 1, this);
            }
        }
    }

    /**
     * Calcule la cause de l'affection d'un litt?ral
     * 
     * @param p
     *            un litt?ral falsifi? (ou Lit.UNDEFINED)
     * @param outReason
     *            vecteur de litt?raux ? remplir
     * @see Constr#calcReason(int p, IVecInt outReason)
     */
    public void calcReason(int p, IVecInt outReason) {
        // TODO calcReason: v?rifier par rapport ? l'article
        // Pour chaque litt?ral
        for (int i = 0; i < lits.length; i++) {
            // Si il est falsifi?
            if (voc.isFalsified(lits[i])) {
                // On ajoute sa n?gation au vecteur
                outReason.push(lits[i] ^ 1);
            }
        }
    }

    /**
     * Obtenir la valeur de l'activit? de la contrainte
     * 
     * @return la valeur de l'activit? de la contrainte
     * @see Constr#getActivity()
     */
    public double getActivity() {
        // TODO getActivity
        return 0;
    }

    /**
     * Incr?mente la valeur de l'activit? de la contrainte
     * 
     * @param claInc
     *            incr?ment de l'activit? de la contrainte
     * @see Constr#incActivity(double claInc)
     */
    public void incActivity(double claInc) {
        // TODO incActivity
    }

    /**
     * D?termine si la contrainte est apprise
     * 
     * @return true si la contrainte est apprise, false sinon
     * @see Constr#learnt()
     */
    public boolean learnt() {
        // TODO learnt
        return false;
    }

    /**
     * La contrainte est la cause d'une propagation unitaire
     * 
     * @return true si c'est le cas, false sinon
     * @see Constr#locked()
     */
    public boolean locked() {
        // TODO locked
        return true;
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
    public static MaxWatchCard maxWatchCardNew(UnitPropagationListener s,
            ILits voc, IVecInt ps, boolean moreThan, int degree)
            throws ContradictionException {

        MaxWatchCard outclause = null;

        // La contrainte ne doit pas ?tre vide
        if (ps.size() == 0) {
            throw new ContradictionException("Cr?ation d'une clause vide");
        } else if (ps.size() == degree) {
            for (int i = 0; i < ps.size(); i++)
                if (!s.enqueue(ps.get(i))) {
                    throw new ContradictionException(
                            "Contradiction avec le litt?ral impliqu?.");
                }
            return null;
        }

        // On cree la contrainte
        outclause = new MaxWatchCard(voc, ps, moreThan, degree);

        // Si le degr? est insufisant
        if (outclause.degree <= 0)
            return null;

        // Si il n'y a aucune chance de satisfaire la contrainte
        if (outclause.watchCumul < outclause.degree)
            throw new ContradictionException("Contrainte non-satisfiable");

        // Si les litt?raux observ?s sont impliqu?s
        if (outclause.watchCumul == outclause.degree) {
            for (int i = 0; i < outclause.lits.length; i++) {
                if (!s.enqueue(outclause.lits[i])) {
                    throw new ContradictionException(
                            "Contradiction avec le litt?ral impliqu?.");
                }
            }
            return null;
        }

        return outclause;
    }

    /**
     * On normalise la contrainte au sens de Barth
     */
    public void normalize() {
        // Gestion du signe
        if (!moreThan) {
            // On multiplie le degr? par -1
            this.degree = 0 - this.degree;
            // On r?vise chaque litt?ral
            for (int indLit = 0; indLit < lits.length; indLit++) {
                lits[indLit] = lits[indLit] ^ 1;
                this.degree++;
            }
            this.moreThan = true;
        }
    }

    /**
     * Propagation de la valeur de v?rit? d'un litt?ral falsifi?
     * 
     * @param s
     *            objet utilis? pour la propagation
     * @param p
     *            le litt?ral propag? (il doit etre falsifie)
     * @return false ssi une inconsistance est d?t?ct?e
     */
    public boolean propagate(UnitPropagationListener s, int p) {

        // On observe toujours tous les litt?raux
        voc.watch(p, this);
        assert !voc.isFalsified(p);

        // Si le litt?ral p est impliqu?
        if (this.watchCumul == this.degree)
            return false;

        // On met en place la mise ? jour du compteur
        voc.undos(p).push(this);
        watchCumul--;

        // Si les litt?raux restant sont impliqu?s
        if (watchCumul == degree) {
            for (int ind = 0; ind < lits.length; ind++) {
                if (voc.isUnassigned(lits[ind])) {
                    if (!s.enqueue(lits[ind], this)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Enl?ve une contrainte du prouveur
     */
    public void remove() {
        for (int i = 0; i < lits.length; i++) {
            voc.watches(lits[i] ^ 1).remove(this);
        }
    }

    /**
     * Permet le r??chantillonage de l'activit? de la contrainte
     * 
     * @param d
     *            facteur d'ajustement
     */
    public void rescaleBy(double d) {
        // TODO Yann rescaleBy
        System.out.println("rescaleBy");
    }

    /**
     * Simplifie la contrainte(l'all?ge)
     * 
     * @return true si la contrainte est satisfaite, false sinon
     */
    public boolean simplify() {

        int i = 0;

        // On esp?re le maximum de la somme
        int curr = watchCumul;

        // Pour chaque litt?ral
        while (i < this.lits.length) {
            // On d?cr?mente si l'espoir n'est pas fond?
            if (voc.isUnassigned(lits[i++])) {
                curr--;
                if (curr < this.degree)
                    return false;
            }
        }

        return false;
    }

    /**
     * Cha?ne repr?sentant la contrainte
     * 
     * @return Cha?ne repr?sentant la contrainte
     */
    @Override
    public String toString() {
        StringBuffer stb = new StringBuffer();

        if (lits.length > 0) {
            if (voc.isUnassigned(lits[0])) {
                stb.append(this.lits[0]);
                stb.append(" ");
            }
            for (int i = 1; i < lits.length; i++) {
                if (voc.isUnassigned(lits[i])) {
                    stb.append(" + ");
                    stb.append(this.lits[i]);
                    stb.append(" ");
                }
            }
            stb.append(">= ");
            stb.append(this.degree);
        }
        return stb.toString();
    }

    /**
     * M?thode appel?e lors du backtrack
     * 
     * @param p
     *            le litt?ral d?saffect?
     */
    public void undo(int p) {
        watchCumul++;
    }

    public void setLearnt() {
        throw new UnsupportedOperationException();
    }

    public void register() {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return lits.length;
    }

    public int get(int i) {
        return lits[i];
    }

    public void assertConstraint(UnitPropagationListener s) {
        throw new UnsupportedOperationException();
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

    public Object clone() {
	// TODO: deep copy
	try {
	    return super.clone();
	} catch (CloneNotSupportedException e) {
	    throw new InternalError(e.toString());
	}
    }
}

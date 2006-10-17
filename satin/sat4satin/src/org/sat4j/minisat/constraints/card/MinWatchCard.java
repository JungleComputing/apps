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
package org.sat4j.minisat.constraints.card;

import java.io.Serializable;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Undoable;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

public class MinWatchCard implements Constr, Undoable, Serializable {

    private static final long serialVersionUID = 1L;

    public static final boolean ATLEAST = true;

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
     * Constructeur de clause accessible par minWatchCard
     * 
     * @param voc
     *            vocabulaire employ? par la contrainte
     * @param ps
     *            vecteur contenant la liste des litt?raux de la contrainte
     * @param moreThan
     *            true si la contrainte est sup?rieure ou ?gale
     * @param degree
     *            le degr? de la contrainte
     */
    private MinWatchCard(ILits voc, IVecInt ps, boolean moreThan, int degree) {
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
     * Met ? jour les affectations
     * 
     * @param voc
     *            vocabulaire employ?
     * @param ps
     *            liste des litt?raux concern?s
     * @return influence des changements sur le degr?
     */
    private static int linearisation(ILits voc, IVecInt ps) {
        // Stockage de l'influence des modifications
        int modif = 0;

        for (int i = 0; i < ps.size();) {
            // on verifie si le litteral est affecte
            if (!voc.isUnassigned(ps.get(i))) {
                // Si le litteral est satisfait,
                // ?a revient ? baisser le degr?
                if (voc.isSatisfied(ps.get(i))) {
                    modif--;
                }
                // dans tous les cas, s'il est assign?,
                // on enleve le ieme litteral
                ps.set(i, ps.last());
                ps.pop();
            } else {
                // on passe au litt?ral suivant
                i++;
            }
        }

        // DLB: inutile?
        // ps.shrinkTo(nbElement);

        return modif;
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
    public static MinWatchCard minWatchCardNew(UnitPropagationListener s,
            ILits voc, IVecInt ps, boolean moreThan, int degree)
            throws ContradictionException {

        degree += linearisation(voc, ps);

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

        // La contrainte est maintenant cr??e
        MinWatchCard retour = new MinWatchCard(voc, ps, moreThan, degree);

        retour.normalize();

        if (degree <= 0)
            return null;

        // On observe degre+1 litt?raux
        int indSwap = retour.lits.length;
        int tmpInt;
        for (int i = 0; i <= retour.degree && i < indSwap; i++) {
            while (voc.isFalsified(retour.lits[i]) && --indSwap <= i) {
                tmpInt = retour.lits[i];
                retour.lits[i] = retour.lits[indSwap];
                retour.lits[indSwap] = tmpInt;
            }

            // Si le litt?ral est observable
            if (!voc.isFalsified(retour.lits[i])) {
                retour.watchCumul++;
                voc.watch(retour.lits[i] ^ 1, retour);
            }
        }

        // Si on observe pas suffisament
        if (retour.watchCumul <= retour.degree) {
            // Si l'on a les litt?rux impliqu?s
            if (retour.watchCumul == retour.degree) {
                for (int i = 0; i < retour.lits.length; i++)
                    if (!s.enqueue(retour.lits[i])) {
                        throw new ContradictionException(
                                "Contradiction avec le litt?ral impliqu?.");
                    }
                return null;
            }
            throw new ContradictionException("Contrainte non-satisfiable");
        }

        return retour;
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
     *            outil pour la propagation des litt?raux
     * @param p
     *            le litt?ral propag?
     * @return false si inconsistance d?t?ct?e, true sinon
     */
    public boolean propagate(UnitPropagationListener s, int p) {

        // Si la contrainte est responsable de propagation unitaire
        if (watchCumul == degree) {
            voc.watch(p, this);
            return false;
        }

        // Recherche du litt?ral falsifi?
        int indFalsified = -1;
        while ((lits[++indFalsified] ^ 1) != p)
            ;

        // Recherche du litt?ral swap
        int indSwap = degree + 1;
        while (indSwap < lits.length && voc.isFalsified(lits[indSwap]))
            indSwap++;

        // Mise ? jour de la contrainte
        if (indSwap == lits.length) {
            // Si aucun litt?ral n'a ?t? trouv?
            voc.watch(p, this);
            // La limite est atteinte
            watchCumul--;
            assert watchCumul == degree;
            voc.undos(p).push(this);

            // On met en queue les litt?raux impliqu?s
            for (int i = 0; i <= degree; i++)
                if ((p != (lits[i] ^ 1)) &&!s.enqueue(lits[i], this))
                    return false;

            return true;
        }
        // Si un litt?ral a ?t? trouv? on les ?change
        int tmpInt = lits[indSwap];
        lits[indSwap] = lits[indFalsified];
        lits[indFalsified] = tmpInt;

        // On observe le nouveau litt?ral
        voc.watch(tmpInt ^ 1, this);

        return true;
    }

    /**
     * Enl?ve une contrainte du prouveur
     */
    public void remove() {
        for (int i = 0; i <= degree; i++) {
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
        // TODO rescaleBy
    }

    /**
     * Simplifie la contrainte
     * 
     * @return true si la contrainte est satisfaite, false sinon
     */
    public boolean simplify() {
        // Calcule de la valeur actuelle
        for (int i = 0, count = 0; i < lits.length; i++)
            if (voc.isSatisfied(lits[i]))
                if (++count == degree)
                    return true;

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
     *            un litt?ral d?saffect?
     */
    public void undo(int p) {
        // Le litt?ral observ? et falsifi? devient non assign?
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

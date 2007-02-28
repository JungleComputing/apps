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

package org.sat4j.minisat.constraints.cnf;

import java.io.Serializable;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVecInt;

/*
 * Created on 16 oct. 2003 To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * Lazy data structure for clause using Watched Literals.
 * 
 * @author leberre
 */
public abstract class WLClause implements Constr, Serializable {

    private static final long serialVersionUID = 1L;

    private double activity;

    protected final int[] lits;

    protected final ILits voc;

    /**
     * Creates a new basic clause
     * 
     * @param voc
     *            the vocabulary of the formula
     * @param ps
     *            A VecInt that WILL BE EMPTY after calling that method.
     */
    public WLClause(IVecInt ps, ILits voc) {
        lits = new int[ps.size()];
        ps.moveTo(lits);
        assert ps.size() == 0;
        this.voc = voc;
        activity = 0;
    }

    /**
     * Perform some sanity check before constructing a clause a) if a literal is
     * assigned true, return null (the clause is satisfied) b) if a literal is
     * assigned false, remove it c) if a clause contains a literal and its
     * opposite (tautology) return null d) remove duplicate literals e) if the
     * clause is empty, return null f) if the clause if unit, transmit it to the
     * object responsible for unit propagation
     * 
     * @param ps
     *            the list of literals
     * @param voc
     *            the vocabulary used
     * @param s
     *            the object responsible for unit propagation
     * @return null if the clause should be ignored, the (possibly modified)
     *         list of literals otherwise
     * @throws ContradictionException
     *             if discovered by unit propagation
     */
    public static IVecInt sanityCheck(IVecInt ps, ILits voc,
            UnitPropagationListener s) throws ContradictionException {
        // si un litt???ral de ps est vrai, retourner vrai
        // enlever les litt???raux falsifi???s de ps
        for (int i = 0; i < ps.size();) {
            // on verifie si le litteral est affecte
            if (voc.isUnassigned(ps.get(i))) {
                // on passe au literal suivant
                i++;
            } else {
                // Si le litteral est satisfait, la clause est
                // satisfaite
                if (voc.isSatisfied(ps.get(i))) {
                    // on retourne la clause
                    return null;
                }
                // on enleve le ieme litteral
                ps.delete(i);

            }
        }

        // on trie le vecteur ps
        ps.sortUnique();

        // ???limine les clauses tautologiques
        // deux litt???raux de signe oppos???s apparaissent dans la m???me
        // clause
        for (int i = 0; i < ps.size() - 1; i++) {
            if (ps.get(i) == (ps.get(i + 1) ^ 1)) {
                // la clause est tautologique
                return null;
            }
        }

        if (propagationCheck(ps, s))
            return null;

        return ps;
    }

    /**
     * Check if this clause is null or unit
     * 
     * @param p
     *            the list of literals (supposed to be clean as after a call to
     *            sanityCheck())
     * @param s
     *            the object responsible for unit propagation
     * @return true iff the clause should be ignored (because it's unit)
     * @throws ContradictionException
     *             when detected by unit propagation
     */
    static boolean propagationCheck(IVecInt ps, UnitPropagationListener s)
            throws ContradictionException {
        if (ps.size() == 0) {
            throw new ContradictionException("Creating Empty clause ?"); //$NON-NLS-1$
        } else if (ps.size() == 1) {
            if (!s.enqueue(ps.get(0))) {
                throw new ContradictionException("Contradictory Unit Clauses"); //$NON-NLS-1$
            }
            return true;
        }

        return false;
    }

    /**
     * Creates a brand new clause, presumably from external data. Performs all
     * sanity checks.
     * 
     * @param s
     *            the object responsible for unit propagation
     * @param voc
     *            the vocabulary
     * @param literals
     *            the literals to store in the clause
     * @return the created clause or null if the clause should be ignored
     *         (tautology for example)
     */
    public static WLClause brandNewClause(UnitPropagationListener s, ILits voc,
            IVecInt literals) {
        WLClause c = new DefaultWLClause(literals, voc);
        c.register();
        return c;
    }

    /*
     * (non-Javadoc)
     * 
     * @see Constr#calcReason(Solver, Lit, Vec)
     */
    public void calcReason(int p, IVecInt outReason) {
        assert outReason.size() == 0
                && ((p == ILits.UNDEFINED) || (p == lits[0]));
        final int[] mylits = lits;
        for (int i = (p == ILits.UNDEFINED) ? 0 : 1; i < mylits.length; i++) {
            assert voc.isFalsified(mylits[i]);
            outReason.push(mylits[i] ^ 1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see Constr#remove(Solver)
     */
    public void remove() {
        voc.watches(lits[0] ^ 1).remove(this);
        voc.watches(lits[1] ^ 1).remove(this);
        // la clause peut etre effacee
    }

    /*
     * (non-Javadoc)
     * 
     * @see Constr#simplify(Solver)
     */
    public boolean simplify() {
        for (int i = 0; i < lits.length; i++) {
            if (voc.isSatisfied(lits[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean propagate(UnitPropagationListener s, int p) {
        final int[] mylits = lits;
        // Lits[1] doit contenir le litt???ral falsifi???
        if (mylits[0] == (p ^ 1)) {
            mylits[0] = mylits[1];
            mylits[1] = p ^ 1;
        }
        assert mylits[1] == (p ^ 1);
        // // Si le premier litt???ral est satisfait, la clause est satisfaite
        // The solver appears to solve more benchmarks (while being sometimes
        // slower)
        // if commenting those lines.
        // if (voc.isSatisfied(lits[0])) {
        // // reinsert la clause dans la liste des clauses surveillees
        // voc.watch(p, this);
        // return true;
        // }

        // Recherche un nouveau litt???ral ??? regarder
        for (int i = 2; i < mylits.length; i++) {
            if (!voc.isFalsified(mylits[i])) {
                mylits[1] = mylits[i];
                mylits[i] = p ^ 1;
                voc.watch(mylits[1] ^ 1, this);
                return true;
            }
        }
        assert voc.isFalsified(mylits[1]);
        // La clause est unitaire ou nulle
        voc.watch(p, this);
        // avance pour la propagation
        return s.enqueue(mylits[0], this);
    }

    /*
     * For learnt clauses only @author leberre
     */
    public boolean locked() {
        return voc.getReason(lits[0]) == this;
    }

    /**
     * @return the activity of the clause
     */
    public double getActivity() {
        return activity;
    }

    @Override
    public String toString() {
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < lits.length; i++) {
            stb.append(Lits.toString(lits[i]));
            stb.append("["); //$NON-NLS-1$
            stb.append(voc.valueToString(lits[i]));
            stb.append("]"); //$NON-NLS-1$
            stb.append(" "); //$NON-NLS-1$
        }
        return stb.toString();
    }

    /**
     * Retourne le ieme literal de la clause. Attention, cet ordre change durant
     * la recherche.
     * 
     * @param i
     *            the index of the literal
     * @return the literal
     */
    public int get(int i) {
        return lits[i];
    }

    /**
     * @param claInc
     */
    public void incActivity(double claInc) {
        activity += claInc;
    }

    /**
     * @param d
     */
    public void rescaleBy(double d) {
        activity *= d;
    }

    public int size() {
        return lits.length;
    }

    public void assertConstraint(UnitPropagationListener s) {
        boolean ret = s.enqueue(lits[0], this);
        assert ret;
    }

    public ILits getVocabulary() {
        return voc;
    }

    public int[] getLits() {
        int[] tmp = new int[size()];
        System.arraycopy(lits, 0, tmp, 0, size());
        return tmp;
    }

}
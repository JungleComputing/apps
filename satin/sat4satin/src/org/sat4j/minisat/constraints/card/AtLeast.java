/*
 * SAT4J: a SATisfiability library for Java   
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

/*
 * Created on 8 janv. 2004 To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author leberre Contrainte de cardinalit?
 */
public class AtLeast implements Constr, Undoable, Serializable {

    private static final long serialVersionUID = 1L;

    /** number of allowed falsified literal */
    private int n;

    /** current number of falsified literals */
    private int counter;

    /**
     * constraint literals
     */
    private final int[] lits;

    // private final ILits voc;
    private ILits voc;

    private long status = 0L;

    /**
     * @param ps
     *            a vector of literals
     * @param n
     *            the minimal number of satisfied literals
     */
    private AtLeast(ILits voc, IVecInt ps, int n) {
        this.n = ps.size() - n;
        this.voc = voc;
        counter = 0;
        lits = new int[ps.size()];
        ps.moveTo(lits);
        for (int q : lits) {
            voc.watch(q ^ 1, this);
        }
    }

    public static AtLeast atLeastNew(UnitPropagationListener s, ILits voc,
        IVecInt ps, int n) throws ContradictionException {
        if (ps.size() < n) {
            throw new ContradictionException();
        }
        int degree = n;
        for (int i = 0; i < ps.size();) {
            // on verifie si le litteral est affecte
            if (!voc.isUnassigned(ps.get(i))) {
                // Si le litteral est satisfait,
                // ?a revient ? baisser le degr?
                if (voc.isSatisfied(ps.get(i))) {
                    degree--;
                }
                // dans tous les cas, s'il est assign?,
                // on enleve le ieme litteral
                ps.delete(i);
            } else {
                // on passe au litt?ral suivant
                i++;
            }
        }

        // on trie le vecteur ps
        ps.sortUnique();

        // ?limine les clauses tautologiques
        // deux litt?raux de signe oppos?s apparaissent dans la m?me
        // clause
        for (int i = 0; i < ps.size() - 1;) {
            if (ps.get(i) == (ps.get(i + 1) ^ 1)) {
                // la clause est tautologique
                // TODO
                // ps.set(i, ps.last());
                // ps.pop();
                // ps.set(i, ps.last());
                // ps.pop();
                // degree--;
                i++;
            } else {
                i++;
            }
        }

        if (ps.size() == degree) {
            for (int i = 0; i < ps.size(); i++) {
                if (!s.enqueue(ps.get(i))) {
                    throw new ContradictionException();
                }
            }
            return null;
        }

        if (ps.size() < degree) {
            throw new ContradictionException();
        }
        if (degree == 0) {
            return null;
        }

        return new AtLeast(voc, ps, degree);
    }

    /*
     * (non-Javadoc)
     * 
     * @see Constr#remove(Solver)
     */
    public void remove() {
        for (int q : lits) {
            voc.watches(q ^ 1).remove(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see Constr#propagate(Solver, Lit)
     */
    public boolean propagate(UnitPropagationListener s, int p) {
        // remet la clause dans la liste des clauses regardees
        voc.watch(p, this);

        if (counter == n) {
            return false;
        }

        counter++;
        voc.undos(p).push(this);

        // If no more can be false, enqueue the rest:
        if (counter == n) {
            for (int q : lits) {
                if (voc.isUnassigned(q)) {
                    if (!s.enqueue(q, this)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see Constr#simplify(Solver)
     */
    public boolean simplify() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see Constr#undo(Solver, Lit)
     */
    public void undo(int p) {
        counter--;
    }

    /*
     * (non-Javadoc)
     * 
     * @see Constr#calcReason(Solver, Lit, Vec)
     */
    public void calcReason(int p, IVecInt outReason) {
        for (int q : lits) {
            if (voc.isFalsified(q)) {
                outReason.push(q ^ 1);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.datatype.Constr#learnt()
     */
    public boolean learnt() {
        // Ces contraintes ne sont pas apprises pour le moment.
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.datatype.Constr#getActivity()
     */
    public double getActivity() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.datatype.Constr#incActivity(double)
     */
    public void incActivity(double claInc) {
        // TODO Auto-generated method stub

    }

    /*
     * For learnt clauses only @author leberre
     */
    public boolean locked() {
        // FIXME need to be adapted to AtLeast
        // return lits[0].getReason() == this;
        return true;
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

    public void rescaleBy(double d) {
        throw new UnsupportedOperationException();
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

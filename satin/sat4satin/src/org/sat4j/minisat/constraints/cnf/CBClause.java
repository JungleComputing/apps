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
import org.sat4j.minisat.core.Undoable;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre
 */
public class CBClause implements Constr, Undoable, Serializable {

    private static final long serialVersionUID = 1L;

    protected int falsified;

    private boolean learnt;

    protected final int[] lits;

    protected final ILits voc;

    private double activity;

    public static CBClause brandNewClause(UnitPropagationListener s, ILits voc,
            IVecInt literals) {
        CBClause c = new CBClause(literals, voc);
        c.register();
        return c;
    }

    /**
     * 
     */
    public CBClause(IVecInt ps, ILits voc, boolean learnt) {
        this.learnt = learnt;
        this.lits = new int[ps.size()];
        this.voc = voc;
        ps.moveTo(this.lits);
	// SATIN:
	this.learntGlobal = false;
    }

    public CBClause(IVecInt ps, ILits voc) {
        this(ps, voc, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#remove()
     */
    public void remove() {
        for (int i = 0; i < lits.length; i++) {
            voc.watches(lits[i] ^ 1).remove(this);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#propagate(org.sat4j.minisat.core.UnitPropagationListener,
     *      int)
     */
    public boolean propagate(UnitPropagationListener s, int p) {
        voc.undos(p).push(this);
        falsified++;
        assert falsified != lits.length;
        if (falsified == lits.length - 1) {
            // find unassigned literal
            for (int i = 0; i < lits.length; i++) {
                if (!voc.isFalsified(lits[i])) {
                    return s.enqueue(lits[i], this);
                }
            }
            // one of the variable in to be propagated to false.
            // (which explains why the falsified counter has not
            // been increased yet)
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#simplify()
     */
    public boolean simplify() {
        for (int p : lits) {
            if (voc.isSatisfied(p)) {
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#undo(int)
     */
    public void undo(int p) {
        falsified--;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#calcReason(int,
     *      org.sat4j.specs.VecInt)
     */
    public void calcReason(int p, IVecInt outReason) {
        assert outReason.size() == 0;
        for (int q : lits) {
            assert voc.isFalsified(q) || q == p;
            if (voc.isFalsified(q)) {
                outReason.push(q ^ 1);
            }
        }
        assert (p == ILits.UNDEFINED) || (outReason.size() == lits.length - 1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#learnt()
     */
    public boolean learnt() {
        return learnt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#incActivity(double)
     */
    public void incActivity(double claInc) {
        activity += claInc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#getActivity()
     */
    public double getActivity() {
        return activity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#locked()
     */
    public boolean locked() {
        return voc.getReason(lits[0]) == this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#setLearnt()
     */
    public void setLearnt() {
        learnt = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#register()
     */
    public void register() {
        for (int p : lits) {
            voc.watch(p ^ 1, this);
        }
        if (learnt) {
            for (int p : lits) {
                if (voc.isFalsified(p)) {
                    voc.undos(p ^ 1).push(this);
                    falsified++;
                }
            }
            assert falsified == lits.length - 1;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#rescaleBy(double)
     */
    public void rescaleBy(double d) {
        activity *= d;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#size()
     */
    public int size() {
        return lits.length;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#get(int)
     */
    public int get(int i) {
        return lits[i];
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Constr#assertConstraint(org.sat4j.minisat.core.UnitPropagationListener)
     */
    public void assertConstraint(UnitPropagationListener s) {
        assert voc.isUnassigned(lits[0]);
        boolean ret = s.enqueue(lits[0], this);
        assert ret;
    }

    @Override
    public String toString() {
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < lits.length; i++) {
            stb.append(lits[i]);
            stb.append("["); //$NON-NLS-1$
            stb.append(voc.valueToString(lits[i]));
            stb.append("]"); //$NON-NLS-1$
            stb.append(" "); //$NON-NLS-1$
        }
        return stb.toString();
    }

    // SATIN
    private boolean learntGlobal;

    public void setLearntGlobal() {
	learntGlobal = true;
    }

    public boolean learntGlobal() {
        return learntGlobal;
    }
}

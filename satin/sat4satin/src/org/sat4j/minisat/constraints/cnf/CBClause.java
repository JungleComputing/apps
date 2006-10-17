/*
 * Created on 4 juil. 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
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

    private int falsified;

    private boolean learnt;

    private final int[] lits;

    // protected final ILits voc;
    protected ILits voc;

    private double activity;

    private long status = 0L;

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
        for (int i = 0; i < lits.length; i++) {
            if (voc.isSatisfied(lits[i])) {
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
        for (int i = 0; i < lits.length; i++) {
            assert voc.isFalsified(lits[i]) || lits[i] == p;
            if (voc.isFalsified(lits[i])) {
                outReason.push(lits[i] ^ 1);
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
        for (int i = 0; i < lits.length; i++) {
            voc.watch(lits[i] ^ 1, this);
        }
        if (learnt) {
            for (int i = 0; i < lits.length; i++) {
                if (voc.isFalsified(lits[i])) {
                    voc.undos(lits[i] ^ 1).push(this);
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
            stb.append(" ");
            stb.append("[");
            stb.append(voc.valueToString(lits[i]));
            stb.append("]");
        }
        return stb.toString();
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

/*
 * Created on 25 mai 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sat4j.minisat.constraints.cnf;

import java.io.Serializable;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BinaryClauses implements Constr, Cloneable, Serializable {

    private static final long serialVersionUID = 1L;

    // private final ILits voc;
    private ILits voc;

    // private final IVecInt clauses = new VecInt();
    private IVecInt clauses = new VecInt();

    private final int reason;

    private int conflictindex = -1;

    private long status = 0L;

    /**
     * 
     */
    public BinaryClauses(ILits voc, int p) {
        this.voc = voc;
        this.reason = p;
    }

    public void addBinaryClause(int p) {
        clauses.push(p);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#remove()
     */
    public void remove() {
        // do nothing
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#propagate(org.sat4j.minisat.UnitPropagationListener,
     *      int)
     */
    public boolean propagate(UnitPropagationListener s, int p) {
        assert voc.isFalsified(this.reason);
        voc.watch(p, this);
        for (int i = 0; i < clauses.size(); i++) {
            int q = clauses.get(i);
            if (!s.enqueue(q,this)) {
                conflictindex = i;
                return false;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#simplify()
     */
    public boolean simplify() {
	System.out.println("simplify BC:" + this);
        for (int i = 0; i < clauses.size(); i++) {
            if (voc.isSatisfied(clauses.get(i))) {
		System.out.println("satisfied " + clauses.get(i));
                return true;
            }
            if (voc.isFalsified(clauses.get(i))) {
		System.out.println("delete falsified " + clauses.get(i));
                clauses.delete(i);
            }

        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#undo(int)
     */
    public void undo(int p) {
        // no to do
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#calcReason(int, org.sat4j.datatype.VecInt)
     */
    public void calcReason(int p, IVecInt outReason) {
        outReason.push(this.reason ^ 1);
        if (p == ILits.UNDEFINED) {
            // int i=0;
            // while(!voc.isFalsified(clauses.get(i))) {
            // i++;
            // }
            assert conflictindex > -1;
            outReason.push(clauses.get(conflictindex) ^ 1);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#learnt()
     */
    public boolean learnt() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#incActivity(double)
     */
    public void incActivity(double claInc) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#getActivity()
     */
    public double getActivity() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#locked()
     */
    public boolean locked() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#setLearnt()
     */
    public void setLearnt() {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#register()
     */
    public void register() {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#rescaleBy(double)
     */
    public void rescaleBy(double d) {
        // TODO Auto-generated method stub
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#size()
     */
    public int size() {
        return clauses.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#get(int)
     */
    public int get(int i) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public void assertConstraint(UnitPropagationListener s) {
        throw new UnsupportedOperationException();
    }

    final boolean debug = false;

    public void setVoc(ILits newvoc) {
	if (debug) {
	    System.out.println("BC setVoc " + this.toString() + ": " + newvoc);
	}
        voc = newvoc;
    }

    public void updateVoc(ILits newvoc) {
	if (debug) {
	    System.out.println("BC updateVoc " + this.toString() +
			       ": " + newvoc);
	}
        voc = newvoc;
    }

    @Override
    public String toString() {
        StringBuffer stb = new StringBuffer();

	stb.append("BC:");
        for (int i = 0; i < clauses.size() - 1; i++) {
            stb.append(clauses.get(i));
            stb.append(",");
        }
        if (clauses.size() > 0) {
            stb.append(clauses.get(clauses.size() - 1));
        }
        return stb.toString();
    }

    public void setStatus(long st) {
	status = st;
    }

    public long getStatus() {
        return status;
    }

    @Override
    public Object clone() {
        BinaryClauses clone;

	try {
	    clone = (BinaryClauses) super.clone();
	}
	catch (CloneNotSupportedException e) {
	    throw new InternalError(e.toString());
	}

	clone.clauses = (VecInt) clone.clauses.clone();

	return clone;
    }
}

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

import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BinaryClauses implements Constr, Serializable {

    private static final long serialVersionUID = 1L;

    private final ILits voc;

    private final IVecInt clauses = new VecInt();

    private final int reason;

    private int conflictindex = -1;

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
            if (!s.enqueue(q, this)) {
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
        IVecInt locclauses = clauses;
        final int size = clauses.size();
        for (int i = 0; i < size; i++) {
            if (voc.isSatisfied(locclauses.get(i))) {
                return true;
            }
            if (voc.isFalsified(locclauses.get(i))) {
                locclauses.delete(i);
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

    // SATIN
    public void setLearntGlobal() {
	// TODO
    }

    public boolean learntGlobal() {
        return false;
    }
}

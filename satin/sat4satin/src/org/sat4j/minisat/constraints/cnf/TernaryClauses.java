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
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TernaryClauses implements Constr, Serializable {

    private static final long serialVersionUID = 1L;

    private final IVecInt stubs = new VecInt();

    private final ILits voc;

    private final int phead;

    public TernaryClauses(ILits voc, int p) {
        this.voc = voc;
        this.phead = p;
    }

    public void addTernaryClause(int a, int b) {
        stubs.push(a);
        stubs.push(b);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#remove()
     */
    public void remove() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#propagate(org.sat4j.minisat.UnitPropagationListener,
     *      int)
     */
    public boolean propagate(UnitPropagationListener s, int p) {
        assert voc.isSatisfied(p);
        assert voc.isFalsified(phead);
        voc.watch(p, this);
        for (int i = 0; i < stubs.size(); i += 2) {
            int a = stubs.get(i);
            int b = stubs.get(i + 1);
            if (voc.isSatisfied(a) || voc.isSatisfied(b)) {
                continue;
            }
            if (voc.isFalsified(a) && !s.enqueue(b, this)) {
                return false;
            } else if (voc.isFalsified(b) && !s.enqueue(a, this)) {
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
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#undo(int)
     */
    public void undo(int p) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#calcReason(int, org.sat4j.datatype.VecInt)
     */
    public void calcReason(int p, IVecInt outReason) {
        assert voc.isFalsified(this.phead);
        if (p == ILits.UNDEFINED) {
            int i = 0;
            while (!voc.isFalsified(stubs.get(i))
                    || !voc.isFalsified(stubs.get(i + 1))) {
                i += 2;
            }
            outReason.push(this.phead ^ 1);
            outReason.push(stubs.get(i) ^ 1);
            outReason.push(stubs.get(i + 1) ^ 1);
        } else {
            outReason.push(this.phead ^ 1);
            int i = 0;
            while ((stubs.get(i) != p) || (!voc.isFalsified(stubs.get(i ^ 1)))) {
                i++;
            }
            assert !voc.isFalsified(stubs.get(i));
            outReason.push(stubs.get(i ^ 1) ^ 1);
            assert voc.isFalsified(stubs.get(i ^ 1));
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
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#getActivity()
     */
    public double getActivity() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#locked()
     */
    public boolean locked() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#setLearnt()
     */
    public void setLearnt() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#register()
     */
    public void register() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#rescaleBy(double)
     */
    public void rescaleBy(double d) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#size()
     */
    public int size() {
        return stubs.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.Constr#get(int)
     */
    public int get(int i) {
        throw new UnsupportedOperationException();
    }

    public void assertConstraint(UnitPropagationListener s) {
        throw new UnsupportedOperationException();
    }

    // SATIN
    public void setLearntGlobal() {
    }

    public boolean learntGlobal() {
        return false;
    }
}

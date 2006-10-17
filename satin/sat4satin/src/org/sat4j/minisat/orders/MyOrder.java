/*
 * Created on 6 juin 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sat4j.minisat.orders;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.ILits2;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MyOrder extends VarOrder implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private ILits2 lits;

    class Temp implements Comparable<Temp> {
        private int id;

        private int count;

        Temp(int id) {
            this.id = id;
            count = lits.nBinaryClauses(id) + lits.nBinaryClauses(id ^ 1);
        }

        public int compareTo(Temp t) {
            if (count == 0) {
                return Integer.MAX_VALUE;
            }
            if (t.count == 0) {
                return -1;
            }
            return count - t.count;
        }

        @Override
        public String toString() {
            return "" + id + "(" + count + ")";
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.VarOrder#setLits(org.sat4j.minisat.Lits)
     */
    @Override
    public void setLits(ILits lits) {
        super.setLits(lits);
        this.lits = (ILits2) lits;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.IHeuristics#init()
     */
    @Override
    public void init() {
        super.init();
        ArrayList<Temp> v = new ArrayList<Temp>(order.length);

        for (int i = 1; i < order.length; i++) {
            Temp t = new Temp(order[i]);
            v.add(t);
        }
        Collections.sort(v);
        // System.out.println(v);
        for (int i = 0; i < v.size(); i++) {
            Temp t = v.get(i);
            order[i + 1] = t.id;
            int index = t.id >> 1;
            varpos[index] = i + 1;
        }
        lastVar = 1;
    }
    
    @Override
    public String toString() {
        return "Init VSIDS order using a POSIT-like static order on 2 and 3 clauses."; 
    }

    @Override
    public Object clone() {
	MyOrder clone;

	// try {
	    clone = (MyOrder) super.clone();
	// }
	// catch (CloneNotSupportedException e) {
	//    throw new InternalError(e.toString());
	// }

	clone.lits = (ILits2) this.lits.clone();

	return clone;
    }
}

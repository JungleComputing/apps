/*
 * Created on 13 janv. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.sat4j.minisat.orders;

import java.io.Serializable;

/**
 * @author leberre TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public class PureOrder extends VarOrder implements Serializable, Cloneable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    private final int period;

    private int cpt;

    public PureOrder() {
        this(20);
    }

    public PureOrder(int p) {
        period = p;
        cpt = period;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.VarOrder#select()
     */
    @Override
    public int select() {
        // wait period branching
        if (cpt < period) {
            cpt++;
        } else {
            // try to find a pure literal
            cpt = 0;
            int nblits = 2 * lits.nVars();
            for (int i = 1; i <= nblits; i++) {
                if (lits.isUnassigned(i) && lits.watches(i).size() > 0
                        && lits.watches(i ^ 1).size() == 0) {
                    return i;
                }
            }
        }
        // not found: using normal order
        return super.select();
    }
    
    @Override
    public String toString() {
        return "tries to first branch on a single phase watched unassigned variable (pure literal if using a CB data structure) else VSIDS from MiniSAT"; 
    }

    @Override
    public Object clone() {
	PureOrder clone;

	// try {
	    clone = (PureOrder) super.clone();
	// }
	// catch (CloneNotSupportedException e) {
	//    throw new InternalError(e.toString());
	// }

	return clone;
    }
}

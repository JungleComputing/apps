/*
 * Created on 15 juin 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.constraints.cnf;

import org.sat4j.core.Vec;
import org.sat4j.minisat.core.ILits2;
import org.sat4j.minisat.core.Propagatable;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Lits2 extends Lits implements ILits2, Cloneable {

    private static final long serialVersionUID = 1L;

    private BinaryClauses[] binclauses = null;

    /**
     * 
     */
    public Lits2() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * To know the number of binary clauses in which the literal occurs. Please
     * note that this method should only be used in conjunction with the
     * BinaryClauses data structure.
     * 
     * @param p
     * @return the number of binary clauses in which the literal occurs.
     */
    public int nBinaryClauses(int p) {
        if (binclauses == null) {
            return 0;
        }
        if (binclauses[p] == null) {
            return 0;
        }
        return binclauses[p].size();
    }

    public void binaryClauses(int lit1, int lit2) {
        register(lit1, lit2);
        register(lit2, lit1);
    }

    private void register(int p, int q) {
        if (binclauses == null) {
            binclauses = new BinaryClauses[2 * nVars() + 2];
        }
        if (binclauses[p] == null) {
            binclauses[p] = new BinaryClauses(this, p);
	    if (watches[p ^ 1] == null) {
		// created lazily after clone
		watches[p ^ 1] = new Vec<Propagatable>();
	    }
            watches[p ^ 1].insertFirstWithShifting(binclauses[p]);
        }
        binclauses[p].addBinaryClause(q);
    }

    @Override
    public Object clone() {
	final boolean debug = false;
	Lits2 clone;

	clone = (Lits2) super.clone();

	if (debug) {
	    System.out.println("Lits2 clone " + clone +
			       " orig super " + super.toString() +
			       ", orig " + this +
			       " watches clone " + clone.watches +
			       " orig " + this.watches);
	}

	if (this.binclauses != null) {
	    clone.binclauses = new BinaryClauses[2 * nVars() + 2];
	    if (debug) {
		System.out.println("Lits2: already binclauses " +
				   this.binclauses +
				   " cloned: " + clone.binclauses);
	    }

	    for (int p = 0; p < clone.binclauses.length; p++) {
		if (this.binclauses[p] != null) {
		    clone.binclauses[p] =
			(BinaryClauses) binclauses[p].clone();
		    clone.binclauses[p].updateVoc(clone);
		    if (clone.watches[p ^ 1] == null) {
			// created lazily after clone
			clone.watches[p ^ 1] = new Vec<Propagatable>();
		    }
		    clone.watches[p ^ 1].
			insertFirstWithShifting(clone.binclauses[p]);

		    if (debug) {
			System.out.println("now " + clone.watches[p ^ 1].size()
					   + " watches for bin-clause lit " + p
					   + " at " + clone.binclauses[p] + 
					   " was " + this.binclauses[p]);
		    }
		}
	    }
	}

	return clone;
    }
}

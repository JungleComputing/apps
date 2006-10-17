/*
 * Created on 15 juin 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.constraints.cnf;

import org.sat4j.minisat.core.ILits23;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Lits23 extends Lits2 implements ILits23 {

    private static final long serialVersionUID = 1L;

    private TernaryClauses[] ternclauses = null;

    /**
     * 
     */
    public Lits23() {
        super();
    }

    private void register(int p, int q, int r) {
        assert p > 1;
        assert q > 1;
        assert r > 1;

        if (ternclauses == null) {
            ternclauses = new TernaryClauses[2 * nVars() + 2];
        }
        if (ternclauses[p] == null) {
            ternclauses[p] = new TernaryClauses(this, p);
            watches[p ^ 1].push(ternclauses[p]);
        }
        ternclauses[p].addTernaryClause(q, r);
    }

    public void ternaryClauses(int lit1, int lit2, int lit3) {
        register(lit1, lit2, lit3);
        register(lit2, lit1, lit3);
        register(lit3, lit1, lit2);
    }

    public int nTernaryClauses(int p) {
        if (ternclauses == null) {
            return 0;
        }
        if (ternclauses[p] == null) {
            return 0;
        }
        return ternclauses[p].size();
    }

    public Object clone() {
	Lits23 clone;

	clone = (Lits23) super.clone();
	if (clone.ternclauses != null) {
	    clone.ternclauses = (TernaryClauses[]) ternclauses.clone();
	}

	return clone;
    }
}

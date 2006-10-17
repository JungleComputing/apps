/*
 * Cree le 4 fevr. 2005
 *
 */
package org.sat4j.minisat.constraints.cnf;

import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.IVecInt;

/**
 * Counter Based clauses that can be mixed with WLCLauses
 * 
 * @author leberre
 */
public class MixableCBClause extends CBClause {

    /**
     * Commentaire pour <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param ps
     * @param voc
     */
    public MixableCBClause(IVecInt ps, ILits voc) {
        super(ps, voc);
        // TODO Raccord de constructeur auto-genere
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.Propagatable#propagate(org.sat4j.minisat.core.UnitPropagationListener,
     *      int)
     */
    @Override
    public boolean propagate(UnitPropagationListener s, int p) {
        voc.watch(p, this);
        return super.propagate(s, p);
    }

    /**
     * @param ps
     * @param voc
     * @param learnt
     */
    public MixableCBClause(IVecInt ps, ILits voc, boolean learnt) {
        super(ps, voc, learnt);
        // TODO Raccord de constructeur auto-genere
    }

    public static CBClause brandNewClause(UnitPropagationListener s, ILits voc,
            IVecInt literals) {
        CBClause c = new MixableCBClause(literals, voc);
        c.register();
        return c;
    }
}

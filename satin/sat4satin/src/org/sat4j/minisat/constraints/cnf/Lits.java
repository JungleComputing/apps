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

import org.sat4j.core.Vec;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Propagatable;
import org.sat4j.minisat.core.Undoable;
import org.sat4j.specs.IVec;

/**
 * @author laihem
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Lits implements Serializable, ILits {

    private static final long serialVersionUID = 1L;

    private boolean pool[] = new boolean[1];

    private int realnVars = 0;

    @SuppressWarnings("unchecked")
    protected IVec<Propagatable>[] watches = new IVec[0];

    private int[] level = new int[0];

    private Constr[] reason = new Constr[0];

    @SuppressWarnings("unchecked")
    private IVec<Undoable>[] undos = new IVec[0];

    private boolean[] falsified = new boolean[0];

    public Lits() {
    }

    @SuppressWarnings( { "unchecked" })
    public void init(int nvar) {
        assert nvar >= 0;
        // let some space for unused 0 indexer.
        int nvars = nvar + 1;
        boolean[] npool = new boolean[nvars];
        System.arraycopy(pool, 0, npool, 0, pool.length);
        pool = npool;

        level = new int[nvars];
        int[] nlevel = new int[nvars];
        System.arraycopy(level, 0, nlevel, 0, level.length);
        level = nlevel;

        IVec<Propagatable>[] nwatches = new IVec[2 * nvars];
        System.arraycopy(watches, 0, nwatches, 0, watches.length);
        watches = nwatches;

        IVec<Undoable>[] nundos = new IVec[nvars];
        System.arraycopy(undos, 0, nundos, 0, undos.length);
        undos = nundos;

        Constr[] nreason = new Constr[nvars];
        System.arraycopy(reason, 0, nreason, 0, reason.length);
        reason = nreason;

        boolean[] newFalsified = new boolean[2 * nvars];
        System.arraycopy(falsified, 0, newFalsified, 0, falsified.length);
        falsified = newFalsified;
    }

    public int getFromPool(int x) {
        int var = Math.abs(x);
        assert var < pool.length;

        int lit = ((x < 0) ? (var << 1) ^ 1 : (var << 1));
        assert lit > 1;
        if (!pool[var]) {
            realnVars++;
            pool[var] = true;
            watches[var << 1] = new Vec<Propagatable>();
            watches[(var << 1) | 1] = new Vec<Propagatable>();
            undos[var] = new Vec<Undoable>();
            level[var] = -1;
            falsified[var << 1] = false; // because truthValue[var] is
                                            // UNDEFINED
            falsified[var << 1 | 1] = false; // because truthValue[var] is
                                                // UNDEFINED
        }
        return lit;
    }

    public boolean belongsToPool(int x) {
        assert x > 0;
        return pool[x];
    }

    public void resetPool() {
        for (int i = 0; i < pool.length; i++) {
            if (pool[i]) {
                reset(i << 1);
            }
        }
    }

    public void ensurePool(int howmany) {
        init(howmany);
    }

    public void unassign(int lit) {
        assert falsified[lit] || falsified[lit ^ 1];
        falsified[lit] = false;
        falsified[lit ^ 1] = false;
    }

    public void satisfies(int lit) {
        assert !falsified[lit] && !falsified[lit ^ 1];
        falsified[lit] = false;
        falsified[lit ^ 1] = true;
    }

    public boolean isSatisfied(int lit) {
        return falsified[lit ^ 1];
    }

    public final boolean isFalsified(int lit) {
        return falsified[lit];
    }

    public boolean isUnassigned(int lit) {
        return !falsified[lit] && !falsified[lit ^ 1];
    }

    public String valueToString(int lit) {
        if (isUnassigned(lit))
            return "?"; //$NON-NLS-1$
        if (isSatisfied(lit))
            return "T"; //$NON-NLS-1$
        return "F"; //$NON-NLS-1$
    }

    public int nVars() {
        return pool.length - 1;
    }

    public int not(int lit) {
        return lit ^ 1;
    }

    public static String toString(int lit) {
        return ((lit & 1) == 0 ? "" : "-") + (lit >> 1); //$NON-NLS-1$//$NON-NLS-2$
    }

    public void reset(int lit) {
        watches[lit].clear();
        watches[lit ^ 1].clear();
        level[lit >> 1] = -1;
        reason[lit >> 1] = null;
        undos[lit >> 1].clear();
        falsified[lit] = false;
        falsified[lit ^ 1] = false;
    }

    public int getLevel(int lit) {
        return level[lit >> 1];
    }

    public void setLevel(int lit, int l) {
        level[lit >> 1] = l;
    }

    public Constr getReason(int lit) {
        return reason[lit >> 1];
    }

    public void setReason(int lit, Constr r) {
        reason[lit >> 1] = r;
    }

    public IVec<Undoable> undos(int lit) {
        return undos[lit >> 1];
    }

    public void watch(int lit, Propagatable c) {
        watches[lit].push(c);
    }

    public IVec<Propagatable> watches(int lit) {
        return watches[lit];
    }

    public boolean isImplied(int lit) {
        int var = lit >> 1;
        assert reason[var] == null || falsified[lit] || falsified[lit ^ 1];
        // a literal is implied if it is a unit clause, ie
        // propagated without reason at decision level 0.
        return reason[var] != null || level[var] == 0;
    }

    public int realnVars() {
        return realnVars;
    }
}

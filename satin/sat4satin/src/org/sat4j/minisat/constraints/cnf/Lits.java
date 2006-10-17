/*
 * SAT4J: a SATisfiability library for Java   
 * Copyright (C) 2004 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas E?n and Niklas S?rensson.
 * Proceedings of the Sixth International Conference on Theory 
 * and Applications of Satisfiability Testing, LNCS 2919, 
 * pp 502-518, 2003.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 */

package org.sat4j.minisat.constraints.cnf;

import java.io.Serializable;

import org.sat4j.core.Vec;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Lbool;
import org.sat4j.minisat.core.Propagatable;
import org.sat4j.minisat.core.Undoable;
import org.sat4j.specs.IVec;

/**
 * @author laihem
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Lits implements Serializable, Cloneable, ILits {

    private static final long serialVersionUID = 1L;

    private boolean pool[] = new boolean[0];

    private int realnVars = 0;

    @SuppressWarnings("unchecked")
    protected IVec<Propagatable>[] watches = new IVec[0];

    private int[] level = new int[0];

    public Lbool[] truthValue = new Lbool[1];

    private Constr[] reason = new Constr[0];

    @SuppressWarnings("unchecked")
    private IVec<Undoable>[] undos = new IVec[0];

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

        Lbool[] ntruthValue = new Lbool[nvars];
        System.arraycopy(truthValue, 0, ntruthValue, 0, truthValue.length);
        truthValue = ntruthValue;

        IVec<Undoable>[] nundos = new IVec[nvars];
        System.arraycopy(undos, 0, nundos, 0, undos.length);
        undos = nundos;

        Constr[] nreason = new Constr[nvars];
        System.arraycopy(reason, 0, nreason, 0, reason.length);
        reason = nreason;
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
            truthValue[var] = Lbool.UNDEFINED;
            undos[var] = new Vec<Undoable>();
            level[var] = -1;
            // reset(var);

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
                reset(i);
            }
        }
    }

    public void ensurePool(int howmany) {
        init(howmany);
    }

    public void unassign(int lit) {
        assert truthValue[lit >> 1] != Lbool.UNDEFINED;
        truthValue[lit >> 1] = Lbool.UNDEFINED;
    }

    public void satisfies(int lit) {
        assert truthValue[lit >> 1] == Lbool.UNDEFINED;
        truthValue[lit >> 1] = satisfyingValue(lit);
    }

    private static final Lbool satisfyingValue(int lit) {
        return ((lit & 1) == 0) ? Lbool.TRUE : Lbool.FALSE;
    }

    public boolean isSatisfied(int lit) {
        return truthValue[lit >> 1] == satisfyingValue(lit);
    }

    public boolean isFalsified(int lit) {
        Lbool falsified = ((lit & 1) == 0) ? Lbool.FALSE : Lbool.TRUE;
        return truthValue[lit >> 1] == falsified;
    }

    public boolean isUnassigned(int lit) {
        return truthValue[lit >> 1] == Lbool.UNDEFINED;
    }

    public String valueToString(int lit) {
        if (isUnassigned(lit)) {
            return "?";
        }
        if (isSatisfied(lit)) {
            return "T";
        }
        return "F";
    }

    public int nVars() {
        return truthValue.length - 1;
    }

    public int not(int lit) {
        return lit ^ 1;
    }

    public static String toString(int lit) {
        return ((lit & 1) == 0 ? "" : "-") + (lit >> 1);
    }

    public void reset(int lit) {
        watches[lit].clear();
        watches[lit ^ 1].clear();
        level[lit >> 1] = -1;
        truthValue[lit >> 1] = Lbool.UNDEFINED;
        reason[lit >> 1] = null;
        undos[lit >> 1].clear();
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
        if (undos[lit >> 1] == null) {
            // create lazily after clone
            undos[lit >> 1] = new Vec<Undoable>();
        }
        return undos[lit >> 1];
    }

    public void watch(int lit, Propagatable c) {
        if (watches[lit] == null) {
            // create lazily after clone
            watches[lit] = new Vec<Propagatable>();
        }
        watches[lit].push(c);
    }

    public IVec<Propagatable> watches(int lit) {
        if (watches[lit] == null) {
            // create lazily after clone
            watches[lit] = new Vec<Propagatable>();
        }
        return watches[lit];
    }

    public boolean isImplied(int lit) {
        int var = lit >> 1;
        // reason[lit] != null => truthValue[lit]!=Lbool.UNDEFINED
        assert reason[var] == null || truthValue[var] != Lbool.UNDEFINED;
        // a literal is implied if it is a unit clause, ie
        // propagated without reason at decision level 0.
        return reason[var] != null || level[var] == 0;
    }

    public int realnVars() {
        return realnVars;
    }

    @Override
    public Object clone() {
        Lits clone;

        try {
            clone = (Lits) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }

        clone.pool = this.pool.clone();
        clone.level = this.level.clone();
        // clone.truthValue = (Lbool[]) this.truthValue.clone();
        // Actually need a shallow copy of the truthValues themselves,
        // since we don't want multiple TRUE/FALSE/UNDEF objects
        // (which would break some assumptions in other code)
        clone.truthValue = new Lbool[truthValue.length];
        for (int i = 0; i < clone.truthValue.length; i++) {
            clone.truthValue[i] = this.truthValue[i];
        }

        // NOTE: undos should be reconstructed after clone
        // clone.undos = (IVec<Undoable>[]) this.undos.clone();
        clone.undos = new IVec[undos.length];

        // NOTE: reason should be reconstructed after clone
        // clone.reason = (Constr[]) this.reason.clone();
        clone.reason = new Constr[reason.length];

        // NOTE: watches need to be reconstructed after deep clone()
        //clone.watches = (IVec<Propagatable>[]) this.watches.clone();
        clone.watches = new IVec[watches.length];
        // for (int var = 1; var <= clone.realnVars; var++) {
        //     clone.watches[var << 1] = new Vec<Propagatable>();
        //     clone.watches[(var << 1) | 1] = new Vec<Propagatable>();
        // }

        if (false) {
            System.out.println("Lits clone " + clone + ", orig " + this);
        }

        return clone;
    }
}

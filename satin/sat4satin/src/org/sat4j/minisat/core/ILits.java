/*
 * Created on 15 juin 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.core;

import org.sat4j.specs.IVec;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface ILits {

    public static int UNDEFINED = -1;

    public Object clone();

    public abstract void init(int nvar);

    public abstract int getFromPool(int x);

    /**
     * Returns true iff the variable is used in the set of constraints.
     * @param x
     * @return true iff the variable belongs to the formula.
     */
    boolean belongsToPool(int x);
    
    public abstract void resetPool();

    public abstract void ensurePool(int howmany);

    public abstract void unassign(int lit);

    public abstract void satisfies(int lit);

    public abstract boolean isSatisfied(int lit);

    public abstract boolean isFalsified(int lit);

    public abstract boolean isUnassigned(int lit);

    /**
     * @param lit
     * @return true iff the truth value of that literal is due to a unit
     *         propagation or a decision.
     */
    public abstract boolean isImplied(int lit);

    /**
     * to obtain the max id of the variable
     * @return the maximum number of variables in the formula
     */
    public abstract int nVars();

    /**
     * to obtain the real number of variables appearing in the formula
     * @return the number of variables used in the pool
     */
    int realnVars();
    
    public abstract int not(int lit);

    public abstract void reset(int lit);

    public abstract int getLevel(int lit);

    public abstract void setLevel(int lit, int l);

    public abstract Constr getReason(int lit);

    public abstract void setReason(int lit, Constr r);

    public abstract IVec<Undoable> undos(int lit);

    public abstract void watch(int lit, Propagatable c);

    /**
     * @param lit
     *            a literal
     * @return the list of all the constraints that watch the negation of lit
     */
    public abstract IVec<Propagatable> watches(int lit);

    public abstract String valueToString(int lit);
}

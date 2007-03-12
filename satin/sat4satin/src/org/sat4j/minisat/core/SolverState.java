package org.sat4j.minisat.core;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

interface SolverStateInterface extends ibis.satin.WriteMethodsInterface {
    public void setFinished(Lbool status);
    public void reinit(boolean init);
    public void addConflicts(long conflicts);
    public void addLearnts(String key, Vec<VecInt> learnts, long conflicts);
    public void addLearntsIntVecs(String key, int[][] learnts,long conflicts);
    public void removeLearnts(String key);
    public void updateTime(TimeInfo timeinfo);
    public void setAllConstraints(Vec<VecInt> allconstrs);
}

public final class SolverState
    // extends ibis.satin.so.SharedObject
    extends ibis.satin.SharedObject
    implements Serializable, SolverStateInterface
{
    boolean wasSet;
    Lbool status;
    long globalConflicts;
    Hashtable learnedHash;
    Vector timeVec;
    Vec<VecInt> allConstrs;
    int iter;

    public SolverState()
    {
	// Comment by Ceriel:
        // Don't call reinit() here, i.e., a write method in constructor
	// of shared object.  The reason is this causes a broadcast, and
	// this invocation may or may not be processed, depending on wether
	// the destination has seen the object yet.
	doInit(true);
    }

    /* write method */
    public void reinit(boolean init)
    {
        doInit(init);
    }

    private void doInit(boolean init) {
	status = Lbool.UNDEFINED;
	wasSet = false;
	globalConflicts = 0;
	learnedHash = new Hashtable();
	timeVec = new Vector();
	iter++;
	// leave allConstrs alone
    }

    /* write method */
    public void setFinished(Lbool result)
    {
	if (!wasSet || result == Lbool.TRUE) {
	    // A solution found overrides an overstepped search bound
	    status = result;
	    // System.out.println("Finished iter " + iter + ": " + result);
	}
	wasSet = true;
    }

    /* write method */
    public void addConflicts(long conflicts)
    {
	globalConflicts += conflicts;
	// System.out.println("globalConflicts now " + globalConflicts + 
	//		   " added " + conflicts);
    }

    /* write method */
    public void addLearnts(String key, Vec<VecInt> learnts, long conflicts)
    {
	learnedHash.put(key, learnts);
	globalConflicts += conflicts;
	// System.out.println("globalConflicts now " + globalConflicts + 
	//		   " added " + conflicts);
    }

    /* write method */
    public void addLearntsIntVecs(String key, int[][] learnts, long conflicts)
    {
	learnedHash.put(key, learnts);
	globalConflicts += conflicts;
	// System.out.println("globalConflicts now " + globalConflicts + 
	//		   " added " + conflicts);
    }

    /* write method */
    public void removeLearnts(String key)
    {
	learnedHash.remove(key);
    }

    /* write method */
    public void updateTime(TimeInfo timeinfo)
    {
	timeVec.add(timeinfo);
    }

    /* write method */
    public void setAllConstraints(Vec<VecInt> allconstrs)
    {
	allConstrs = allconstrs;
    }

    public boolean updated(int iteration)
    {
	if (iteration == iter) {
	    return wasSet;
	} else {
	    System.out.println("*** shared SolverState at iter " + iter +
				" requested iter " + iteration +
				" (wasSet ignored) ***");
	    return false;
	}
    }

    /* guard for updated() */
    /*
    public boolean guard_updated(int iteration) {
        return iter == iteration;
    }
    */
}

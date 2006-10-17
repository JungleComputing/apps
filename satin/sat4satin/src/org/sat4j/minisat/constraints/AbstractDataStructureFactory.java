/*
 * Created on 13 juin 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sat4j.minisat.constraints;

import java.io.Serializable;
import java.math.BigInteger;

import org.sat4j.core.Vec;
import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.constraints.cnf.WLClause;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.Learner;
import org.sat4j.minisat.core.Propagatable;
import org.sat4j.minisat.core.UnitPropagationListener;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public abstract class AbstractDataStructureFactory implements
        DataStructureFactory, Serializable, Cloneable {

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.DataStructureFactory#conflictDetectedInWatchesFor(int)
     */
    public void conflictDetectedInWatchesFor(int p, int i) {
        for (int j = i + 1; j < tmp.size(); j++) {
            lits.watch(p, tmp.get(j));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.DataStructureFactory#getWatchesFor(int)
     */
    public IVec<Propagatable> getWatchesFor(int p) {
        tmp.clear();
        lits.watches(p).moveTo(tmp);
	if (false) {
	    System.out.println("watches for " + p + ": " +
			       tmp + " len " + tmp.size());
	}
        return tmp;
    }

    protected ILits lits = new Lits();

    // private final IVec<Propagatable> tmp = new Vec<Propagatable>();
    protected IVec<Propagatable> tmp = new Vec<Propagatable>();

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.DataStructureFactory#createVocabulary()
     */
    public ILits getVocabulary() {
	// System.out.println("ADSWB: getVoc: " + lits);
        return lits;
    }

    protected UnitPropagationListener solver;

    protected Learner learner;

    public void setUnitPropagationListener(UnitPropagationListener s) {
        solver = s;
    }

    public void setLearner(Learner learner) {
        this.learner = learner;
    }

    public void reset() {
        WLClause.resetIds();
    }

    public void learnConstraint(Constr constr) {
        learner.learn(constr);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.core.DataStructureFactory#createCardinalityConstraint(org.sat4j.specs.VecInt,
     *      int)
     */
    public Constr createCardinalityConstraint(IVecInt literals, int degree)
            throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.DataStructureFactory#createPseudoBooleanConstraint(org.sat4j.datatype.VecInt,
     *      org.sat4j.datatype.VecInt, int)
     */
    public Constr createPseudoBooleanConstraint(IVecInt literals,
            IVec<BigInteger> coefs, boolean moreThan, BigInteger degree)
            throws ContradictionException {
        throw new UnsupportedOperationException();
    }

    public Constr createUnregisteredPseudoBooleanConstraint(IVecInt literals,
            IVec<BigInteger> coefs, BigInteger degree) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object clone() {
	try {
	    return super.clone();
	} catch (CloneNotSupportedException e) {
	    throw new InternalError(e.toString());
	}
    }
}

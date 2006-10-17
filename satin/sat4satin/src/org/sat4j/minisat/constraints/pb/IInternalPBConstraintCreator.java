/*
 * Created on 2 janv. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.sat4j.minisat.constraints.pb;

import java.math.BigInteger;

import org.sat4j.specs.IConstr;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author leberre TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public interface IInternalPBConstraintCreator {
    public abstract IConstr createUnregisteredPseudoBooleanConstraint(
        IVecInt literals, IVec<BigInteger> coefs, BigInteger degree);
}

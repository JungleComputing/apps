package org.sat4j.reader;

import java.math.BigInteger;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * Abstraction for an Objective Function for Pseudo Boolean Optimization.
 * 
 * May be generalized in the future to deal with other optimization functions.
 * 
 * @author leberre
 *
 */
public class ObjectiveFunction {

    // contains the coeffs of the objective function for each variable
    private final IVec<BigInteger> coeffs;

    private final IVecInt vars;

    public ObjectiveFunction(IVecInt vars, IVec<BigInteger> coeffs) {
        this.vars = new VecInt(vars.size());
        vars.copyTo(this.vars);
        this.coeffs = new Vec<BigInteger>(coeffs.size());
        coeffs.copyTo(this.coeffs);
    }

    // calculate the degree of the objectif function
    public BigInteger calculateDegree(int[] model) {
        BigInteger tempDegree = BigInteger.ZERO;

        for (int i = 0; i < vars.size(); i++) {
            if (varInModel(vars.get(i), model))
                tempDegree = tempDegree.add(coeffs.get(i));
        }
        return tempDegree;
    }

    private boolean varInModel(int var, int[] model) {
        for (int i = 0; i < model.length; i++)
            if (var == model[i])
                return true;
        return false;
    }

    public IVec<BigInteger> getCoeffs() {
        IVec<BigInteger> coefbis = new Vec<BigInteger>(coeffs.size());
        coeffs.copyTo(coefbis);
        return coefbis;
    }

    public IVecInt getVars() {
        IVecInt varbis = new VecInt(vars.size());
        vars.copyTo(varbis);
        return varbis;
    }

}

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
package org.sat4j.reader;

import java.io.Serializable;
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
public class ObjectiveFunction implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

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

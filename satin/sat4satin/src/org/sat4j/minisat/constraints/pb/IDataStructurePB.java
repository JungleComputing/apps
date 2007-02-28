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
package org.sat4j.minisat.constraints.pb;

import java.math.BigInteger;

import org.sat4j.minisat.core.VarActivityListener;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public interface IDataStructurePB {
    BigInteger saturation();

    BigInteger cuttingPlane(PBConstr cpb, BigInteger deg,
            BigInteger[] reducedCoefs, VarActivityListener val);

    BigInteger cuttingPlane(PBConstr cpb, BigInteger degreeCons,
            BigInteger[] reducedCoefs, BigInteger coefMult,
            VarActivityListener val);

    BigInteger cuttingPlane(int[] lits, BigInteger[] reducedCoefs,
            BigInteger deg);

    BigInteger cuttingPlane(int lits[], BigInteger[] reducedCoefs,
            BigInteger degreeCons, BigInteger coefMult);

    void buildConstraintFromConflict(IVecInt resLits, IVec<BigInteger> resCoefs);

    public void buildConstraintFromMapPb(int[] resLits, BigInteger[] resCoefs);

    public BigInteger getDegree();

    public int size();

}

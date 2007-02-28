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
import java.util.HashMap;
import java.util.Map;

import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.VarActivityListener;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author parrain TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public class MapPb implements IDataStructurePB {

    /*
     * During the process of cutting planes, pseudo-boolean constraints are
     * coded with a HashMap <literal, coefficient> and a BigInteger for the
     * degree.
     */
    protected Map<Integer, BigInteger> coefs;

    protected BigInteger degree;

    MapPb(Map<Integer, BigInteger> m, BigInteger d) {
        coefs = m;
        degree = d;
    }

    MapPb(ILits lits) {
        this();
    }

    MapPb() {
        this(new HashMap<Integer, BigInteger>(), BigInteger.ZERO);
    }

    public BigInteger saturation() {
        assert degree.signum() > 0;
        BigInteger minimum = degree;
        for (Map.Entry<Integer, BigInteger> e : coefs.entrySet()) {
            assert e.getValue().signum() > 0;
            // e.setValue(degree.min(e.getValue()));
            if (degree.compareTo(e.getValue()) < 0)
                setCoef(e.getKey(), degree);
            assert e.getValue().signum() > 0;
            minimum = minimum.min(e.getValue());
        }
        // on a appris une clause
        if (minimum.equals(degree) && minimum.compareTo(BigInteger.ONE) > 0) {
            degree = BigInteger.ONE;
            for (Integer i : coefs.keySet())
                // coefs.put(i, BigInteger.ONE);
                setCoef(i, BigInteger.ONE);
        }

        return degree;
    }

    public BigInteger cuttingPlane(PBConstr cpb, BigInteger deg,
            BigInteger[] reducedCoefs, VarActivityListener val) {
        return cuttingPlane(cpb, deg, reducedCoefs, BigInteger.ONE, val);
    }

    public BigInteger cuttingPlane(PBConstr cpb, BigInteger degreeCons,
            BigInteger[] reducedCoefs, BigInteger coefMult,
            VarActivityListener val) {
        degree = degree.add(degreeCons);
        assert degree.signum() > 0;

        if (reducedCoefs == null)
            for (int i = 0; i < cpb.size(); i++) {
                val.varBumpActivity(cpb.get(i));
                cuttingPlaneStep(cpb.get(i), multiplyCoefficient(
                        cpb.getCoef(i), coefMult));
            }
        else
            for (int i = 0; i < cpb.size(); i++) {
                val.varBumpActivity(cpb.get(i));
                cuttingPlaneStep(cpb.get(i), multiplyCoefficient(
                        reducedCoefs[i], coefMult));
            }

        return degree;
    }

    public BigInteger cuttingPlane(int[] lits, BigInteger[] reducedCoefs,
            BigInteger deg) {
        return cuttingPlane(lits, reducedCoefs, deg, BigInteger.ONE);
    }

    public BigInteger cuttingPlane(int lits[], BigInteger[] reducedCoefs,
            BigInteger degreeCons, BigInteger coefMult) {
        degree = degree.add(degreeCons);
        assert degree.signum() > 0;

        for (int i = 0; i < lits.length; i++)
            cuttingPlaneStep(lits[i], reducedCoefs[i].multiply(coefMult));

        return degree;
    }

    private void cuttingPlaneStep(final int lit, final BigInteger coef) {
        assert coef.signum() >= 0;
        Integer blit = Integer.valueOf(lit);
        Integer bnlit = Integer.valueOf(lit ^ 1);
        if (coef.signum() > 0) {
            if (coefs.containsKey(bnlit)) {
                assert !coefs.containsKey(blit);
                if (coefs.get(bnlit).compareTo(coef) < 0) {
                    BigInteger tmp = coefs.get(bnlit);
                    // coefs.put(blit, coef.subtract(tmp));
                    setCoef(blit, coef.subtract(tmp));
                    assert coefs.get(blit).signum() > 0;
                    degree = degree.subtract(tmp);
                    // coefs.remove(bnlit);
                    removeCoef(bnlit);
                } else {
                    if (coefs.get(bnlit).equals(coef)) {
                        degree = degree.subtract(coef);
                        // coefs.remove(bnlit);
                        removeCoef(bnlit);
                    } else {
                        // coefs.put(bnlit, coefs.get(bnlit).subtract(coef));
                        decreaseCoef(bnlit, coef);
                        assert coefs.get(bnlit).signum() > 0;
                        degree = degree.subtract(coef);
                    }
                }
            } else {
                assert (!coefs.containsKey(blit))
                        || (coefs.get(blit).signum() > 0);
                if (coefs.containsKey(blit))
                    increaseCoef(blit, coef);
                else
                    setCoef(blit, coef);
                // coefs.put(blit, (coefs.containsKey(blit) ? coefs.get(blit)
                // : BigInteger.ZERO).add(coef));
                assert coefs.get(blit).signum() > 0;
            }
        }
        assert (!coefs.containsKey(bnlit)) || (!coefs.containsKey(blit));
    }

    public void buildConstraintFromConflict(IVecInt resLits,
            IVec<BigInteger> resCoefs) {
        resLits.clear();
        resCoefs.clear();
        // On recherche tous les litt?raux concern?s
        for (Map.Entry<Integer, BigInteger> e : coefs.entrySet()) {
            resLits.push(e.getKey());
            assert e.getValue().signum() > 0;
            resCoefs.push(e.getValue());
        }
    };

    public void buildConstraintFromMapPb(int[] resLits, BigInteger[] resCoefs) {
        // On recherche tous les litt?raux concern?s
        assert resLits.length == resCoefs.length;
        assert resLits.length == coefs.keySet().size();
        int i = 0;
        for (Map.Entry<Integer, BigInteger> e : coefs.entrySet()) {
            resLits[i] = e.getKey();
            assert e.getValue().signum() > 0;
            resCoefs[i] = e.getValue();
            i++;
        }
    };

    public BigInteger getDegree() {
        return degree;
    }

    public int size() {
        return coefs.keySet().size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        for (Map.Entry<Integer, BigInteger> entry : coefs.entrySet()) {
            stb.append(entry.getValue());
            stb.append(".");
            stb.append(Lits.toString(entry.getKey()));
            stb.append(" ");
        }
        return stb.toString() + " >= " + degree; //$NON-NLS-1$
    }

    private BigInteger multiplyCoefficient(BigInteger coef, BigInteger mult) {
        if (coef.equals(BigInteger.ONE))
            return mult;
        return coef.multiply(mult);
    }

    void increaseCoef(Integer lit, BigInteger incCoef) {
        coefs.put(lit, coefs.get(lit).add(incCoef));
    }

    void decreaseCoef(Integer lit, BigInteger decCoef) {
        coefs.put(lit, coefs.get(lit).subtract(decCoef));
    }

    void setCoef(Integer lit, BigInteger newValue) {
        coefs.put(lit, newValue);
    }

    void removeCoef(Integer lit) {
        coefs.remove(lit);
    }

}

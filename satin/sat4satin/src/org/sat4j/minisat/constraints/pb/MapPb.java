/*
 * Created on Jan 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.sat4j.minisat.constraints.pb;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author parrain TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
class MapPb {

    protected Map<Integer, BigInteger> coefs;

    protected BigInteger degree;

    MapPb(Map<Integer, BigInteger> m, BigInteger d) {
        coefs = m;
        degree = d;
    }

    MapPb() {
        this(new HashMap<Integer, BigInteger>(), BigInteger.ZERO);
    }

    BigInteger saturation() {
        assert degree.signum() > 0;
        BigInteger minimum = degree;
        for (Map.Entry<Integer, BigInteger> e : coefs.entrySet()) {
            assert e.getValue().signum() > 0;
            e.setValue(degree.min(e.getValue()));
            assert e.getValue().signum() > 0;
            minimum = minimum.min(e.getValue());
        }
        // on a appris une clause
        if (minimum.equals(degree) && minimum.compareTo(BigInteger.ONE) > 0) {
            degree = BigInteger.ONE;
            for (Integer i : coefs.keySet()) {
                coefs.put(i, BigInteger.ONE);
            }
        }

        return degree;
    }

    BigInteger addCoeffNewConstraint(int[] lits, BigInteger[] coefsBis,
            BigInteger deg) {
        return addCoeffNewConstraint(lits, coefsBis, deg, BigInteger.ONE);
    }

    BigInteger addCoeffNewConstraint(int[] litsCons, BigInteger[] coefsCons,
            BigInteger degreeCons, BigInteger coefMult) {
        int lit;
        BigInteger coef;
        degree = degree.add(degreeCons);
        assert degree.signum() > 0;
        for (int i = 0; i < litsCons.length; i++) {
            lit = litsCons[i];
            coef = coefsCons[i].multiply(coefMult);
            assert coef.signum() >= 0;
            if (coef.signum() > 0) {
                if (!coefs.containsKey(lit ^ 1)) {
                    assert (!coefs.containsKey(lit))
                            || (coefs.get(lit).signum() > 0);
                    coefs.put(lit, (coefs.containsKey(lit) ? coefs.get(lit)
                            : BigInteger.ZERO).add(coef));
                    assert coefs.get(lit).signum() > 0;
                } else {
                    assert !coefs.containsKey(lit);
                    if (coefs.get(lit ^ 1).compareTo(coef) < 0) {
                        coefs.put(lit, coef.subtract(coefs.get(lit ^ 1)));
                        assert coefs.get(lit).signum() > 0;
                        degree = degree.subtract(coefs.get(lit ^ 1));
                        coefs.remove(lit ^ 1);
                    } else {
                        if (coefs.get(lit ^ 1).equals(coef)) {
                            degree = degree.subtract(coef);
                            coefs.remove(lit ^ 1);
                        } else {
                            coefs.put(lit ^ 1, coefs.get(lit ^ 1)
                                    .subtract(coef));
                            assert coefs.get(lit ^ 1).signum() > 0;
                            degree = degree.subtract(coef);
                        }
                    }
                }
            }
            assert (!coefs.containsKey(lit ^ 1)) || (!coefs.containsKey(lit));
        }
        return degree;
    }

    void buildConstraintFromConflict(IVecInt resLits, IVec<BigInteger> resCoefs) {
        resLits.clear();
        resCoefs.clear();
        // On recherche tous les litt?raux concern?s
        for (Map.Entry<Integer, BigInteger> e : coefs.entrySet()) {
            resLits.push(e.getKey());
            assert e.getValue().signum() > 0;
            resCoefs.push(e.getValue());
        }
    };

    void buildConstraintFromMapPb(int[] resLits, BigInteger[] resCoefs) {
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

    int size() {
        return coefs.keySet().size();
    }

}

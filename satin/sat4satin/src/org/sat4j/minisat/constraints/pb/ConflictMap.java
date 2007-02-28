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

import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.cnf.Lits;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.VarActivityListener;

/**
 * @author parrain TODO To change the template for this generated type comment
 *         go to Window - Preferences - Java - Code Style - Code Templates
 */
public class ConflictMap extends MapPb implements IConflict {

    private final ILits voc;

    /**
     * to store the slack of the current resolvant
     */
    protected BigInteger currentSlack;

    protected int currentLevel;

    /**
     * allows to access directly to all variables belonging to a particular
     * level At index 0, unassigned literals are stored (usually level -1); so
     * there is always a step between index and levels.
     */
    protected VecInt[] byLevel;

    /**
     * constructs the data structure needed to perform cutting planes
     * 
     * @param cpb
     *            pseudo-boolean constraint which rosed the conflict
     * @param level
     *            current decision level
     * @return a conflict on which cutting plane can be performed.
     */
    public static IConflict createConflict(PBConstr cpb, int level) {
        Map<Integer, BigInteger> m = new HashMap<Integer, BigInteger>();
        int lit;
        BigInteger coefLit;
        for (int i = 0; i < cpb.size(); i++) {
            lit = cpb.get(i);
            coefLit = cpb.getCoef(i);
            assert cpb.get(i) != 0;
            assert cpb.getCoef(i).signum() > 0;
            m.put(lit, coefLit);
        }
        return new ConflictMap(m, cpb.getDegree(), cpb.getVocabulary(), level);
    }

    ConflictMap(Map<Integer, BigInteger> m, BigInteger d, ILits voc, int level) {
        super(m, d);
        this.voc = voc;
        this.currentLevel = level;
        initStructures();
    }

    private void initStructures() {
        currentSlack = BigInteger.ZERO;
        byLevel = new VecInt[levelToIndex(currentLevel) + 1];
        int ilit, litLevel, index;
        BigInteger tmp;
        for (Integer lit : coefs.keySet()) {
            ilit = lit.intValue();
            litLevel = voc.getLevel(ilit);
			// eventually add to slack
            tmp = coefs.get(lit);
            if ((tmp.signum() > 0)
                    && (((!voc.isFalsified(ilit)) || litLevel == currentLevel)))
                currentSlack = currentSlack.add(tmp);
			// add to byLevel structure
			index = levelToIndex(litLevel);
			if (byLevel[index] == null) {
				byLevel[index] = new VecInt();
			}
			byLevel[index].push(ilit);
		}
	}

    /**
     * convert level into an index in the byLevel structure
     * 
     * @param level
     * @return
     */
    private static final int levelToIndex(int level) {
        return level + 1;
    }

    /**
     * convert index in the byLevel structure into a level
     * 
     * @param indLevel
     * @return
     */
    private static final int indexToLevel(int indLevel) {
        return indLevel - 1;
    }

    /*
     * coefficient to be computed.
     * 
     */
    protected BigInteger coefMult = BigInteger.ZERO;

    protected BigInteger coefMultCons = BigInteger.ZERO;

    /**
     * computes a cutting plane with a pseudo-boolean constraint. this method
     * updates the current instance (of ConflictMap).
     * 
     * @param cpb
     *            constraint to compute with the cutting plane
     * @param litImplied
     *            literal that must be resolved by the cutting plane
     * @return an update of the degree of the current instance
     */
    public BigInteger resolve(PBConstr cpb, int litImplied,
            VarActivityListener val) {
        assert litImplied > 1;
        int nLitImplied = litImplied ^ 1;
        if (!coefs.containsKey(nLitImplied)) {
			// no resolution
			if (coefs.containsKey(litImplied)){
				// undo operation should be anticipated
				assert byLevel[levelToIndex(voc.getLevel(litImplied))].contains(litImplied);
				byLevel[levelToIndex(voc.getLevel(litImplied))].remove(litImplied);
				if(byLevel[0]==null)
					byLevel[0] = new VecInt();
				byLevel[0].push(litImplied);
				if (voc.isFalsified(litImplied))
					currentSlack = currentSlack.add(coefs.get(litImplied));
			}
            return degree;
        }

        assert slackConflict().signum() <= 0;
        assert degree.signum() >= 0;

        // coefficients of the constraint must be copied in an other structure
        // in order to make reduction operations.
        BigInteger[] coefsCons = null;
        BigInteger degreeCons = cpb.getDegree();

        // search of the index of the implied literal
        int ind = 0;
        while (cpb.get(ind) != litImplied)
            ind++;

        assert cpb.get(ind) == litImplied;
        assert cpb.getCoef(ind) != BigInteger.ZERO;

        if (cpb.getCoef(ind).equals(BigInteger.ONE)) {
            // then we know that the resolvant will still be a conflict (cf.
            // Dixon's property)
            coefMultCons = coefs.get(nLitImplied);
            coefMult = BigInteger.ONE;
            // updating of the degree of the conflict
            degreeCons = (degreeCons.multiply(coefMultCons));
        } else {
            if (coefs.get(nLitImplied).equals(BigInteger.ONE)) {
                // then we know that the resolvant will still be a conflict (cf.
                // Dixon's property)
                coefMult = cpb.getCoef(ind);
                coefMultCons = BigInteger.ONE;
                // updating of the degree of the conflict
                degree = degree.multiply(coefMult);
            } else {
                // pb-constraint has to be reduced
                // to obtain a conflictual result from the cutting plane
                WatchPb wpb = (WatchPb) cpb;
                coefsCons = wpb.getCoefs();
                assert positiveCoefs(coefsCons);
                degreeCons = reduceUntilConflict(litImplied, ind, coefsCons,
                        wpb);
                // updating of the degree of the conflict
                degreeCons = (degreeCons.multiply(coefMultCons));
                degree = degree.multiply(coefMult);
            }
            // coefficients of the conflict must be multiplied by coefMult
            for (Integer i : coefs.keySet()) {
                setCoef(i, coefs.get(i).multiply(coefMult));
            }
        }

        // cutting plane
        degree = cuttingPlane(cpb, degreeCons, coefsCons, coefMultCons, val);

		//neither litImplied nor nLitImplied is present in coefs structure
		assert !coefs.containsKey(litImplied);
		assert !coefs.containsKey(nLitImplied);
		//neither litImplied nor nLitImplied is present in byLevel structure
		assert getLevelByLevel(litImplied) == -1;
		assert getLevelByLevel(nLitImplied) == -1;
		assert degree.signum() > 0;

        // saturation
        degree = saturation();
        assert slackConflict().signum() <= 0;

        return degree;
    }

    protected BigInteger reduceUntilConflict(int litImplied, int ind,
            BigInteger[] reducedCoefs, WatchPb wpb) {
        BigInteger slackResolve = BigInteger.ONE.negate();
        BigInteger slackThis = BigInteger.ZERO;
        BigInteger slackIndex = BigInteger.ZERO;
        BigInteger ppcm;
        BigInteger reducedDegree = wpb.getDegree();

        do {
            if (slackResolve.signum() >= 0) {
                assert slackThis.signum() > 0;
                BigInteger tmp = reduceInConstraint(wpb, reducedCoefs, ind,
                        reducedDegree);
                assert ((tmp.compareTo(reducedDegree) < 0) && (tmp
                        .compareTo(BigInteger.ONE) >= 0));
                reducedDegree = tmp;
            }
            // search of the multiplying coefficients
            assert coefs.get(litImplied ^ 1).signum() > 0;
            assert reducedCoefs[ind].signum() > 0;

            BigInteger coefLitImplied = coefs.get(litImplied ^ 1);
            ppcm = ppcm(reducedCoefs[ind], coefLitImplied);
            assert ppcm.signum() > 0;
            coefMult = ppcm.divide(coefLitImplied);
            coefMultCons = ppcm.divide(reducedCoefs[ind]);

            assert coefMultCons.signum() > 0;
            assert coefMult.signum() > 0;
            assert coefMult.multiply(coefLitImplied).equals(
                    coefMultCons.multiply(reducedCoefs[ind]));

            // slacks computed for each constraint
            slackThis = wpb.slackConstraint(reducedCoefs, reducedDegree)
                    .multiply(coefMultCons);
            slackIndex = slackConflict().multiply(coefMult);

            assert slackIndex.signum() <= 0;

            // estimation of the slack after the cutting plane
            slackResolve = slackThis.add(slackIndex);
        } while (slackResolve.signum() >= 0);
        assert coefMult.multiply(coefs.get(litImplied ^ 1)).equals(
                coefMultCons.multiply(reducedCoefs[ind]));
        return reducedDegree;

    }

    /**
     * computes the slack of the current instance
     */
    public BigInteger slackConflict() {
        BigInteger poss = BigInteger.ZERO;
        BigInteger tmp;
        // for each literal
        for (Integer i : coefs.keySet()) {
            tmp = coefs.get(i);
            if (tmp.signum() != 0 && !voc.isFalsified(i))
                poss = poss.add(tmp);
        }
        return poss.subtract(degree);
    }

    public boolean oldIsAssertive(int dl) {
        BigInteger tmp;
        BigInteger slack = computeSlack(dl).subtract(degree);
        if (slack.signum() < 0)
            return false;
        for (Integer i : coefs.keySet()) {
            tmp = coefs.get(i);
            if ((tmp.signum() > 0)
                    && (voc.isUnassigned(i) || voc.getLevel(i) >= dl)
                    && (slack.compareTo(tmp) < 0))
                return true;
        }
        return false;
    }

    // computes a slack with respect to a particular decision level
    private BigInteger computeSlack(int dl) {
        BigInteger slack = BigInteger.ZERO;
        BigInteger tmp;
        for (Integer i : coefs.keySet()) {
            tmp = coefs.get(i);
            if ((tmp.signum() > 0)
                    && (((!voc.isFalsified(i)) || voc.getLevel(i) >= dl)))
                slack = slack.add(tmp);
        }
        return slack;
    }

    /**
     * tests if the conflict is assertive (allows to imply a literal) at a
     * particular decision level
     * 
     * @param dl
     *            the decision level
     * @return true if the conflict is assertive at the decision level
     */
    public boolean isAssertive(int dl) {
        assert dl <= currentLevel;
        assert dl <= currentLevel;

        currentLevel = dl;
        // assert currentSlack.equals(computeSlack(dl));
        BigInteger slack = currentSlack.subtract(degree);
        if (slack.signum() < 0)
            return false;
        return isImplyingLiteral(dl, slack);
    }

    // given the slack already computed, tests if a literal could be implied at
    // a particular level
    private boolean isImplyingLiteral(int dl, BigInteger slack) {
        // unassigned literals are tried first
        int unassigned = levelToIndex(-1);
        if (byLevel[unassigned] != null) {
            for (Integer lit : byLevel[unassigned])
                if (slack.compareTo(coefs.get(lit)) < 0)
                    return true;
        }
        // then we have to look at every literal at a decision level >=dl
        BigInteger tmp;
        for (int level = levelToIndex(dl); level < byLevel.length; level++) {
            if (byLevel[level] != null)
                for (Integer lit : byLevel[level]) {
                    tmp = coefs.get(lit);
                    if (tmp != null && slack.compareTo(tmp) < 0)
                        return true;
                }
        }
        return false;
    }

    /**
     * computes the least common factor of two integers (Plus Petit Commun
     * Multiple in french)
     * 
     * @param a
     *            first integer
     * @param b
     *            second integer
     * @return the least common factor
     */
    protected static BigInteger ppcm(BigInteger a, BigInteger b) {
        return a.divide(a.gcd(b)).multiply(b);
    }

    /**
     * constraint reduction : removes a literal of the constraint. The literal
     * should be either unassigned or satisfied. The literal can not be the
     * literal that should be resolved.
     * 
     * @param wpb
     *            the constraint to reduce
     * @param coefsBis
     *            the coefficients of the constraint wrt which the reduction
     *            will be proposed
     * @param indLitImplied
     *            index in wpb of the literal that should be resolved
     * @param degreeBis
     *            the degree of the constraint wrt which the reduction will be
     *            proposed
     * @return new degree of the reduced constraint
     */
    public BigInteger reduceInConstraint(WatchPb wpb,
            final BigInteger[] coefsBis, final int indLitImplied,
            final BigInteger degreeBis) {
        // logger.entering(this.getClass().getName(),"reduceInConstraint");
        assert degreeBis.compareTo(BigInteger.ONE) > 0;
        // search of an unassigned literal
        int lit = -1;
        for (int ind = 0; (ind < wpb.lits.length) && (lit == -1); ind++)
            if (coefsBis[ind].signum() != 0 && voc.isUnassigned(wpb.lits[ind])) {
                assert coefsBis[ind].compareTo(degreeBis) < 0;
                lit = ind;
            }

        // else, search of a satisfied literal
        if (lit == -1)
            for (int ind = 0; (ind < wpb.lits.length) && (lit == -1); ind++)
                if ((coefsBis[ind].signum() != 0)
                        && (voc.isSatisfied(wpb.lits[ind]))
                        && (ind != indLitImplied))
                    lit = ind;

        // a literal has been found
        assert lit != -1;

        assert lit != indLitImplied;
        // logger.finer("Found literal "+Lits.toString(lits[lit]));
        // reduction can be done
        BigInteger degUpdate = degreeBis.subtract(coefsBis[lit]);
        coefsBis[lit] = BigInteger.ZERO;

        // saturation of the constraint
        assert degUpdate.signum() > 0;
        BigInteger minimum = degUpdate;
        for (int i = 0; i < coefsBis.length; i++) {
            if (coefsBis[i].signum() > 0)
                minimum = minimum.min(coefsBis[i]);
            coefsBis[i] = degUpdate.min(coefsBis[i]);
        }
        if (minimum.equals(degUpdate) && !degUpdate.equals(BigInteger.ONE)) {
            // the result is a clause
            // there is no more possible reduction
            degUpdate = BigInteger.ONE;
            for (int i = 0; i < coefsBis.length; i++)
                if (coefsBis[i].signum() > 0)
                    coefsBis[i] = degUpdate;
        }

        assert coefsBis[indLitImplied].signum() > 0;
        assert degreeBis.compareTo(degUpdate) > 0;
        return degUpdate;
    }

    private boolean positiveCoefs(final BigInteger[] coefsCons) {
        for (int i = 0; i < coefsCons.length; i++) {
            if (coefsCons[i].signum() <= 0)
                return false;
        }
        return true;
    }

    /**
     * computes the level for the backtrack : the highest decision level for
     * which the conflict is assertive.
     * 
     * @param maxLevel
     *            the lowest level for which the conflict is assertive
     * @return the highest level (smaller int) for which the constraint is
     *         assertive.
     */
    public int getBacktrackLevel(int maxLevel) {
        // we are looking for a level higher than maxLevel
        // where the constraint is still assertive
        VecInt lits;
        int level;
        int indStop = levelToIndex(maxLevel) - 1;
        int indStart = levelToIndex(0);
        // BigInteger slack = currentSlack.subtract(degree);
        // for (int indLevel = indStart; indLevel >= indStop; indLevel++){
        // if (byLevel[indLevel] != null){
        // // updating the new slack
        // lits = byLevel[indLevel];
        // for (Integer lit : lits) {
        // if (voc.isFalsified(lit)
        // && voc.getLevel(lit) == indexToLevel(indLevel))
        // slack = slack.add(coefs.get(lit));
        // }
        // }
        // }
        BigInteger slack = computeSlack(0).subtract(degree);
        int previous = 0;
        for (int indLevel = indStart; indLevel <= indStop; indLevel++) {
            if (byLevel[indLevel] != null) {
                level = indexToLevel(indLevel);
                assert computeSlack(level).subtract(degree).equals(slack);
                if (isImplyingLiteral(level, slack)) {
                    break;
                }
                // updating the new slack
                lits = byLevel[indLevel];
                for (Integer lit : lits) {
                    if (voc.isFalsified(lit)
                            && voc.getLevel(lit) == indexToLevel(indLevel))
                        slack = slack.subtract(coefs.get(lit));
                }
                if (!lits.isEmpty())
                    previous = level;
            }
        }
        int retour = oldGetBacktrackLevel(maxLevel);
        assert previous == retour;
        return previous;
    }

    public int oldGetBacktrackLevel(int maxLevel) {
        int litLevel;
        int borneMax = maxLevel;
        assert oldIsAssertive(borneMax);
        int borneMin = -1;
        // on calcule borneMax,
        // le niveau le plus haut dans l'arbre ou la contrainte est assertive
        for (int lit : coefs.keySet()) {
            litLevel = voc.getLevel(lit);
            if (litLevel < borneMax && litLevel > borneMin
                    && oldIsAssertive(litLevel))
                borneMax = litLevel;
        }
        // on retourne le niveau immediatement inferieur ? borneMax
        // pour lequel la contrainte possede un literal
        int retour = 0;
        for (int lit : coefs.keySet()) {
            litLevel = voc.getLevel(lit);
            if (litLevel > retour && litLevel < borneMax) {
                retour = litLevel;
            }
        }
        return retour;
    }

    public void updateSlack(int level) {
        int dl = levelToIndex(level);
        if (byLevel[dl] != null)
            for (int lit : byLevel[dl]) {
                if (voc.isFalsified(lit)) 
                    currentSlack = currentSlack.add(coefs.get(lit));
            }
    }

    @Override
    void increaseCoef(Integer lit, BigInteger incCoef) {
        int ilit = lit.intValue();
        if ((!voc.isFalsified(ilit)) || voc.getLevel(ilit) == currentLevel) {
            currentSlack = currentSlack.add(incCoef);
        }
		assert byLevel[levelToIndex(voc.getLevel(ilit))].contains(ilit);
        super.increaseCoef(lit, incCoef);
    }

    @Override
    void decreaseCoef(Integer lit, BigInteger decCoef) {
        int ilit = lit.intValue();
        if ((!voc.isFalsified(ilit)) || voc.getLevel(ilit) == currentLevel) {
            currentSlack = currentSlack.subtract(decCoef);
        }
		assert byLevel[levelToIndex(voc.getLevel(ilit))].contains(ilit);
        super.decreaseCoef(lit, decCoef);
    }

    @Override
    void setCoef(Integer lit, BigInteger newValue) {
        int ilit = lit.intValue();
        int litLevel = voc.getLevel(ilit);
        if ((!voc.isFalsified(ilit)) || litLevel == currentLevel) {
            if (coefs.containsKey(lit))
                currentSlack = currentSlack.subtract(coefs.get(lit));
            currentSlack = currentSlack.add(newValue);
        }
        int indLitLevel = levelToIndex(litLevel);
		if (!coefs.containsKey(lit)) {
			if (byLevel[indLitLevel] == null) {
				byLevel[indLitLevel] = new VecInt();
			}
			byLevel[indLitLevel].push(ilit);

		}
		assert byLevel[indLitLevel] != null;
		assert byLevel[indLitLevel].contains(ilit);
        super.setCoef(lit, newValue);
    }

    @Override
    void removeCoef(Integer lit) {
        int ilit = lit.intValue();
        int litLevel = voc.getLevel(ilit);
        if ((!voc.isFalsified(ilit)) || litLevel == currentLevel) {
            currentSlack = currentSlack.subtract(coefs.get(lit));
        }

        int indLitLevel = levelToIndex(litLevel);
        assert indLitLevel < byLevel.length;
        assert byLevel[indLitLevel] != null;
		assert byLevel[indLitLevel].contains(ilit);
        byLevel[indLitLevel].remove(lit);
        super.removeCoef(lit);
    }


	private int getLevelByLevel(int lit){
		for (int i=0; i<byLevel.length; i++)
			if (byLevel[i]!= null && byLevel[i].contains(lit))
				return i;
		return -1;
	}
    
    
    @Override
    public String toString() {
        int lit;
        StringBuilder stb = new StringBuilder();
        for (Map.Entry<Integer, BigInteger> entry : coefs.entrySet()) {
            lit = Integer.valueOf(entry.getKey());
            stb.append(entry.getValue());
            stb.append(".");
            stb.append(Lits.toString(lit));
            stb.append(" ");
            stb.append("[");
            stb.append(voc.valueToString(lit));
            stb.append("@");
            stb.append(voc.getLevel(lit));
            stb.append("]");
        }
        return stb.toString() + " >= " + degree; //$NON-NLS-1$
    }

}
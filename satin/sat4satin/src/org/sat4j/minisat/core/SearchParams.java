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

package org.sat4j.minisat.core;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Some parameters used during the search.
 * 
 * @author daniel
 * 
 */
public class SearchParams implements Serializable {

    private static final long serialVersionUID = 1L;

    public SearchParams() {
        this(0.95, 0.999, 1.5, 100);
    }

    public SearchParams(int conflictBound) {
        this(0.95, 0.999, 1.5, conflictBound);
    }

    public SearchParams(double confincfactor, int conflictBound) {
        this(0.95, 0.999, confincfactor, conflictBound);
    }

    /**
     * @param d
     *            variable decay
     * @param e
     *            clause decay
     * @param f
     *            conflict bound increase factor
     * @param i
     *            initialConflictBound
     */
    public SearchParams(double d, double e, double f, int i) {
        varDecay = d;
        claDecay = e;
        conflictBoundIncFactor = f;
        initConflictBound = i;
    }

    /**
     * @return la valeur de clause decay
     */
    public double getClaDecay() {
        return claDecay;
    }

    /**
     * @return la valeur de var decay
     */
    public double getVarDecay() {
        return varDecay;
    }

    private double claDecay;

    private double varDecay;

    private double conflictBoundIncFactor;

    private int initConflictBound;

    // START SATIN
    private double learntBoundIncFactor = 1.1;

    private double initLearntBoundConstraintFactor = 0.5;

    /**
     * @param d
     *            variable decay
     * @param e
     *            clause decay
     * @param f
     *            conflict bound increase factor
     * @param i
     *            initialConflictBound
     * @param j
     *            learnt bound increase factor
     * @param k
     *            initial bound for learnt clauses as a factor of the number of
     *            constraints
     */
    public SearchParams(double d, double e, double f, int i,
			double j, double k) {
        varDecay = d;
        claDecay = e;
        conflictBoundIncFactor = f;
        initConflictBound = i;
        learntBoundIncFactor = j;
        initLearntBoundConstraintFactor = k;
    }

    /**
     * @return the learntBoundIncFactor
     */
    public double getLearntBoundIncFactor() {
        return learntBoundIncFactor;
    }

    /**
     * @return the initLearntBoundConstraintFactor
     */
    public double getInitLearntBoundConstraintFactor() {
        return initLearntBoundConstraintFactor;
    }
    // END SATIN

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        for (Field field : SearchParams.class.getDeclaredFields()) {
            if (!field.getName().startsWith("serial")) {
                stb.append(field.getName());
                stb.append("="); //$NON-NLS-1$
                try {
                    stb.append(field.get(this));
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                stb.append(" "); //$NON-NLS-1$
            }
        }
        return stb.toString();
    }

    /**
     * @param conflictBoundIncFactor
     *            the conflictBoundIncFactor to set
     */
    public void setConflictBoundIncFactor(double conflictBoundIncFactor) {
        this.conflictBoundIncFactor = conflictBoundIncFactor;
    }

    /**
     * @param initConflictBound
     *            the initConflictBound to set
     */
    public void setInitConflictBound(int initConflictBound) {
        this.initConflictBound = initConflictBound;
    }

    /**
     * @return the conflictBoundIncFactor
     */
    public double getConflictBoundIncFactor() {
        return conflictBoundIncFactor;
    }

    /**
     * @return the initConflictBound
     */
    public int getInitConflictBound() {
        return initConflictBound;
    }

    /**
     * @param claDecay
     *            the claDecay to set
     */
    public void setClaDecay(double claDecay) {
        this.claDecay = claDecay;
    }

    /**
     * @param varDecay
     *            the varDecay to set
     */
    public void setVarDecay(double varDecay) {
        this.varDecay = varDecay;
    }
}

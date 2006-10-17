/*
 * SAT4J: a SATisfiability library for Java   
 * Copyright (C) 2004 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas Een and Niklas Serensson.
 * Proceedings of the Sixth International Conference on Theory 
 * and Applications of Satisfiability Testing, LNCS 2919, 
 * pp 502-518, 2003.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 */

package org.sat4j.minisat.core;

import java.io.Serializable;
import java.lang.reflect.Field;

/*
 * Created on 29 oct. 2003 
 */

/**
 * @author leberre Structure de donnees permettant de configurer le prouveur.
 */
public class SearchParams implements Serializable {

    private static final long serialVersionUID = 1L;

    public SearchParams() {
        this(0.95, 0.999, 1.5, 1.1, 0.5, 100);
    }

    public SearchParams(int conflictBound) {
        this(0.95, 0.999, 1.5, 1.1, 0.5, conflictBound);
    }

    public SearchParams(double initLearntbound, int conflictBound) {
        this(0.95, 0.999, 1.5, 1.1, initLearntbound, conflictBound);
    }

    public SearchParams(double learntincfactor, double confincfactor,
            double initLearntbound, int conflictBound) {
        this(0.95, 0.999, learntincfactor, confincfactor, initLearntbound,
                conflictBound);
    }

    /**
     * @param d
     *            variable decay
     * @param e
     *            clause decay
     * @param f
     *            conflict bound increase factor
     * @param g
     *            learnt bound increase factor
     * @param h
     *            initial bound for learnt clauses as a factor of the number of
     *            constraints
     * @param i
     *            initialConflictBound
     */
    public SearchParams(double d, double e, double f, double g, double h, int i) {
        varDecay = d;
        claDecay = e;
        conflictBoundIncFactor = f;
        learntBoundIncFactor = g;
        initLearntBoundConstraintFactor = h;
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

    public double conflictBoundIncFactor;

    public double learntBoundIncFactor;

    public double initLearntBoundConstraintFactor;

    public int initConflictBound;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder stb = new StringBuilder();
        for (Field field : SearchParams.class.getFields()) {
            stb.append(field.getName());
            stb.append("=");
            try {
                stb.append(field.get(this));
            } catch (IllegalArgumentException e) {
                // TODO Bloc catch auto-genere
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Bloc catch auto-genere
                e.printStackTrace();
            }
            stb.append(" ");
        }
        return stb.toString();
    }

    @Override
    public Object clone()
    {
	SearchParams clone;

	try {
	    clone = (SearchParams) super.clone();
	}
	catch (CloneNotSupportedException e) {
	    throw new InternalError(e.toString());
	}

	return clone;
    }
}

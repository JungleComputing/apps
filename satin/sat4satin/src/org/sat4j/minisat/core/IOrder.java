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

import java.io.PrintWriter;

/**
 * Interface for the variable ordering heuristics. It has both the
 * responsability to choose the next variable to branch on and the phase of the
 * literal (positive or negative one).
 * 
 * @author daniel
 * 
 */
public interface IOrder {

    /**
     * Method used to provide an easy access the the solver vocabulary.
     * 
     * @param lits
     *            the vocabulary
     */
    void setLits(ILits lits);

    /**
     * Method called each time Solver.newVar() is called.
     * 
     */
    @Deprecated
    void newVar();

    /**
     * Method called when Solver.newVar(int) is called.
     * 
     * @param howmany
     *            the maximum number of variables
     * @see Solver#newVar(int)
     */
    void newVar(int howmany);

    /**
     * Selects the next "best" unassigned literal.
     * 
     * Note that it means selecting the best variable and the phase to branch on
     * first.
     * 
     * @return an unassigned literal or Lit.UNDEFINED no such literal exists.
     */
    int select();

    /**
     * Method called when a variable is unassigned.
     * 
     * It is useful to add back a variable in the pool of variables to order.
     * 
     * @param x
     *            a variable.
     */
    void undo(int x);

    /**
     * To be called when the activity of a literal changed.
     * 
     * @param p
     *            a literal. The associated variable will be updated.
     */
    void updateVar(int p);

    /**
     * that method has the responsability to initialize all arrays in the
     * heuristics. PLEASE CALL super.init() IF YOU OVERRIDE THAT METHOD.
     */
    void init();

    /**
     * Display statistics regarding the heuristics.
     * 
     * @param out
     *            the writer to display the information in
     * @param prefix
     *            to be used in front of each newline.
     */
    void printStat(PrintWriter out, String prefix);

    /**
     * Sets the variable activity decay as a growing factor for the next
     * variable activity.
     * 
     * @param d
     *            a number bigger than 1 that will increase the activity of the
     *            variables involved in future conflict. This is similar but
     *            more efficient than decaying all the activities by a similar
     *            factor.
     */
    void setVarDecay(double d);

    /**
     * Decay the variables activities.
     * 
     */
    void varDecayActivity();

    /**
     * To obtain the current activity of a variable.
     * 
     * @param p
     *            a literal
     * @return the activity of the variable associated to that literal.
     */
    double varActivity(int p);

    /**
     * Added for SATIN: add activity from other searches
     *
     * @param p
     *            a literal
     * @param val
     *            activity to be added
     */
     void addActivity(int p, double val);
}

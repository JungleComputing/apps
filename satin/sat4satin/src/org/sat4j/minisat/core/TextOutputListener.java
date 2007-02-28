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

/**
 * Debugging Search Listener allowing to follow the search in a textual way.
 * 
 * @author daniel
 * 
 */
@SuppressWarnings("serial")
public class TextOutputListener implements SearchListener {

    public void assuming(int p) {
        System.out.println("assuming " + p);
    }

    public void propagating(int p) {
        System.out.println("implies " + p);
    }

    public void backtracking(int p) {
        System.out.println("backtracking " + p);
    }

    public void adding(int p) {
        System.out.println("adding " + p);
    }

    public void learn(Constr clause) {

    }

    public void delete(int[] clause) {

    }

    public void conflictFound() {
        System.out.println("conflict ");
    }

    public void solutionFound() {
        System.out.println("solution found ");
    }

    public void beginLoop() {
    }

    public void start() {
    }

    public void end(Lbool result) {
    }

}

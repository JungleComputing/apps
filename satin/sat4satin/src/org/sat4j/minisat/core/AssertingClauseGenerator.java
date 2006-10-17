/*
 * SAT4J: a SATisfiability library for Java   
 * Copyright (C) 2004 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas E?n and Niklas S?rensson.
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

import org.sat4j.specs.IConstr;

/**
 * An assertingClauseGenerator is responsible for the creation of an asserting
 * clause during conflict analysis. An asserting clause is a clause that will
 * become unit when the solver will backtrack to the latest decision level,
 * providing a nice way for the solver to backtrack.
 * 
 * @author leberre
 */
public interface AssertingClauseGenerator {

    /**
     * hook method called before the analysis.
     */
    void initAnalyze();

    /**
     * hook method called when a literal from the current decision lelvel is
     * found.
     * 
     * @param p
     *            the literal in the current decision level
     */
    void onCurrentDecisionLevelLiteral(int p);

    /**
     * method indicating if an asserting clause has been built. note that this
     * method is called right after a resolution step is finished.
     * 
     * @param reason
     *            the reason of the current literal assignment
     * @return false iff the current clause is assertive
     */
    boolean clauseNonAssertive(IConstr reason);
}

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

package org.sat4j.minisat.uip;

import java.io.Serializable;

import org.sat4j.minisat.core.AssertingClauseGenerator;
import org.sat4j.specs.IConstr;

/**
 * FirstUIP scheme introduced in Chaff. Here the generator stops when a
 * syntactical criteria is met: only one literal in the current decision level
 * appears in the generated clause. The computation is done by counting the
 * literals appearing in the current decision level and decrementing that
 * counter when a resolution step is done.
 * 
 * @author leberre
 */
public class FirstUIP implements AssertingClauseGenerator, Serializable {

    private static final long serialVersionUID = 1L;

    private int counter;

    public void initAnalyze() {
        counter = 0;
    }

    public void onCurrentDecisionLevelLiteral(int p) {
        counter++;
    }

    public boolean clauseNonAssertive(IConstr reason) {
        return --counter > 0;
    }

}

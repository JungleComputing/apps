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

package org.sat4j.reader.csp;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class AllDiff implements Clausifiable {

    public void toClause(ISolver solver, IVec<Var> scope, IVec<Evaluable> vars)
            throws ContradictionException {
        int n = scope.size();
        IVecInt clause = new VecInt();
        for (int i = 0; i < n; i++) {
            Var vi = scope.get(i);
            for (int j = i + 1; j < n; j++) {
                // make all values differents
                Var vj = scope.get(j);
                for (int vali : vi.domain()) {
                    for (int valj : vj.domain()) {
                        // the two variables share a common value
                        if (vali == valj) {
                            clause.push(-vi.translate(vali));
                            clause.push(-vj.translate(valj));
                            solver.addClause(clause);
                            clause.clear();
                        }
                    }
                }
            }
        }
    }

}

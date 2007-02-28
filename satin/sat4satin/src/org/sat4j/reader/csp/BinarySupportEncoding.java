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

import java.util.HashMap;
import java.util.Map;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class BinarySupportEncoding implements Encoding {

    private final Map<Integer, IVecInt> supportsa = new HashMap<Integer, IVecInt>();

    private final Map<Integer, IVecInt> supportsb = new HashMap<Integer, IVecInt>();

    private static final Encoding instance = new BinarySupportEncoding();

    private BinarySupportEncoding() {
        // nothing here
    }

    public static Encoding instance() {
        return instance;
    }

    public void onFinish(ISolver solver, IVec<Var> scope)
            throws ContradictionException {
        generateClauses(scope.get(0), supportsa, solver);
        generateClauses(scope.get(1), supportsb, solver);

    }

    public void onInit(ISolver solver, IVec<Var> scope) {
        supportsa.clear();
        supportsb.clear();
    }

    public void onNogood(ISolver solver, IVec<Var> scope,
            Map<Evaluable, Integer> tuple) throws ContradictionException {
    }

    public void onSupport(ISolver solver, IVec<Var> scope,
            Map<Evaluable, Integer> tuple) throws ContradictionException {
        Var vara = scope.get(0);
        int va = tuple.get(vara);
        Var varb = scope.get(1);
        int vb = tuple.get(varb);
        addSupport(va, varb, vb, supportsa);
        addSupport(vb, vara, va, supportsb);
    }

    private void addSupport(int head, Evaluable v, int value,
            Map<Integer, IVecInt> supports) {
        IVecInt sup = supports.get(head);
        if (sup == null) {
            sup = new VecInt();
            supports.put(head, sup);
        }
        sup.push(v.translate(value));
    }

    private void generateClauses(Evaluable v, Map<Integer, IVecInt> supports,
            ISolver solver) throws ContradictionException {
        IVecInt clause = new VecInt();
        for (int key : v.domain()) {
            clause.clear();
            IVecInt support = supports.get(key);
            clause.push(-v.translate(key));
            if (support != null) {
                for (int i : support)
                    clause.push(i);
            }
            solver.addClause(clause);
        }
    }

}

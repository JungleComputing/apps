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
import java.util.Set;
import java.util.TreeSet;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

public class GeneralizedSupportEncoding implements Encoding {

    private final Map<Set<Integer>, IVecInt> supports = new HashMap<Set<Integer>, IVecInt>();

    private static final Encoding instance = new GeneralizedSupportEncoding();

    private GeneralizedSupportEncoding() {

    }

    public static Encoding instance() {
        return instance;
    }

    public void onFinish(ISolver solver, IVec<Var> scope)
            throws ContradictionException {
        // TODO Auto-generated method stub

    }

    public void onInit(ISolver solver, IVec<Var> scope) {
        supports.clear();
        int[] acc = new int[scope.size()];
        fill(0, scope, acc, supports);
    }

    public void onNogood(ISolver solver, IVec<Var> scope,
            Map<Evaluable, Integer> tuple) throws ContradictionException {

    }

    public void onSupport(ISolver solver, IVec<Var> scope,
            Map<Evaluable, Integer> tuple) throws ContradictionException {
        for (int i = 0; i < scope.size(); i++) {
            Set<Integer> set = new TreeSet<Integer>();
            Var vari = scope.get(i);
            for (int j = 0; j < scope.size(); j++) {
                if (i != j) {
                    set.add(scope.get(j).translate(tuple.get(vari)));
                }
            }
            IVecInt support = supports.get(set);
            assert support != null;
            support.push(vari.translate(tuple.get(vari)));
        }

    }

    private void fill(int n, IVec<Var> scope, int[] acc,
            Map<Set<Integer>, IVecInt> supports) {
        if (n == scope.size()) {
            for (int j = 0; j < acc.length; j++) {
                Set<Integer> set = new TreeSet<Integer>();
                for (int i = 0; i < acc.length; i++)
                    if (i != j)
                        set.add(scope.get(i).translate(acc[i]));
                supports.put(set, new VecInt());
            }
        } else
            for (int i : scope.get(n).domain()) {
                acc[n] = i;
                fill(n + 1, scope, acc, supports);
            }

    }
}

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

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;

public abstract class Supports implements Relation {

    private Encoding encoding;

    private final int arity;

    private int[][] tuples;

    private int lastmatch;

    private Map<Evaluable, Integer> mtuple;

    public Supports(int arity, int nbtuples) {
        this.arity = arity;
        tuples = new int[nbtuples][];
    }

    public void addTuple(int index, int[] tuple) {
        tuples[index] = tuple;
    }

    public int arity() {
        return arity;
    }

    public void toClause(ISolver solver, IVec<Var> scope, IVec<Evaluable> vars)
            throws ContradictionException {
        assert vars.size() == 0;
        assert scope.size() == arity;
        int[] tuple = new int[scope.size()];
        mtuple = new HashMap<Evaluable, Integer>();
        lastmatch = -1;
        encoding = chooseEncoding(scope);
        encoding.onInit(solver, scope);
        find(tuple, 0, scope, solver);
        encoding.onFinish(solver, scope);
    }

    protected abstract Encoding chooseEncoding(IVec<Var> scope);

    private void find(int[] tuple, int n, IVec<Var> scope, ISolver solver)
            throws ContradictionException {
        if (n == scope.size()) {
            assert mtuple.size() == n;
            if (notPresent(tuple)) {
                encoding.onNogood(solver, scope, mtuple);
            } else {
                encoding.onSupport(solver, scope, mtuple);
            }
        } else {
            Domain domain = scope.get(n).domain();
            for (int i = 0; i < domain.size(); i++) {
                tuple[n] = domain.get(i);
                mtuple.put(scope.get(n), tuple[n]);
                find(tuple, n + 1, scope, solver);
            }
            mtuple.remove(scope.get(n));

        }

    }

    private boolean notPresent(int[] tuple) {
        // System.out.println("Checking:" + Arrays.asList(tuple));
        // find the first tuple begining with the same
        // initial number
        int i = lastmatch + 1;
        int j = 0;
        final int[][] ltuples = tuples;
        int searchedvalue, currentvalue;
        while (i < ltuples.length && j < tuple.length) {
            searchedvalue = ltuples[i][j];
            currentvalue = tuple[j];
            if (searchedvalue < currentvalue) {
                i++;
                j = 0;
                continue;
            }
            if (searchedvalue > currentvalue)
                return true;
            j++;
        }
        if (j == tuple.length) {
            lastmatch = i;
            return false;
        }
        return true;
    }
}

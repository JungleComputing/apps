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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.sat4j.core.Vec;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;

/**
 * A predicate is a formula given in intension.
 * 
 * @author daniel
 */
public class Predicate implements Clausifiable {

    private String expr;

    private Encoding encoding;

    private final IVec<String> variables = new Vec<String>();

    private static Context cx;

    private static Scriptable scope;

    static {
        cx = Context.enter();
        // cx.setOptimizationLevel(1);
        scope = cx.initStandardObjects();
        // System.out.println("scope "+vscope);
        // System.out.println("params "+vars);
        try {
            URL url = Predicate.class.getResource("predefinedfunctions.js");
            cx.evaluateReader(scope, new InputStreamReader(url.openStream()),
                    "predefinedfunctions.js", 1, null);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Predicate() {
    }

    public void setExpression(String expr) {
        this.expr = expr;
    }

    public void addVariable(String name) {
        variables.push(name);
    }

    private boolean evaluate(int[] values) {
        assert values.length == variables.size();
        for (int i = 0; i < variables.size(); i++) {
            scope.put(variables.get(i), scope, values[i]);
        }
        Object result = myscript.exec(cx, scope);
        return Context.toBoolean(result);
    }

    public void toClause(ISolver solver, IVec<Var> vscope, IVec<Evaluable> vars)
            throws ContradictionException {
        if (myscript == null) {
            myscript = cx.compileString(expr, "rhino.log", 1, null);
        }
        if (vscope.size() == 2) {
            encoding = BinarySupportEncoding.instance();
        } else {
            encoding = DirectEncoding.instance();
        }
        encoding.onInit(solver, vscope);
        int[] tuple = new int[vars.size()];
        valuemapping.clear();
        find(tuple, 0, vscope, vars, solver);
        encoding.onFinish(solver, vscope);
    }

    private final Map<Evaluable, Integer> valuemapping = new HashMap<Evaluable, Integer>();

    private Script myscript;

    private void find(int[] tuple, int n, IVec<Var> scope,
            IVec<Evaluable> vars, ISolver solver) throws ContradictionException {
        if (valuemapping.size() == scope.size()) {
            for (int i = 0; i < tuple.length; i++) {
                Evaluable ev = vars.get(i);
                Integer value = valuemapping.get(ev);
                if (value == null) {
                    tuple[i] = ev.domain().get(0);
                } else {
                    tuple[i] = value;
                }
            }
            if (evaluate(tuple)) {
                encoding.onSupport(solver, scope, valuemapping);
            } else {
                encoding.onNogood(solver, scope, valuemapping);
            }
        } else {
            Var var = scope.get(n);
            Domain domain = var.domain();
            for (int i = 0; i < domain.size(); i++) {
                valuemapping.put(var, domain.get(i));
                find(tuple, n + 1, scope, vars, solver);
            }
            valuemapping.remove(var);
        }
    }
}

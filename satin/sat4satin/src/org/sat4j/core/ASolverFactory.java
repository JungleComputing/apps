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

package org.sat4j.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.sat4j.specs.ISolver;

/**
 * A solver factory is responsible to provide prebuilt solvers to the end user.
 * 
 * @author bourgeois
 */
public abstract class ASolverFactory {

    /**
     * This methods returns names of solvers to be used with the method
     * getSolverByName().
     * 
     * @return an array containing the names of all the solvers available in the
     *         library.
     * @see #createSolverByName(String)
     */
    public String[] solverNames() {
        List<String> l = new ArrayList<String>();
        Method[] solvers = this.getClass().getDeclaredMethods();
        for (int i = 0; i < solvers.length; i++) {
            if (solvers[i].getParameterTypes().length == 0
                    && solvers[i].getName().startsWith("new")) { //$NON-NLS-1$
                l.add(solvers[i].getName().substring(3));
            }
        }
        String[] names = new String[l.size()];
        l.toArray(names);
        return names;
    }

    /**
     * create a solver from its String name. the solvername Xxxx must map one of
     * the newXxxx methods.
     * 
     * @param solvername
     *            the name of the solver
     * @return an ISolver built using newSolvername. <code>null</code> if the
     *         solvername doesn't map one of the method of the factory.
     */
    public ISolver createSolverByName(String solvername) {
        Class[] paramtypes = {};
        try {
            Method m = this.getClass()
                    .getMethod("new" + solvername, paramtypes); //$NON-NLS-1$
            return (ISolver) m.invoke(null, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * To obtain the default solver of the library. The solver is suitable to
     * solve huge SAT benchmarks. It should reflect state-of-the-art SAT
     * technologies.
     * 
     * For solving small/easy SAT benchmarks, use lightSolver() instead.
     * 
     * @return a solver from the factory
     * @see #lightSolver()
     */
    public abstract ISolver defaultSolver();

    /**
     * To obtain a solver that is suitable for solving many small instances of
     * SAT problems.
     * 
     * The solver is not using sophisticated but costly reasoning and avoids to
     * allocate too much memory.
     * 
     * For solving bigger SAT benchmarks, use defaultSolver() instead.
     * 
     * @return a solver from the factory
     * @see #defaultSolver()
     */
    public abstract ISolver lightSolver();
}

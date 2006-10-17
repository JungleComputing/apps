/**
 * 
 */
package org.sat4j.core;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.sat4j.specs.ISolver;

/**
 * A solver factory is responsible to provide prebuilt solvers
 * to the end user.
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
                && solvers[i].getName().startsWith("new")) {
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
                .getMethod("new" + solvername, paramtypes);
            return (ISolver) m.invoke(null, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * To obtain the default solver of the library.
     * The solver is suitable to solve huge SAT benchmarks.
     * It should reflect state-of-the-art SAT technologies.
     * 
     * For solving small/easy SAT benchmarks, use lightSolver() instead.
     * 
     * @return a solver from the factory
     * @see #lightSolver()
     */
    public abstract ISolver defaultSolver();

    /**
     * To obtain a solver that is suitable for solving
     * many small instances of SAT problems.
     * 
     * The solver is not using sophisticated but costly 
     * reasoning and avoids to allocate too much memory.
     * 
     * For solving bigger SAT benchmarks, use defaultSolver() instead.
     * 
     * @return a solver from the factory
     * @see #defaultSolver()
     */
    public abstract ISolver lightSolver();
}

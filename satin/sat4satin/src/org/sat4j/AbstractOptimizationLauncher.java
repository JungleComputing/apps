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
package org.sat4j;

import java.io.PrintWriter;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.TimeoutException;

/**
 * This class is intended to be used by launchers to solve optimization
 * problems, i.e. problems for which a loop is needed to find the optimal
 * solution.
 * 
 * @author leberre
 * 
 */
@SuppressWarnings("PMD")
public abstract class AbstractOptimizationLauncher extends AbstractLauncher {

    private static final String CURRENT_OPTIMUM_VALUE_PREFIX = "o "; //$NON-NLS-1$

    @Override
    protected void displayResult() {
        if (solver == null)
            return;
        PrintWriter out = getLogWriter();
        solver.printStat(out, COMMENT_PREFIX);
        ExitCode exitCode = getExitCode();
        out.println(ANSWER_PREFIX + exitCode);
        if (exitCode == ExitCode.SATISFIABLE
                || exitCode == ExitCode.OPTIMUM_FOUND) {
            out.print(SOLUTION_PREFIX);
            getReader().decode(solver.model(), out);
            out.println();
            IOptimizationProblem optproblem = (IOptimizationProblem) solver;
            if (!optproblem.hasNoObjectiveFunction()) {
                log("objective function=" + optproblem.calculateObjective()); //$NON-NLS-1$
            }

        }
        log("Total wall clock time (ms): " //$NON-NLS-1$
                + (System.currentTimeMillis() - getBeginTime()) / 1000.0);
    }

    @Override
    protected void solve(IProblem problem) throws TimeoutException {
        boolean isSatisfiable = false;

        IOptimizationProblem optproblem = (IOptimizationProblem) problem;

        try {
            while (optproblem.admitABetterSolution()) {
                if (!isSatisfiable) {
                    if (optproblem.nonOptimalMeansSatisfiable()) {
                        setExitCode(ExitCode.SATISFIABLE);
                        if (optproblem.hasNoObjectiveFunction()) {
                            return;
                        }
                        log("SATISFIABLE"); //$NON-NLS-1$
                    }
                    isSatisfiable = true;
                    log("OPTIMIZING..."); //$NON-NLS-1$
                }
                log("Got one! Elapsed wall clock time (in seconds):" //$NON-NLS-1$
                        + (System.currentTimeMillis() - getBeginTime())
                        / 1000.0);
                getLogWriter().println(
                        CURRENT_OPTIMUM_VALUE_PREFIX
                                + optproblem.calculateObjective());
                optproblem.discard();
            }
            if (isSatisfiable) {
                setExitCode(ExitCode.OPTIMUM_FOUND);
            } else {
                setExitCode(ExitCode.UNSATISFIABLE);
            }
        } catch (ContradictionException ex) {
            assert isSatisfiable;
            setExitCode(ExitCode.OPTIMUM_FOUND);
        }
    }

}

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

package org.sat4j;

import static java.lang.System.exit;
import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

// import org.apache.commons.beanutils.BeanUtils;
import org.sat4j.core.ASolverFactory;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

//
import org.sat4j.minisat.core.Solver;

/**
 * This class is used to launch the SAT solvers from the command line.
 *
 * It is compliant with the SAT competition (www.satcompetition.org) I/O format.
 * The launcher is to be used as follows:
 *
 * <pre>
 *   [solvername] finalname [key=value]*
 * </pre>
 *
 * If no solver name is given, then the default solver of the solver factory is
 * used (@see org.sat4j.core.ASolverFactory#defaultSolver()).
 *
 * @author leberre
 */
public class Lanceur {

    /**
     * Enumeration allowing to manage easily exit code for the SAT and PB
     * Competitions.
     * 
     * @author leberre
     * 
     */
    public enum ExitCode {
        OPTIMUM_FOUND(30, "OPTIMUM FOUND"), SATISFIABLE(10), UNKNOWN(0), UNSATISFIABLE(
                20);

        /** value of the exit code. */
        private final int value;

        /** alternative textual representation of the exit code. */
        private final String str;

        /**
         * creates an exit code with a given value.
         * 
         * @param i
         *            the value of the exit code
         */
        ExitCode(final int i) {
            this.value = i;
            str = null;
        }

        /**
         * creates an exit code with a given value and an alternative textual
         * representation.
         * 
         * @param i
         *            the value of the exit code
         * @param str
         *            the alternative textual representation
         */
        ExitCode(final int i, final String str) {
            this.value = i;
            this.str = str;
        }

        /**
         * @return the exit code value
         */
        public int value() {
            return value;
        }

        /**
         * @return the name of the enum or the alternative textual
         *         representation if any.
         */
        @Override
        public String toString() {
            if (str != null) {
                return str;
            }
            return super.toString();
        }
    }

    /**
     * Lance le prouveur sur un fichier Dimacs.
     * 
     * @param args
     *            doit contenir le nom d'un fichier Dimacs, eventuellement
     *            compress?.
     */
    public static void main(final String[] args) {
        Lanceur lanceur = new Lanceur();
        lanceur.run(args);
    }

    protected long begintime;

    protected ExitCode exitcode = ExitCode.UNKNOWN;

    protected ASolverFactory factory;

    protected Reader reader;

    protected int argindex = 0;

    private Thread shutdownHook = new Thread() {
        @Override
        public void run() {
            displayResult(solver, begintime, exitcode);
        }
    };

    protected ISolver solver;

    Lanceur() {
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    /**
     * @param args
     * @return
     */
    protected ISolver configureSolver(String[] args) {
        factory = new org.sat4j.minisat.SolverFactory();

        if (args.length < 1) {
            usage();
            exit(-1);
        }

        ISolver asolver;
        if (args.length == argindex + 1) {
            asolver = factory.defaultSolver();
        } else {
            asolver = factory.createSolverByName(args[argindex++]);
            // use remaining data to configure the solver
            int others = argindex + 1;
            while (args.length > others) {
                String[] param = args[others].split("=");
                assert param.length == 2;
                System.out.println("c setting " + param[0] + " to " + param[1]);
                try {
		  // BeanUtils.setProperty(asolver, param[0], param[1]);
                } catch (Exception e) {
                    System.out.println("c Cannot set parameter : "
                            + args[others]);
                }
                others++;
            }
        }
        out.println(asolver.toString("c "));
        out.println("c timeout: " + asolver.getTimeout() + "s");
        return asolver;
    }

    private void usage() {
        out
                .println("Usage: java -jar sat4j.jar [<solver>] <cnffile> [<timeout>]");
        showAvailableSolvers();
    }

    protected Reader createReader(ISolver solver) {
        return new InstanceReader(solver);
    }

    /**
     * @throws IOException
     */
    private void displayHeader() throws IOException {
        out
                .println("c SAT4J: a SATisfiability library for Java (c) 2004-2005 Daniel Le Berre");
        out
                .println("c This is free software under the GNU LGPL licence. See www.sat4j.org for details.");
        URL url = Lanceur.class.getResource("/sat4j.version");
        if (url != null) {
            BufferedReader in = new BufferedReader(new InputStreamReader(url
                    .openStream()));
            out.println("c version " + in.readLine());
            in.close();
        } else {
            out.println("c no version file found!!!");
        }
    }

    /**
     * @param solver
     * @param begintime
     * @param isSat
     */
    protected void displayResult(final ISolver solver, final long begintime,
            final ExitCode exitcode) {
        if (solver != null) {
            double cputime = (System.currentTimeMillis() - begintime) / 1000.0;
            solver.printStat(System.out, "c ");
            out.println("s " + exitcode);
            if (exitcode == ExitCode.SATISFIABLE) {
                int[] model = solver.model();
                out.println("v " + reader.decode(model));
            }
            out.println("c Total CPU time (ms) : " + cputime);
        }
    }

    /**
     * Reads a problem file from the command line.
     *
     * @param args command line arguments
     * @param solver the solver to feed
     * @param begintime program begin time
     * @return a reference to the problem to solve
     * @throws FileNotFoundException if the file is not found
     * @throws ParseFormatException if the problem is not expressed using the
     *         right format
     * @throws IOException for other IO problems
     * @throws ContradictionException if the problem is found trivially unsat
     */
    private IProblem readProblem(String[] args, ISolver solver, long begintime)
            throws FileNotFoundException, ParseFormatException, IOException,
            ContradictionException {
        out.println("c solving " + args[argindex]);
        out.println("c reading problem ... ");
        reader = createReader(solver);
        IProblem problem = reader.parseInstance(args[argindex]);
        out.println("c ... done. Time "
                + (System.currentTimeMillis() - begintime) / 1000.0 + " ms.");
        out.println("c #vars     " + solver.nVars());
        out.println("c #constraints  " + solver.nConstraints());
        return problem;
    }

    public void run(String[] args) {

        try {
            displayHeader();

            solver = configureSolver(args);

            begintime = System.currentTimeMillis();

            IProblem problem = readProblem(args, solver, begintime);

            try {
                solve(problem);
            } catch (TimeoutException e) {
                out.println("c timeout");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ContradictionException e) {
            exitcode = ExitCode.UNSATISFIABLE;
            out.println("c (trivial inconsistency)");
        } catch (ParseFormatException e) {
            e.printStackTrace();
        }
        // exit(exitcode.value());
    }

    void showAvailableSolvers() {
        if (factory != null) {
            out.println("Available solvers: ");
            String[] names = factory.solverNames();
            for (int i = 0; i < names.length; i++) {
                out.println(names[i]);
            }
        }
    }

    protected void solve(IProblem problem) throws TimeoutException {
        boolean isSat = problem.isSatisfiable();
        exitcode = isSat ? ExitCode.SATISFIABLE : ExitCode.UNSATISFIABLE;
    }
}

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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.Properties;

import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * That class is used by launchers used to solve decision problems, i.e.
 * problems with YES/NO/UNKNOWN answers.
 * 
 * @author leberre
 * 
 */
public abstract class AbstractLauncher implements Serializable {

    public static final String SOLUTION_PREFIX = "v "; //$NON-NLS-1$

    public static final String ANSWER_PREFIX = "s "; //$NON-NLS-1$

    public static final String COMMENT_PREFIX = "c "; //$NON-NLS-1$

    private long beginTime;
    
    private ExitCode exitCode = ExitCode.UNKNOWN;

    protected Reader reader;

    private transient PrintWriter out = new PrintWriter(System.out, true);

    private static final boolean cputimesupported = ManagementFactory.getThreadMXBean().isCurrentThreadCpuTimeSupported();
    
    protected transient Thread shutdownHook = new Thread() {
        @Override
        public void run() {
            displayResult();
        }
    };

    protected ISolver solver;

    private boolean silent = false;

    private double cputime;

    protected AbstractLauncher() {
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    protected void displayResult() {
        if (solver != null) {
            double wallclocktime = (System.currentTimeMillis() - beginTime) / 1000.0;
            solver.printStat(out, COMMENT_PREFIX);
            out.println(ANSWER_PREFIX + exitCode);
            if (exitCode == ExitCode.SATISFIABLE) {
                int[] model = solver.model();
                out.print(SOLUTION_PREFIX);
                reader.decode(model, out);
                out.println();
            }
            log("Total wall clock time (in seconds) : " + wallclocktime); //$NON-NLS-1$
            if (cputimesupported) {
                log("Total CPU time (experimental, in seconds) : " + cputime) ; //$NON-NLS-1$
            }
        }
    }

    protected abstract void usage();

    /**
     * @throws IOException
     */
    protected final void displayHeader() throws IOException {
        log("SAT4J: a SATisfiability library for Java (c) 2004-2006 Daniel Le Berre"); //$NON-NLS-1$
        log("This is free software under the GNU LGPL licence. See www.sat4j.org for details."); //$NON-NLS-1$
        log("This software uses some libraries from the Jakarta project. See jakarta.apache.org for details."); //$NON-NLS-1$
        URL url = Lanceur.class.getResource("/sat4j.version"); //$NON-NLS-1$
        if (url == null) {
            log("no version file found!!!"); //$NON-NLS-1$			
        } else {
            BufferedReader in = new BufferedReader(new InputStreamReader(url
                    .openStream()));
            log("version " + in.readLine()); //$NON-NLS-1$
            in.close();
        }
        Properties prop = System.getProperties();
        String[] infoskeys = {
                "sun.arch.data.model", "java.version", "os.name", "os.version", "os.arch" }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$//$NON-NLS-5$
        for (String key : infoskeys) {
            log(key + "\t" + prop.getProperty(key)); //$NON-NLS-1$
        }
        Runtime runtime = Runtime.getRuntime();
        log("Free memory " + runtime.freeMemory()); //$NON-NLS-1$
        log("Max memory " + runtime.maxMemory()); //$NON-NLS-1$
        log("Total memory " + runtime.totalMemory()); //$NON-NLS-1$
        log("Number of processors " + runtime.availableProcessors()); //$NON-NLS-1$
    }

    /**
     * Reads a problem file from the command line.
     * 
     * @param args
     *            command line arguments
     * @param solver
     *            the solver to feed
     * @param begintime
     *            program begin time
     * @return a reference to the problem to solve
     * @throws FileNotFoundException
     *             if the file is not found
     * @throws ParseFormatException
     *             if the problem is not expressed using the right format
     * @throws IOException
     *             for other IO problems
     * @throws ContradictionException
     *             if the problem is found trivially unsat
     */
    protected IProblem readProblem(String problemname)
            throws FileNotFoundException, ParseFormatException, IOException,
            ContradictionException {
        log("solving " + problemname); //$NON-NLS-1$
        log("reading problem ... "); //$NON-NLS-1$
        reader = createReader(solver, problemname);
        IProblem problem = reader.parseInstance(problemname);
        log("... done. Wall clock time " //$NON-NLS-1$
                + (System.currentTimeMillis() - beginTime) / 1000.0 + "s."); //$NON-NLS-1$
        if (cputimesupported) {
            log("CPU time (experimental) "+ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() /1000000000.0+"s.");
        }
        log("#vars     " + solver.nVars()); //$NON-NLS-1$
        log("#constraints  " + solver.nConstraints()); //$NON-NLS-1$
        return problem;
    }

    protected abstract Reader createReader(ISolver solver, String problemname);

    public void run(String[] args) {

        try {
            displayHeader();
            solver = configureSolver(args);
            if (solver == null)
                return;
            String instanceName = getInstanceName(args);
            beginTime = System.currentTimeMillis();
            IProblem problem = readProblem(instanceName);
            try {
                solve(problem);
            } catch (TimeoutException e) {
                log("timeout"); //$NON-NLS-1$
            }
            if (cputimesupported) {
                cputime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() /1000000000.0;
            }
        } catch (FileNotFoundException e) {
            log("FATAL");
            e.printStackTrace();
        } catch (IOException e) {
            log("FATAL");
            e.printStackTrace();
        } catch (ContradictionException e) {
            exitCode = ExitCode.UNSATISFIABLE;
            log("(trivial inconsistency)"); //$NON-NLS-1$
        } catch (ParseFormatException e) {
            log("FATAL");
            e.printStackTrace();
        }
    }

    protected abstract String getInstanceName(String[] args);

    protected abstract ISolver configureSolver(String[] args);

    /**
     * Display messages as comments on STDOUT
     * 
     * @param message
     */
    protected void log(String message) {
        if (!silent)
            out.println(COMMENT_PREFIX + message);
    }

    protected void solve(IProblem problem) throws TimeoutException {
        exitCode = problem.isSatisfiable() ? ExitCode.SATISFIABLE
                : ExitCode.UNSATISFIABLE;
    }

    /**
     * Change the value of the exit code in the Launcher
     * 
     * @param exitCode
     *            the new ExitCode
     */
    public final void setExitCode(ExitCode exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Get the value of the ExitCode
     * 
     * @return the current value of the Exitcode
     */
    public final ExitCode getExitCode() {
        return exitCode;
    }

    /**
     * Obtaining the current time spent since the beginning of the solving
     * process.
     * 
     * @return the time signature at the beginning of the run() method.
     */
    public final long getBeginTime() {
        return beginTime;
    }

    /**
     * 
     * @return the reader used to parse the instance
     */
    public final Reader getReader() {
        return reader;
    }

    /**
     * To change the output stream on which statistics are displayed. By
     * default, the solver displays everything on System.out.
     * 
     * @param out
     */
    public void setLogWriter(PrintWriter out) {
        this.out = out;
    }

    public PrintWriter getLogWriter() {
        return out;
    }

    protected void setSilent(boolean b) {
        silent = b;
    }
}

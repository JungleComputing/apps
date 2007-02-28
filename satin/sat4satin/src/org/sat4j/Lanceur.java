/*
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2006 Daniel Le
 * Berre
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.sat4j.core.ASolverFactory;
import org.sat4j.minisat.constraints.MixedDataStructureDaniel;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.DotSearchListener;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.learning.LimitedLearning;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.minisat.uip.FirstUIP;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * This class is used to launch the SAT solvers from the command line. It is
 * compliant with the SAT competition (www.satcompetition.org) I/O format. The
 * launcher is to be used as follows:
 * 
 * <pre>
 *                [solvername] filename [key=value]*
 * </pre>
 * 
 * If no solver name is given, then the default solver of the solver factory is
 * used (@see org.sat4j.core.ASolverFactory#defaultSolver()).
 * 
 * @author leberre
 */
public class Lanceur extends AbstractLauncher {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

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
	// SATIN: currently cannot call exit() from main app, or the
	// fault-tolerance code will kick in!
        // System.exit(lanceur.getExitCode().value());
    }

    protected ASolverFactory factory;

    private String filename;

    private String resultsfile;

    private boolean update;

    private boolean replay;

    @SuppressWarnings("nls")
    private Options createCLIOptions() {
        Options options = new Options();

        options.addOption("l", "library", true,
                "specifies the name of the library used (minisat by default)");
        options.addOption("s", "solver", true,
                "specifies the name of a prebuilt solver from the library");
        options.addOption("S", "Solver", true,
                "setup a solver using a solver config string");
        options.addOption("t", "timeout", true,
                "specifies the timeout (in seconds)");
        options.addOption("T", "timeoutms", true,
                "specifies the timeout (in milliseconds)");
        options
                .addOption("d", "dot", true,
                        "create a sat4j.dot file in current directory representing the search");
        options
                .addOption("f", "filename", true,
                        "specifies the file to use (in conjunction with -d for instance)");
        options.addOption("r", "replay", true, "replay stored results");
        options.addOption("b", "backup", true,
                "backup results in specified file");
        options
                .addOption("u", "update", false,
                        "update results file if needed");
        options.addOption("m", "mute", false, "Set launcher in silent mode");
        Option op = options.getOption("l");
        op.setArgName("libname");
        op = options.getOption("s");
        op.setArgName("solvername");
        op = options.getOption("t");
        op.setArgName("delay");
        op = options.getOption("d");
        return options;
    }

    /**
     * @param args
     * @return
     */
    @SuppressWarnings("nls")
    @Override
    protected ISolver configureSolver(String[] args) {
        Options options = createCLIOptions();
        if (args.length == 0) {
            HelpFormatter helpf = new HelpFormatter();
            helpf.printHelp("java -jar sat4j.jar", options, true);
            return null;
        }
        try {
            CommandLine cmd = new PosixParser().parse(options, args);

            String framework = cmd.getOptionValue("l"); //$NON-NLS-1$
            if (framework == null) { //$NON-NLS-1$
                framework = "minisat";
            }
            assert "minisat".equals(framework) || "ubcsat".equals(framework); //$NON-NLS-1$//$NON-NLS-2$

            try {
                Class clazz = Class
                        .forName("org.sat4j." + framework + ".SolverFactory"); //$NON-NLS-1$ //$NON-NLS-2$
                Class[] params = {};
                Method m = clazz.getMethod("instance", params); //$NON-NLS-1$
                factory = (ASolverFactory) m.invoke(null, (Object[]) null);
            } catch (Exception e) {
                System.err.println(Messages
                        .getString("Lanceur.wrong.framework")); //$NON-NLS-1$
                e.printStackTrace();
            }

            ISolver asolver;
            if (cmd.hasOption("S")) {
                asolver = configureFromString(cmd.getOptionValue("S"));
            } else {
                String solvername = cmd.getOptionValue("s");
                if (solvername == null) {
                    asolver = factory.defaultSolver();
                } else {
                    asolver = factory.createSolverByName(solvername);
                }
            }
            String timeout = cmd.getOptionValue("t");
            if (timeout == null) {
                timeout = cmd.getOptionValue("T");
                if (timeout != null) {
                    asolver.setTimeoutMs(Long.parseLong(timeout));
                }
            } else {
                asolver.setTimeout(Integer.parseInt(timeout));
            }
            filename = cmd.getOptionValue("f");

            if (cmd.hasOption("d")) {
                String dotfilename = null;
                if (filename != null) {
                    dotfilename = cmd.getOptionValue("d");
                }
                if (dotfilename == null) {
                    dotfilename = "sat4j.dot";
                }
                ((Solver) asolver).setSearchListener(new DotSearchListener(
                        dotfilename));
            }

            if (cmd.hasOption("m")) {
                setSilent(true);
            }
            int others = 0;
            String[] rargs = cmd.getArgs();
            if (filename == null) {
                filename = rargs[others++];
            }

            update = cmd.hasOption("u");

            resultsfile = cmd.getOptionValue("r");

            if (resultsfile == null) {
                replay = false;
                resultsfile = cmd.getOptionValue("b");
            } else
                replay = true;

            // use remaining data to configure the solver
            while (others < rargs.length) {
                String[] param = rargs[others].split("="); //$NON-NLS-1$
                assert param.length == 2;
                log("setting " + param[0] + " to " + param[1]); //$NON-NLS-1$ //$NON-NLS-2$
                try {
                    BeanUtils.setProperty(asolver, param[0], param[1]);
                } catch (Exception e) {
                    log("Cannot set parameter : " //$NON-NLS-1$
                            + args[others]);
                }
                others++;
            }

            log(asolver.toString(COMMENT_PREFIX)); //$NON-NLS-1$
            log("timeout: " + asolver.getTimeout() + "s"); //$NON-NLS-1$ //$NON-NLS-2$
            return asolver;
        } catch (ParseException e1) {
            HelpFormatter helpf = new HelpFormatter();
            helpf.printHelp("java -jar sat4j.jar", options, true);
            usage();
        }
        return null;
    }

    @Override
    protected Reader createReader(ISolver solver, String problemname) {
        return new InstanceReader(solver);
    }

    @Override
    public void run(String[] args) {
        if (replay) {
            try {
                displayHeader();
            } catch (IOException e) {
                e.printStackTrace();
            }

            solver = configureSolver(args);

            if (solver == null)
                return;

            runValidationFile(solver, resultsfile, update);
        } else
            super.run(args);
    }

    void showAvailableSolvers() {
        if (factory != null) {
            log("Available solvers: "); //$NON-NLS-1$
            String[] names = factory.solverNames();
            for (int i = 0; i < names.length; i++) {
                log(names[i]);
            }
        }
    }

    @Override
    protected void usage() {
        showAvailableSolvers();
    }

    @Override
    protected String getInstanceName(String[] args) {
        return filename;
    }

    private final void stockValidationFile(final ISolver solver,
            final String filename, final String wxpFileName,
            final boolean tosave) {
        try {
            if (tosave) {
                (new FileWriter(wxpFileName, true)).close();
            }
            final ResultsManager unitTest = new ResultsManager(wxpFileName,
                    tosave);
            ExitCode exitCode = ExitCode.UNKNOWN;
            Reader reader = createReader(solver, filename);
            IProblem problem;
            problem = reader.parseInstance(filename);
            if (problem.isSatisfiable()) {
                exitCode = ExitCode.SATISFIABLE;
            } else {
                exitCode = ExitCode.UNSATISFIABLE;
            }
            final ResultCode resultCode = unitTest.compare(filename, exitCode);
            getLogWriter().println(
                    ResultsManager.printLine(filename, exitCode, resultCode));

            if ((tosave)
                    && ((resultCode != ResultCode.KO) && (resultCode != ResultCode.WARNING))) {
                unitTest.save(wxpFileName);
                System.out.println("File Saved As <" + wxpFileName + ">");
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (ParseFormatException e) {
            e.printStackTrace();
        } catch (ContradictionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

    }

    private void runValidationFile(ISolver solver, String filename,
            boolean tosave) {
        final ResultsManager unitTest;
        try {

            unitTest = new ResultsManager(filename, tosave);
            final int[] results = new int[ResultCode.values().length + 1];
            for (String file : unitTest.getFiles()) {
                ExitCode exitCode = ExitCode.UNKNOWN;
                Reader reader = createReader(solver, file);
                IProblem problem;
                try {
                    problem = reader.parseInstance(file);
                    if (problem.isSatisfiable()) {
                        exitCode = ExitCode.SATISFIABLE;
                    } else {
                        exitCode = ExitCode.UNSATISFIABLE;
                    }
                } catch (FileNotFoundException e) {
                    log("FATAL "+e.getMessage());
                } catch (ParseFormatException e) {
                    log("FATAL "+e.getMessage());
                } catch (IOException e) {
                    log("FATAL "+e.getMessage());
                } catch (ContradictionException e) {
                    log("Trivial inconsistency");
                    exitCode = ExitCode.UNSATISFIABLE;
                } catch (TimeoutException e) {
                    log("Timeout");
                }
                final ResultCode resultCode = unitTest.compare(file, exitCode);
                results[resultCode.getValue()]++;
                getLogWriter().println(
                        ResultsManager.printLine(file, exitCode, resultCode));
            }
            getLogWriter().println(getSummary(results));

            if ((tosave) && (getSuccess(results))) {
                final String path = ResultsManager.createPath()
                        + "."
                        + ResultsManager.EXT_JU
                                .toLowerCase(Locale.getDefault());
                unitTest.save(path);
                System.out.println("File Saved As <"
                        + (new File(path)).getCanonicalPath() + ">");
            }
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    private static final boolean getSuccess(final int[] results) {
        if ((results[ResultCode.KO.getValue()] > 0)
                || (results[ResultCode.WARNING.getValue()] > 0)) {
            return false;
        }
        return true;
    }

    private static final String getSummary(final int[] results) {
        final StringBuffer sb = new StringBuffer("\n\nValidation Tests ");

        if (getSuccess(results)) {
            sb.append("Success");
        } else {
            sb.append("Failed");
        }

        sb.append("\nsummary : number of OKs ");
        sb.append(results[ResultCode.OK.getValue()]);
        sb.append("\n          number of KOs ");
        sb.append(results[ResultCode.KO.getValue()]);
        sb.append("\n          number of WARNINGs ");
        sb.append(results[ResultCode.WARNING.getValue()]);
        sb.append("\n          number of UPDATEDs ");
        sb.append(results[ResultCode.UPDATED.getValue()]);
        sb.append("\n          number of UNKNOWNs ");
        sb.append(results[ResultCode.UNKNOWN.getValue()]);
        sb.append('\n');
        return sb.toString();
    }

    private final ISolver configureFromString(String solverconfig) {
        StringTokenizer stk = new StringTokenizer(solverconfig, ",");
        Properties pf = new Properties();
        String token;
        String[] couple;
        while (stk.hasMoreElements()) {
            token = stk.nextToken();
            couple = token.split("=");
            pf.setProperty(couple[0], couple[1]);
        }
        DataStructureFactory dsf = setupObject("DSF", pf,
                new MixedDataStructureDaniel());
        LearningStrategy learning = setupObject("LEARNING", pf,
                new LimitedLearning());
        IOrder order = setupObject("ORDER", pf, new VarOrderHeap());
        Solver solver = new Solver(new FirstUIP(), learning, dsf, order);
        learning.setSolver(solver);
        solver.setSimplifier(pf.getProperty("SIMP", "NO_SIMPLIFICATION"));
        SearchParams params = setupObject("PARAMS", pf, new SearchParams());
        solver.setSearchParams(params);
        return solver;

    }

    @SuppressWarnings("unchecked")
    private final <T> T setupObject(String component, Properties pf,
            T defaultcomp) {
        try {
            String configline = pf.getProperty(component);
            if (configline == null) {
                log("using default component " + defaultcomp + " for "
                        + component);
                return defaultcomp;
            }
            log("configuring " + component);
            String[] config = configline.split("/");
            T comp = (T) Class.forName(config[0]).newInstance();
            for (int i = 1; i < config.length; i++) {
                String[] param = config[i].split(":"); //$NON-NLS-1$
                assert param.length == 2;
                try {
                    // Check first that the property really exists
                    BeanUtils.getProperty(comp, param[0]);
                    BeanUtils.setProperty(comp, param[0], param[1]);
                } catch (Exception e) {
                    log("Problem with component " + config[0] + " " + e);
                }
            }
            return comp;
        } catch (InstantiationException e) {
            log("Problem with component " + component + " " + e);
        } catch (IllegalAccessException e) {
            log("Problem with component " + component + " " + e);
        } catch (ClassNotFoundException e) {
            log("Problem with component " + component + " " + e);
        }
        log("using default component " + defaultcomp + " for " + component);
        return defaultcomp;
    }
}

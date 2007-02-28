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

import static java.lang.System.out;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.opt.MaxSatDecorator;
import org.sat4j.opt.MinCostDecorator;
import org.sat4j.opt.MinOneDecorator;
import org.sat4j.opt.WeightedMaxSatDecorator;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ISolver;

public class GenericOptLauncher extends AbstractOptimizationLauncher {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("nls")
    private Options createCLIOptions() {
        Options options = new Options();

        options.addOption("l", "library", true,
                "specifies the name of the library used (minisat by default)");
        options.addOption("s", "solver", true,
                "specifies the name of the solver to use");
        options.addOption("t", "timeout", true,
                "specifies the timeout (in seconds)");
        options.addOption("k", "kind", true,
                "kind of problem: minone, maxsat, etc.");
        return options;
    }

    @Override
    protected void usage() {
        out.println("java -jar sat4jmax instance-name"); //$NON-NLS-1$
    }

    @Override
    protected Reader createReader(ISolver solver, String problemname) {
        if (problemname.endsWith(".wcnf")) { //$NON-NLS-1$
            return new DimacsReader(solver, "wcnf"); //$NON-NLS-1$
        }
        return new DimacsReader(solver);
    }

    @Override
    protected String getInstanceName(String[] args) {
        return args[args.length - 1];
    }

    @Override
    protected ISolver configureSolver(String[] args) {
        ISolver asolver = null;
        Options options = createCLIOptions();
        if (args.length == 0) {
            HelpFormatter helpf = new HelpFormatter();
            helpf.printHelp("java -jar sat4jopt.jar", options, true);
        } else {
            try {
                CommandLine cmd = new PosixParser().parse(options, args);

                String kind = cmd.getOptionValue("k"); //$NON-NLS-1$
                if (kind == null) { //$NON-NLS-1$
                    kind = "maxsat";
                }
                if ("minone".equalsIgnoreCase(kind)) {
                    asolver = new MinOneDecorator(SolverFactory.newDefault());
                } else if ("mincost".equalsIgnoreCase(kind)) {
                    asolver = new MinCostDecorator(SolverFactory
                            .newMiniOPBClauseCardConstrMax());
                } else {
                    assert "maxsat".equalsIgnoreCase(kind);
                    int problemindex = args.length - 1;
                    if (args[problemindex].endsWith(".wcnf")) { //$NON-NLS-1$
                        asolver = new WeightedMaxSatDecorator(SolverFactory
                                .newMiniOPBClauseCardConstrMax());
                    } else {
                        asolver = new MaxSatDecorator(SolverFactory
                                .newMini3SAT());
                    }
                }
                log(asolver.toString(COMMENT_PREFIX));
            } catch (ParseException e1) {
                HelpFormatter helpf = new HelpFormatter();
                helpf.printHelp("java -jar sat4jopt.jar", options, true);
            }
        }
        return asolver;
    }

    public static void main(String[] args) {
        AbstractLauncher lanceur = new GenericOptLauncher();
        lanceur.run(args);
    }
}

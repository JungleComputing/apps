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

import org.sat4j.minisat.SolverFactory;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.orders.VarOrderHeapObjective;
import org.sat4j.opt.PseudoOptDecorator;
import org.sat4j.reader.OPBReader2005;
import org.sat4j.reader.OPBReader2006;
import org.sat4j.reader.ObjectiveFunction;
import org.sat4j.reader.Reader;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

/**
 * Launcher especially dedicated to the pseudo boolean 05 evaluation (@link
 * http://www.cril.univ-artois.fr/PB05/).
 * 
 * @author mederic
 */
public class LanceurPseudo2005 extends AbstractOptimizationLauncher {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Lance le prouveur sur un fichier Dimacs
     * 
     * @param args
     *            doit contenir le nom d'un fichier Dimacs, eventuellement
     *            compress?.
     */
    public static void main(final String[] args) {
        final AbstractLauncher lanceur = new LanceurPseudo2005();
        lanceur.run(args);
        System.exit(lanceur.getExitCode().value());
    }

    protected ObjectiveFunction obfct;

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.Lanceur#createReader(org.sat4j.specs.ISolver)
     */
    @Override
    protected Reader createReader(ISolver solver, String problemname) {
        return new OPBReader2006(solver);
    }

    @Override
    protected void solve(IProblem problem) throws TimeoutException {
        ObjectiveFunction obj = ((OPBReader2005) getReader())
                .getObjectiveFunction();
        ((PseudoOptDecorator) problem).setObjectTiveFunction(obj);
        IOrder order = ((Solver) ((PseudoOptDecorator) problem).decorated())
                .getOrder();
        if (order instanceof VarOrderHeapObjective) {
            ((VarOrderHeapObjective) order).setObjectiveFunction(obj);
        }
        super.solve(problem);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.Lanceur#configureSolver(java.lang.String[])
     */
    @Override
    protected ISolver configureSolver(String[] args) {
        ISolver solver;
        if (args.length > 1) {
            solver = SolverFactory.instance().createSolverByName(args[0]);
        } else {
            solver = SolverFactory
                    .newMiniLearningOPBClauseCardConstrMaxSpecificOrderIncremental();
        }
        solver = new PseudoOptDecorator(solver);
        solver.setTimeout(Integer.MAX_VALUE);
        out.println(solver.toString(COMMENT_PREFIX)); //$NON-NLS-1$
        return solver;
    }

    @Override
    protected void usage() {
        out.println("java -jar sat4jPseudo instancename.opb"); //$NON-NLS-1$
    }

    @Override
    protected String getInstanceName(String[] args) {
        assert args.length == 1 || args.length == 2;
        return args[args.length - 1];
    }
}

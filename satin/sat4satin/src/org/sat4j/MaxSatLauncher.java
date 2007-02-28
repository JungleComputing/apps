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
import org.sat4j.opt.MaxSatDecorator;
import org.sat4j.opt.WeightedMaxSatDecorator;
import org.sat4j.reader.DimacsReader;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ISolver;

public class MaxSatLauncher extends AbstractOptimizationLauncher {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

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
        return args[0];
    }

    @Override
    protected ISolver configureSolver(String[] args) {
        int problemindex = args.length - 1;
        if (args[problemindex].endsWith(".wcnf")) { //$NON-NLS-1$
            return new WeightedMaxSatDecorator(SolverFactory
                    .newMinimalOPBMinPueblo());
        }
        return new MaxSatDecorator(SolverFactory.instance().defaultSolver());
    }

    public static void main(String[] args) {
        AbstractLauncher lanceur = new MaxSatLauncher();
        lanceur.run(args);
    }
}

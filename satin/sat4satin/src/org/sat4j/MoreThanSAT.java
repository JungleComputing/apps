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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.InstanceReader;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.Reader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.RemiUtils;
import org.sat4j.tools.SolutionCounter;

/**
 * This is an example of use of the SAT4J library for computing the backbone of
 * a CNF or to compute the number of solutions of a CNF. We do not claim that
 * those tools are very efficient: they were simple to write and they helped us
 * on small examples.
 * 
 * @author leberre
 */
public class MoreThanSAT {

    /**
     * This constructor is private to prevent people to use instances of that
     * class.
     * 
     */
    private MoreThanSAT() {
        // to silent PMD audit
    }

    public static void main(final String[] args) {
        final ISolver solver = SolverFactory.newMiniLearning();
        final SolutionCounter sc = new SolutionCounter(solver);
        solver.setTimeout(3600); // 1 hour timeout
        Reader reader = new InstanceReader(solver);

        // filename is given on the command line
        try {
            final IProblem problem = reader.parseInstance(args[0]);
            if (problem.isSatisfiable()) {
                System.out.println(Messages.getString("MoreThanSAT.0")); //$NON-NLS-1$
                reader.decode(problem.model(), new PrintWriter(System.out));
                IVecInt backbone = RemiUtils.backbone(solver);
                System.out
                        .println(Messages.getString("MoreThanSAT.1") + backbone); //$NON-NLS-1$
                System.out.println(Messages.getString("MoreThanSAT.2")); //$NON-NLS-1$
                System.out.println(Messages.getString("MoreThanSAT.3") //$NON-NLS-1$
                        + sc.countSolutions());
            } else {
                System.out.println(Messages.getString("MoreThanSAT.4")); //$NON-NLS-1$
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ContradictionException e) {
            System.out.println(Messages.getString("MoreThanSAT.5")); //$NON-NLS-1$
        } catch (TimeoutException e) {
            System.out.println(Messages.getString("MoreThanSAT.6")); //$NON-NLS-1$
        }
    }
}

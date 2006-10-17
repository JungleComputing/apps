package org.sat4j;

import java.io.FileNotFoundException;
import java.io.IOException;

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

/*
 * Created on 21 d�c. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * This is an example of use of the SAT4J library for computing the backbone of
 * a CNF or to compute the number of solutions of a CNF. We do not claim that
 * those tools are very efficient: they were simple to write and they helped us
 * on small examples.
 * 
 * @author leberre
 */
public class MoreThanSAT {

    public static void main(String[] args) {
        ISolver solver = SolverFactory.newMiniLearning();
        SolutionCounter sc = new SolutionCounter(solver);
        solver.setTimeout(3600); // 1 hour timeout
        Reader reader = new InstanceReader(solver);

        // filename is given on the command line
        try {
            IProblem problem = reader.parseInstance(args[0]);
            if (problem.isSatisfiable()) {
                System.out.println("Satisfiable !");
                System.out.println(reader.decode(problem.model()));
                IVecInt backbone = RemiUtils.backbone(solver);
                System.out.println("BackBone:" + backbone);
                System.out.println("Counting solutions...");
                System.out.println("Number of solutions : "
                        + sc.countSolutions());
            } else {
                System.out.println("Unsatisfiable !");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ContradictionException e) {
            System.out.println("Unsatisfiable (trivial)!");
        } catch (TimeoutException e) {
            System.out.println("Timeout, sorry!");
        }
    }
}

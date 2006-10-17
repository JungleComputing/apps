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

package org.sat4j.reader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 * Very simple Dimacs file parser. Allow solvers to read the constraints from a
 * Dimacs formatted file. It should be used that way:
 * 
 * <pre>
 * DimacsReader solver = new DimacsReader(SolverFactory.OneSolver());
 * solver.readInstance(&quot;mybench.cnf&quot;);
 * if (solver.isSatisfiable()) {
 *     // SAT case
 * } else {
 *     // UNSAT case
 * }
 * </pre>
 * 
 * That parser is not used for efficiency reasons. It will be updated with Java
 * 1.5 scanner feature.
 * 
 * @version 1.0
 * @author dlb
 * @author or
 */
public class DimacsReader implements Reader, Serializable {

    private static final long serialVersionUID = 1L;

    protected int expectedNbOfConstr; // as announced on the p cnf line

    protected final ISolver solver;

    private boolean checkConstrNb = true;

    public DimacsReader(ISolver solver) {
        this.solver = solver;
    }

    public void disableNumberOfConstraintCheck() {
        checkConstrNb = false;
    }

    /**
     * Skip comments at the beginning of the input stream.
     * 
     * @param in
     *            the input stream
     * @throws IOException
     *             if an IO problem occurs.
     */
    protected void skipComments(final LineNumberReader in) throws IOException {
        int c;

        do {
            in.mark(4);
            c = in.read();
            if (c == 'c') {
                in.readLine();
            } else {
                in.reset();
            }
        } while (c == 'c');
    }

    /**
     * @param in
     *            the input stream
     * @throws IOException
     *             iff an IO occurs
     * @throws ParseFormatException
     *             if the input stream does not comply with the DIMACS format.
     */
    protected void readProblemLine(LineNumberReader in) throws IOException,
        ParseFormatException {

        String line = in.readLine();

        if (line == null) {
            throw new ParseFormatException(
                "premature end of file: <p cnf ...> expected  on line "
                    + in.getLineNumber());
        }
        StringTokenizer stk = new StringTokenizer(line);

        if (!(stk.hasMoreTokens() && stk.nextToken().equals("p")
            && stk.hasMoreTokens() && stk.nextToken().equals("cnf"))) {
            throw new ParseFormatException(
                "problem line expected (p cnf ...) on line "
                    + in.getLineNumber());
        }

        int vars;

        // reads the max var id
        vars = Integer.parseInt(stk.nextToken());
        assert vars > 0;
        solver.newVar(vars);
        // reads the number of clauses
        expectedNbOfConstr = Integer.parseInt(stk.nextToken());
        assert expectedNbOfConstr > 0;

    }

    /**
     * @param in
     *            the input stream
     * @throws IOException
     *             iff an IO problems occurs
     * @throws ParseFormatException
     *             if the input stream does not comply with the DIMACS format.
     * @throws ContradictionException
     *             si le probl?me est trivialement inconsistant.
     */
    protected void readConstrs(LineNumberReader in) throws IOException,
        ParseFormatException, ContradictionException {
        String line;

        int realNbOfConstr = 0;

        IVecInt literals = new VecInt();

        while (true) {
            line = in.readLine();

            if (line == null) {
                // end of file
                if (literals.size() > 0) {
                    // no 0 end the last clause
                    solver.addClause(literals);
                    realNbOfConstr++;
                }

                break;
            }

            if (line.startsWith("c ")) {
                // ignore comment line
                System.out.println("Found commmented line : " + line);
                continue;
            }
            if (line.startsWith("%") && expectedNbOfConstr == realNbOfConstr) {
                System.out
                    .println("Ignoring the rest of the file (SATLIB format");
                break;
            }
            boolean added = handleConstr(line, literals);
            if (added) {
                realNbOfConstr++;
            }
        }
        if (checkConstrNb && expectedNbOfConstr != realNbOfConstr) {
            throw new ParseFormatException("wrong nbclauses parameter. Found "
                + realNbOfConstr + ", " + expectedNbOfConstr + " expected");
        }
    }

    protected boolean handleConstr(String line, IVecInt literals)
        throws ContradictionException {
        int lit;
        boolean added = false;
        Scanner scan;
        scan = new Scanner(line);
        while (scan.hasNext()) {
            lit = scan.nextInt();

            if (lit != 0) {
                literals.push(lit);
            } else {
                if (literals.size() > 0) {
                    solver.addClause(literals);
                    literals.clear();
                    added = true;
                }
            }
        }
        return added;
    }

    /**
     * Remplit un prouveur ? partir d'un fichier Dimacs.
     * 
     * @param filename
     *            le nom du fichier Dimacs (?ventuellement compress?)
     * @throws FileNotFoundException
     *             si le fichier n'est pas trouv?
     * @throws ParseFormatException
     *             si le fichier ne respecte pas le format Dimacs
     * @throws IOException
     *             pour un autre probl?me d'entr?e/sortie
     * @throws ContradictionException
     *             si le probl?me est trivialement inconsitant
     */
    public IProblem parseInstance(String filename)
        throws FileNotFoundException, ParseFormatException, IOException,
        ContradictionException {

        if (filename.endsWith(".gz")) {
            parseInstance(new LineNumberReader(new InputStreamReader(
                new GZIPInputStream(new FileInputStream(filename)))));
        } else {
            parseInstance(new LineNumberReader(new FileReader(filename)));
        }
        return solver;
    }

    /**
     * @param in
     *            the input stream
     * @throws ParseFormatException
     *             if the input stream does not comply with the DIMACS format.
     * @throws ContradictionException
     *             si le probl?me est trivialement inconsitant
     */
    public void parseInstance(LineNumberReader in) throws ParseFormatException,
        ContradictionException {
        solver.reset();
        try {
            skipComments(in);
            readProblemLine(in);
            readConstrs(in);
        } catch (IOException e) {
            throw new ParseFormatException(e);
        } catch (NumberFormatException e) {
            throw new ParseFormatException("integer value expected on line "
                + in.getLineNumber(), e);
        }
    }

    public String decode(int[] model) {
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < model.length; i++) {
            stb.append(model[i]);
            stb.append(" ");
        }
        stb.append("0");
        return stb.toString();
    }

    protected ISolver getSolver() {
        return solver;
    }
}

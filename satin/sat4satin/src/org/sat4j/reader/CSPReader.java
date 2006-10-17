package org.sat4j.reader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 * This class is a CSP to SAT translator that is able to read
 * a CSP problem using the First CSP solver competition input 
 * format and that translates it into clausal and cardinality 
 * (equality) constraints.
 * 
 * That code has not been tested very thoroughtly yet and was 
 * written very quickly to meet the competition deadline :=))
 * There is plenty of room for improvement.
 * 
 * @author leberre
 * 
 */
public class CSPReader implements Reader {

    private final ISolver solver;

    private int[][] domains;

    private Var[] vars;

    protected Relation[] relations;

    public CSPReader(ISolver solver) {
        this.solver = solver;
    }

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

    public void parseInstance(LineNumberReader in) throws ParseFormatException,
        ContradictionException {
        solver.reset();
        try {
            readProblem(in);
        } catch (NumberFormatException e) {
            throw new ParseFormatException("integer value expected on line "
                + in.getLineNumber(), e);
        }
    }

    public String decode(int[] model) {
        StringBuilder stb = new StringBuilder();
        for (int i = 0; i < vars.length; i++) {
            stb.append(vars[i].findValue(model));
            stb.append(" ");
        }
        return stb.toString();
    }

    private void readProblem(LineNumberReader in) throws ContradictionException {
        Scanner input = new Scanner(in);
        // discard problem name
        System.out.println("c reading problem " + input.nextLine());
        // read number of domain
        System.out.println("c reading domains");
        int nbdomain = input.nextInt();
        domains = new int[nbdomain][];
        for (int i = 0; i < nbdomain; i++) {
            // read domain number
            int dnum = input.nextInt();
            assert dnum == i;

            // read domain
            domains[dnum] = readArrayOfInt(input);
        }
        System.out.println("c reading variables");
        int nbvar = input.nextInt();
        vars = new Var[nbvar];
        int nbvarstocreate = 0;
        for (int i = 0; i < nbvar; i++) {
            // read var number
            int varnum = input.nextInt();
            assert varnum == i;
            // read var domain
            int vardom = input.nextInt();
            vars[varnum] = new Var(domains[vardom], nbvarstocreate);
            nbvarstocreate += domains[vardom].length;
        }
        solver.newVar(nbvarstocreate);
        for (int i = 0; i < nbvar; i++) {
            vars[i].toClause(solver);
        }
        System.out.println("c reading relations");
        // relation definition
        // read number of relations
        int nbrel = input.nextInt();
        relations = new Relation[nbrel];
        for (int i = 0; i < nbrel; i++) {
            // read relation number
            int relnum = input.nextInt();
            assert relnum == i;

            boolean forbidden = input.nextInt() == 1 ? false : true;
            int[] domains = readArrayOfInt(input);

            int nbtuples = input.nextInt();
            if (forbidden) {
                relations[relnum] = new ForbiddenRelation(domains, nbtuples);
            } else {
                manageAllowedTuples(relnum, domains, nbtuples);
            }
            // allowed/forbidden tuples
            for (int j = 0; j < nbtuples; j++) {
                int[] tuple = readArrayOfInt(input, relations[relnum].arity());
                // do something with tuple
                relations[relnum].addTuple(j, tuple);
            }
        }
        System.out.println("c reading constraints");
        // constraint definition
        int nbconstr = input.nextInt();
        for (int i = 0; i < nbconstr; i++) {
            int[] variables = readArrayOfInt(input);
            int relnum = input.nextInt();
            // manage constraint
            relations[relnum].toClause(solver, intToVar(variables));
        }
    }

    protected void manageAllowedTuples(int relnum, int[] domains, int nbtuples) {
        relations[relnum] = new AllowedRelation(domains, nbtuples);
    }

    private Var[] intToVar(int[] variables) {
        Var[] nvars = new Var[variables.length];
        for (int i = 0; i < variables.length; i++) {
            nvars[i] = vars[variables[i]];
        }
        return nvars;
    }

    private int[] readArrayOfInt(Scanner input) {
        int size = input.nextInt();
        return readArrayOfInt(input, size);
    }

    private int[] readArrayOfInt(Scanner input, int size) {
        int[] tab = new int[size];
        for (int i = 0; i < size; i++) {
            tab[i] = input.nextInt();
        }
        return tab;
    }
}

class Var {

    Map<Integer, Integer> mapping = new HashMap<Integer, Integer>();

    private final int[] domain;

    Var(int[] domain, int startid) {
        this.domain = domain;
        for (int i = 0; i < domain.length; i++) {
            mapping.put(domain[i], ++startid);
        }
    }

    int[] domain() {
        return domain;
    }

    int translate(int key) {
        return mapping.get(key);
    }

    void toClause(ISolver solver) throws ContradictionException {
        IVecInt clause = new VecInt();
        for (int key : mapping.keySet()) {
            clause.push(mapping.get(key));
        }
        // System.err.println("Adding clause: # " + clause + " = 1 ");
        solver.addClause(clause);
        solver.addAtMost(clause, 1);
    }

    int findValue(int[] model) {
        for (Map.Entry<Integer, Integer> entry : mapping.entrySet()) {
            if (model[entry.getValue() - 1] == entry.getValue()) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("BIG PROBLEM: no value for a var!");
    }
}

interface Relation {
    void addTuple(int index, int[] tuple);

    public void toClause(ISolver solver, Var[] vars)
        throws ContradictionException;

    int arity();
}

class AllowedRelation implements Relation {

    protected final int[] domains;

    protected int[][] tuples;

    private IVecInt clause;

    AllowedRelation(int[] domains, int nbtuples) {
        this.domains = domains;
        tuples = new int[nbtuples][];
    }

    public void addTuple(int index, int[] tuple) {
        tuples[index] = tuple;
    }

    public void toClause(ISolver solver, Var[] vars)
        throws ContradictionException {
        // need to find all the tuples that are not expressed here.
        clause = new VecInt();
        int[] tuple = new int[vars.length];
        find(tuple, 0, vars, solver);
    }

    private void find(int[] tuple, int n, Var[] vars, ISolver solver)
        throws ContradictionException {
        if (n == vars.length) {
            if (notPresent(tuple)) {
                clause.clear();
                for (int j = 0; j < vars.length; j++) {
                    clause.push(-vars[j].translate(tuple[j]));
                }
                // System.err.println("Adding clause:" + clause);
                solver.addClause(clause);
            }
        } else {
            int[] domain = vars[n].domain();
            for (int i = 0; i < domain.length; i++) {
                tuple[n] = domain[i];
                find(tuple, n + 1, vars, solver);
            }

        }

    }

    private boolean notPresent(int[] tuple) {
        // System.out.println("Checking:" + Arrays.asList(tuple));
        // find the first tuple begining with the same
        // initial number
        int i = 0;
        int j = 0;
        while (i < tuples.length && j < tuple.length) {
            if (tuples[i][j] < tuple[j]) {
                i++;
                j = 0;
                continue;
            }
            if (tuples[i][j] > tuple[j]) {
                return true;
            }
            j++;
        }
        return (j != tuple.length);
    }

    public int arity() {
        return domains.length;
    }
}

class ForbiddenRelation implements Relation {

    private final int[] domains;

    private int[][] tuples;

    ForbiddenRelation(int[] domains, int nbtuples) {
        this.domains = domains;
        tuples = new int[nbtuples][];
    }

    public void addTuple(int index, int[] tuple) {
        tuples[index] = tuple;
    }

    public void toClause(ISolver solver, Var[] vars)
        throws ContradictionException {
        IVecInt clause = new VecInt();
        for (int i = 0; i < tuples.length; i++) {
            clause.clear();
            for (int j = 0; j < domains.length; j++) {
                clause.push(-vars[j].translate(tuples[i][j]));
            }
            // System.err.println("Adding clause (EZ) :" + clause);
            solver.addClause(clause);
        }
    }

    public int arity() {
        return domains.length;
    }
}

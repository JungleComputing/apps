/*
 * Created on 8 sept. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.reader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * This class is a quick hack to read opb formatted files. The reader skip
 * commented lines (beginning with COMMENT_SYMBOL) and expect constraints of the
 * form: [name :] [[+|-]COEF] [*] [+|-]LIT >=|<=|= DEGREE where COEF and DEGREE
 * are plain integer and LIT is an identifier.
 * 
 * @author leberre
 */
public class GoodOPBReader implements Reader, Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    private static final String COMMENT_SYMBOL = "*";

    private ISolver solver;

    private final Map<String, Integer> map = new HashMap<String, Integer>();

    private final IVec<String> decode = new Vec<String>();

    /**
     * 
     */
    public GoodOPBReader(ISolver solver) {
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

    public void parseInstance(LineNumberReader in)
            throws ContradictionException, IOException {
        solver.reset();
        String line;
        while ((line = in.readLine()) != null) {
            // cannot trim is line is null
            line = line.trim();
            if (line.endsWith(";")) {
                line = line.substring(0, line.length() - 1);
            }
            parseLine(line);
        }
    }

    void parseLine(String line) throws ContradictionException {
        // Skip commented line
        if (line.startsWith(COMMENT_SYMBOL)) {
            return;
        }
        if (line.startsWith("p")) {
            return;
        }
        if (line.startsWith("min:") || line.startsWith("min :")) {
            return; // we will use that case later
        }
        if (line.startsWith("max:") || line.startsWith("max :")) {
            return; // we will use that case later
        }

        // skip name of constraints:
        int index = line.indexOf(":");
        if (index != -1) {
            line = line.substring(index + 1);
        }

        IVecInt lits = new VecInt();
        IVec<BigInteger> coeffs = new Vec<BigInteger>();
        Scanner stk = new Scanner(line)
                .useDelimiter("\\s*\\*\\s*|\\s*\\+\\s*|\\s+");
        while (stk.hasNext()) {
            String token = stk.next();
            if (token.equals(">=") || token.equals("<=") || token.equals("=")) {
                assert stk.hasNext();
                String tok = stk.next();
                // we need to remove + from the integer
                if (tok.startsWith("+")) {
                    tok = tok.substring(1);
                }
                BigInteger d = new BigInteger(tok);

                try {
                    // System.out.println("c constraint: " + line);
                    // System.out.println("c lits: " + lits);
                    // System.out.println("c coeffs: " + coeffs);
                    if (token.equals(">=") || token.equals("=")) {
                        solver.addPseudoBoolean(lits, coeffs, true, d);
                    }
                    if (token.equals("<=") || token.equals("=")) {
                        solver.addPseudoBoolean(lits, coeffs, false, d);
                    }
                } catch (ContradictionException ce) {
                    System.out.println("c inconsistent constraint: " + line);
                    System.out.println("c lits: " + lits);
                    System.out.println("c coeffs: " + coeffs);
                    throw ce;
                }
            } else {
                // on est toujours en train de lire la partie gauche de la
                // contrainte
                if (token.equals("+")) {
                    assert stk.hasNext();
                    token = stk.next();
                } else if (token.equals("-")) {
                    assert stk.hasNext();
                    token = token + stk.next();
                }
                BigInteger coef;
                // should contain a coef and a literal
                try {
                    // we need to remove + from the integer
                    if (token.startsWith("+")) {
                        token = token.substring(1);
                    }
                    coef = new BigInteger(token);
                    assert stk.hasNext();
                    token = stk.next();
                } catch (NumberFormatException nfe) {
                    // its only an identifier
                    coef = BigInteger.ONE;
                }
                if (token.equals("-") || token.equals("~")) {
                    assert stk.hasNext();
                    token = token + stk.next();
                }
                boolean negative = false;
                if (token.startsWith("+")) {
                    token = token.substring(1);
                } else if (token.startsWith("-")) {
                    token = token.substring(1);
                    assert coef.equals(BigInteger.ONE);
                    coef = BigInteger.ONE.negate();
                } else if (token.startsWith("~")) {
                    token = token.substring(1);
                    negative = true;
                }
                Integer id = map.get(token);
                if (id == null) {
                    map.put(token, id = solver.newVar());
                    decode.push(token);
                    assert decode.size() == id.intValue();
                }
                coeffs.push(coef);
                int lid = (negative ? -1 : 1) * id.intValue();
                lits.push(lid);
                assert coeffs.size() == lits.size();
            }
        }
    }

    public String decode(int[] model) {
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < model.length; i++) {
            if (model[i] < 0) {
                stb.append("-");
                stb.append(decode.get(-model[i] - 1));
            } else {
                stb.append(decode.get(model[i] - 1));
            }
            stb.append(" ");
        }
        return stb.toString();
    }

}
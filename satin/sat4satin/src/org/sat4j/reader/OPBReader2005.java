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
package org.sat4j.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.math.BigInteger;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * "Official" reader for the Pseudo Boolean evaluation 2005.
 * 
 * @author leberre
 * @author or
 * @author mederic baron
 */
public class OPBReader2005 extends Reader implements Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    private final ISolver solver;

    private final IVecInt lits;

    private final IVec<BigInteger> coeffs;

    private BigInteger d;

    private String operator;

    private final IVecInt objectiveVars = new VecInt();

    private final IVec<BigInteger> objectiveCoeffs = new Vec<BigInteger>();

    // does the instance have an objective function?
    private boolean hasObjFunc = false;

    private int nbVars, nbConstr; // MetaData: #Variables and #Constraints in
                                    // file.

    /**
     * callback called when we get the number of variables and the expected
     * number of constraints
     * 
     * @param nbvar:
     *            the number of variables
     * @param nbconstr:
     *            the number of contraints
     */
    protected void metaData(int nbvar, int nbconstr) {
        solver.newVar(nbvar);
    }

    /**
     * callback called before we read the objective function
     */
    protected void beginObjective() {
    }

    /**
     * callback called after we've read the objective function
     */
    protected void endObjective() {
        assert lits.size() == coeffs.size();
        assert lits.size() == coeffs.size();
        for (int i = 0; i < lits.size(); i++) {
            objectiveVars.push(lits.get(i));
            objectiveCoeffs.push(coeffs.get(i));
        }
    }

    /**
     * callback called before we read a constraint
     */
    protected void beginConstraint() {
        lits.clear();
        coeffs.clear();
        assert lits.size() == 0;
        assert coeffs.size() == 0;
    }

    /**
     * callback called after we've read a constraint
     */
    /**
     * @throws ContradictionException
     */
    protected void endConstraint() throws ContradictionException {

        assert !(lits.size() == 0);
        assert !(coeffs.size() == 0);
        assert lits.size() == coeffs.size();

        if ("<=".equals(operator) || "=".equals(operator))
            solver.addPseudoBoolean(lits, coeffs, false, d);
        if (">=".equals(operator) || "=".equals(operator))
            solver.addPseudoBoolean(lits, coeffs, true, d);
    }

    /**
     * callback called when we read a term of a constraint
     * 
     * @param coeff:
     *            the coefficient of the term
     * @param var:
     *            the identifier of the variable
     */
    protected void constraintTerm(BigInteger coeff, String var) {
        coeffs.push(coeff);
        int id = Integer.parseInt(var.substring(1));
        int lid = ((savedChar == '-') ? -1 : 1) * id;
        lits.push(lid);
    }

    /**
     * callback called when we read the relational operator of a constraint
     * 
     * @param relop:
     *            the relational oerator (>= or =)
     */
    protected void constraintRelOp(String relop) {
        operator = relop;
    }

    /**
     * callback called when we read the right term of a constraint (also known
     * as the degree)
     * 
     * @param val:
     *            the degree of the constraint
     */
    protected void constraintRightTerm(BigInteger val) {
        d = val;
    }

    transient BufferedReader in; // the stream we're reading from

    char savedChar; // a character read from the file but not yet consumed

    boolean charAvailable = false; // true iff savedChar contains a character

    boolean eofReached = false; // true iff we've reached EOF

    /**
     * get the next character from the stream
     * 
     * @throws IOException
     */
    private char get() throws IOException {
        int c;

        if (charAvailable) {
            charAvailable = false;
            return savedChar;
        }

        c = in.read();
        if (c == -1)
            eofReached = true;

        return (char) c;
    }

    public IVecInt getVars() {
        return objectiveVars;
    }

    public IVec<BigInteger> getCoeffs() {
        return objectiveCoeffs;
    }

    /**
     * put back a character into the stream (only one chr can be put back)
     */
    private void putback(char c) {
        savedChar = c;
        charAvailable = true;
    }

    /**
     * return true iff we've reached EOF
     */
    private boolean eof() {
        return eofReached;
    }

    /**
     * skip white spaces
     * 
     * @throws IOException
     */
    protected void skipSpaces() throws IOException {
        char c;

        while (Character.isWhitespace(c = get()))
            ;

        putback(c);
    }

    /**
     * read a word from file
     * 
     * @return the word we read
     * @throws IOException
     */
    public String readWord() throws IOException {
        StringBuffer s = new StringBuffer();
        char c;

        skipSpaces();

        while (!Character.isWhitespace(c = get()) && !eof())
            s.append(c);

        return s.toString();
    }

    /**
     * read a integer from file
     * 
     * @param s
     *            a StringBuffer to store the integer that was read
     * @throws IOException
     */
    public void readInteger(StringBuffer s) throws IOException {
        char c;

        skipSpaces();
        s.setLength(0);

        c = get();
        if (c == '-' || Character.isDigit(c))
            s.append(c);
        // note: BigInteger don't like a '+' before the number, we just skip it

        while (Character.isDigit(c = get()) && !eof())
            s.append(c);

        putback(c);
    }

    /**
     * read an identifier from stream and store it in s
     * 
     * @return the identifier we read or null
     * @throws IOException
     * @throws ParseFormatException
     */
    protected boolean readIdentifier(StringBuffer s) throws IOException,
            ParseFormatException {
        char c;

        s.setLength(0);

        skipSpaces();

        // first char (must be a letter or underscore)
        c = get();
        if (eof())
            return false;

        if (!Character.isLetter(c) && c != '_') {
            putback(c);
            return false;
        }

        s.append(c);

        // next chars (must be a letter, a digit or an underscore)
        while (true) {
            c = get();
            if (eof())
                break;

            if (Character.isLetter(c) || Character.isDigit(c) || c == '_')
                s.append(c);
            else {
                putback(c);
                break;
            }
        }

        // Small check on the coefficient ID to make sure everything is ok
        int varID = Integer.parseInt(s.substring(1));
        if (varID > nbVars) {
            throw new ParseFormatException(
                    "Variable identifier larger than #variables in metadata.");
        }
        return true;
    }

    /**
     * read a relational operator from stream and store it in s
     * 
     * @return the relational operator we read or null
     * @throws IOException
     */
    private String readRelOp() throws IOException {
        char c;

        skipSpaces();

        c = get();
        if (eof())
            return null;

        if (c == '=')
            return "=";

        if (c == '>' && get() == '=')
            return ">=";

        return null;
    }

    /**
     * read the first comment line to get the number of variables and the number
     * of constraints in the file calls metaData with the data that was read
     * 
     * @throws IOException
     * @throws ParseException
     */
    private void readMetaData() throws IOException, ParseFormatException {
        char c;
        String s;

        // get the number of variables and constraints
        c = get();
        if (c != '*')
            throw new ParseFormatException(
                    "First line of input file should be a comment");

        s = readWord();
        if (eof() || !"#variable=".equals(s))
            throw new ParseFormatException(
                    "First line should contain #variable= as first keyword");

        nbVars = Integer.parseInt(readWord());

        s = readWord();
        if (eof() || !"#constraint=".equals(s))
            throw new ParseFormatException(
                    "First line should contain #constraint= as second keyword");

        nbConstr = Integer.parseInt(readWord());

        // skip the rest of the line
        in.readLine();

        // callback to transmit the data
        metaData(nbVars, nbConstr);
    }

    /**
     * skip the comments at the beginning of the file
     * 
     * @throws IOException
     */
    private void skipComments() throws IOException {
        char c = ' ';

        // skip further comments

        while (!eof() && (c = get()) == '*') {
            in.readLine();
        }

        putback(c);
    }

    /**
     * read a term into coeff and var
     * 
     * @param coeff:
     *            the coefficient of the variable
     * @param var:
     *            the indentifier we read
     * @throws IOException
     * @throws ParseException
     */
    protected void readTerm(StringBuffer coeff, StringBuffer var)
            throws IOException, ParseFormatException {
        char c;

        readInteger(coeff);

        skipSpaces();
        c = get();
        if (c != '*')
            throw new ParseFormatException(
                    "'*' expected between a coefficient and a variable");

        if (!readIdentifier(var))
            throw new ParseFormatException("identifier expected");
    }

    /**
     * read the objective line (if any) calls beginObjective, objectiveTerm and
     * endObjective
     * 
     * @throws IOException
     * @throws ParseException
     */
    private void readObjective() throws IOException, ParseFormatException {
        char c;
        StringBuffer var = new StringBuffer();
        StringBuffer coeff = new StringBuffer();

        // read objective line (if any)

        skipSpaces();
        c = get();
        if (c != 'm') {
            // no objective line
            putback(c);
            return;
        }

        hasObjFunc = true;
        if (get() == 'i' && get() == 'n' && get() == ':') {
            beginObjective(); // callback

            while (!eof()) {
                readTerm(coeff, var);
                constraintTerm(new BigInteger(coeff.toString()), var.toString()); // callback

                skipSpaces();
                c = get();
                if (c == ';')
                    break; // end of objective

                else if (c == '-' || c == '+' || Character.isDigit(c))
                    putback(c);
                else
                    throw new ParseFormatException(
                            "unexpected character in objective function");
            }

            endObjective();
        } else
            throw new ParseFormatException(
                    "input format error: 'min:' expected");
    }

    /**
     * read a constraint calls beginConstraint, constraintTerm and endConstraint
     * 
     * @throws ParseException
     * @throws IOException
     * @throws ContradictionException
     */
    private void readConstraint() throws IOException, ParseFormatException,
            ContradictionException {
        StringBuffer var = new StringBuffer();
        StringBuffer coeff = new StringBuffer();
        char c;

        beginConstraint();

        while (!eof()) {
            readTerm(coeff, var);
            constraintTerm(new BigInteger(coeff.toString()), var.toString());

            skipSpaces();
            c = get();
            if (c == '>' || c == '=') {
                // relational operator found
                putback(c);
                break;
            } else if (c == '-' || c == '+' || Character.isDigit(c))
                putback(c);
            else {
                throw new ParseFormatException(
                        "unexpected character in constraint");
            }
        }

        if (eof())
            throw new ParseFormatException(
                    "unexpected EOF before end of constraint");

        String relop;
        if ((relop = readRelOp()) == null) {
            throw new ParseFormatException(
                    "unexpected relational operator in constraint");

        }
        constraintRelOp(relop);
        readInteger(coeff);
        constraintRightTerm(new BigInteger(coeff.toString()));

        skipSpaces();
        c = get();
        if (eof() || c != ';')
            throw new ParseFormatException(
                    "semicolon expected at end of constraint");

        endConstraint();
    }

    public OPBReader2005(ISolver solver) {
        this.solver = solver;
        lits = new VecInt();
        coeffs = new Vec<BigInteger>();
    }

    /**
     * parses the file and uses the callbacks to send to send the data back to
     * the program
     * 
     * @throws IOException
     * @throws ParseException
     * @throws ContradictionException
     */
    public void parse() throws IOException, ParseFormatException,
            ContradictionException {
        readMetaData();

        skipComments();

        readObjective();

        // read constraints
        int nbConstraintsRead = 0;
        char c;
        while (!eof()) {
            skipSpaces();
            if (eof())
                break;

            c = get();
            putback(c);
            if (c == '*')
                skipComments();

            if (eof())
                break;

            readConstraint();
            nbConstraintsRead++;
        }
        // Small check on the number of constraints
        if (nbConstraintsRead != nbConstr) {
            throw new ParseFormatException(
                    "Number of constraints read is different from metadata.");
        }
    }

    @Override
    public final IProblem parseInstance(final java.io.Reader in)
            throws ParseFormatException, ContradictionException {
        return parseInstance(new LineNumberReader(in));
    }

    private IProblem parseInstance(LineNumberReader in)
            throws ParseFormatException, ContradictionException {
        solver.reset();
        this.in = in;
        try {
            parse();
            return solver;
        } catch (IOException e) {
            throw new ParseFormatException(e);
        }
    }

    @Override
    public String decode(int[] model) {
        StringBuffer stb = new StringBuffer();

        for (int i = 0; i < model.length; i++) {
            if (model[i] < 0) {
                stb.append("-x");
                stb.append(-model[i]);
            } else {
                stb.append("x");
                stb.append(model[i]);
            }
            stb.append(" ");
        }
        return stb.toString();
    }

    @Override
    public void decode(int[] model, PrintWriter out) {
        for (int i = 0; i < model.length; i++) {
            if (model[i] < 0) {
                out.print("-x");
                out.print(-model[i]);
            } else {
                out.print("x");
                out.print(model[i]);
            }
            out.print(" ");
        }
    }

    public ObjectiveFunction getObjectiveFunction() {
        if (hasObjFunc)
            return new ObjectiveFunction(getVars(), getCoeffs());
        return null;
    }

}
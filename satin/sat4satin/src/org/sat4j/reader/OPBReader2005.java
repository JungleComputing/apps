package org.sat4j.reader;

import java.io.BufferedReader;
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
import java.util.zip.GZIPInputStream;

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
public class OPBReader2005 implements Reader, Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    private ISolver solver;

    private final IVec<String> decode = new Vec<String>();

    private IVecInt lits;

    private IVec<BigInteger> coeffs;

    private BigInteger d;

    private String operator;

    private final Map<String, Integer> map = new HashMap<String, Integer>();

    private final IVecInt objectiveVars = new VecInt();

    private final IVec<BigInteger> objectiveCoeffs = new Vec<BigInteger>();

    // does the instance have an objective function?
    private boolean hasObjFunc = false;

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
        // try {
        // solver.addPseudoBoolean(lits, coeffs, false, Integer.MAX_VALUE);
        // } catch (ContradictionException e) {
        // e.printStackTrace();
        // }

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

        if (operator.equals("<=") || operator.equals("=")) {
            solver.addPseudoBoolean(lits, coeffs, false, d);
        }
        if (operator.equals(">=") || operator.equals("=")) {
            solver.addPseudoBoolean(lits, coeffs, true, d);
        }
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
        Integer id = map.get(var);
        if (id == null) {
            assert map.size() < solver.nVars();
            map.put(var, id = map.size() + 1);
            assert id > 0;
            assert id <= solver.nVars();
            decode.push(var);
            assert decode.size() == id;
        }
        int lid = ((savedChar == '-') ? -1 : 1) * id.intValue();
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
        if (c == -1) {
            eofReached = true;
        }

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
    private void skipSpaces() throws IOException {
        char c;

        while (Character.isWhitespace(c = get())) {
            ;
        }

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

        while (!Character.isWhitespace(c = get()) && !eof()) {
            s.append(c);
        }

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
        if (c == '-' || Character.isDigit(c)) {
            s.append(c);
            // note: BigInteger don't like a '+' before the number, we just skip it
        }

        while (Character.isDigit(c = get()) && !eof()) {
            s.append(c);
        }

        putback(c);
    }

    /**
     * read an identifier from stream and store it in s
     * 
     * @return the identifier we read or null
     * @throws IOException
     */
    private boolean readIdentifier(StringBuffer s) throws IOException {
        char c;

        s.setLength(0);

        skipSpaces();

        // first char (must be a letter or underscore)
        c = get();
        if (eof()) {
            return false;
        }

        if (!Character.isLetter(c) && c != '_') {
            putback(c);
            return false;
        }

        s.append(c);

        // next chars (must be a letter, a digit or an underscore)
        while (true) {
            c = get();
            if (eof()) {
                break;
            }

            if (Character.isLetter(c) || Character.isDigit(c) || c == '_') {
                s.append(c);
            } else {
                putback(c);
                break;
            }
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
        if (eof()) {
            return null;
        }

        if (c == '=') {
            return "=";
        }

        if (c == '>' && get() == '=') {
            return ">=";
        }

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
        int nbVars, nbConstr;

        // get the number of variables and constraints
        c = get();
        if (c != '*') {
            throw new ParseFormatException(
                "First line of input file should be a comment");
        }

        s = readWord();
        if (eof() || !s.equals("#variable=")) {
            throw new ParseFormatException(
                "First line should contain #variable= as first keyword");
        }

        nbVars = Integer.parseInt(readWord());

        s = readWord();
        if (eof() || !s.equals("#constraint=")) {
            throw new ParseFormatException(
                "First line should contain #constraint= as second keyword");
        }

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
    private void readTerm(StringBuffer coeff, StringBuffer var)
        throws IOException, ParseFormatException {
        char c;

        readInteger(coeff);

        skipSpaces();
        c = get();
        if (c != '*') {
            throw new ParseFormatException(
                "'*' expected between a coefficient and a variable");
        }

        if (!readIdentifier(var)) {
            throw new ParseFormatException("identifier expected");
        }
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
                if (c == ';') {
                    break; // end of objective
                } else if (c == '-' || c == '+' || Character.isDigit(c)) {
                    putback(c);
                } else {
                    throw new ParseFormatException(
                        "unexpected character in objective function");
                }
            }

            endObjective();
        } else {
            throw new ParseFormatException(
                "input format error: 'min:' expected");
        }
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
            } else if (c == '-' || c == '+' || Character.isDigit(c)) {
                putback(c);
            } else {
                throw new ParseFormatException(
                    "unexpected character in constraint");
            }
        }

        if (eof()) {
            throw new ParseFormatException(
                "unexpected EOF before end of constraint");
        }

        String relop;
        if ((relop = readRelOp()) != null) {
            constraintRelOp(relop);
        } else {
            throw new ParseFormatException(
                "unexpected relational operator in constraint");
        }

        readInteger(coeff);
        constraintRightTerm(new BigInteger(coeff.toString()));

        skipSpaces();
        c = get();
        if (eof() || c != ';') {
            throw new ParseFormatException(
                "semicolon expected at end of constraint");
        }

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
        while (!eof()) {
            skipSpaces();
            if (eof()) {
                break;
            }

            readConstraint();
        }
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
        this.in = in;
        try {
            parse();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

    public ObjectiveFunction getObjectiveFunction() {
        if (hasObjFunc) {
            return new ObjectiveFunction(getVars(), getCoeffs());
        }
        return null;
    }

}

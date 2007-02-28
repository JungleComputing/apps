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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

/**
 * An reader having the responsability to choose the right reader according to
 * the input.
 * 
 * @author leberre
 */
public class InstanceReader extends Reader {

    private AAGReader aag;

    private AIGReader aig;

    private DimacsReader ezdimacs;

    private LecteurDimacs dimacs;

    private GoodOPBReader opb;

    private ExtendedDimacsReader edimacs;

    private CSPReader csp;

    private CSPReader csp2;

    private CSPReader csp3;

    private XMLCSPReader xmlcsp;

    private Reader reader = null;

    private final ISolver solver;

    public InstanceReader(ISolver solver) {
        // dimacs = new DimacsReader(solver);
        this.solver = solver;
    }

    private Reader getDefaultSATReader() {
        if (dimacs == null) {
            dimacs = new LecteurDimacs(solver);// new LecteurDimacs(solver);
        }
        return dimacs;
    }

    private Reader getEZSATReader() {
        if (ezdimacs == null) {
            ezdimacs = new DimacsReader(solver);// new LecteurDimacs(solver);
        }
        return ezdimacs;
    }

    private Reader getDefaultOPBReader() {
        if (opb == null) {
            opb = new GoodOPBReader(solver);
        }
        return opb;
    }

    private Reader getDefaultExtendedDimacsReader() {
        if (edimacs == null) {
            edimacs = new ExtendedDimacsReader(solver);
        }
        return edimacs;
    }

    private Reader getCSPReader1() {
        if (csp == null) {
            csp = new CSPReader(solver);
        }
        return csp;
    }

    private Reader getCSPReader2() {
        if (csp2 == null) {
            csp2 = new CSPSupportReader(solver);
        }
        return csp2;
    }

    private Reader getCSPReader3() {
        if (csp3 == null) {
            csp3 = new CSPExtSupportReader(solver);
        }
        return csp3;
    }

    private Reader getXMLCSPReader() {
        if (xmlcsp == null) {
            xmlcsp = new XMLCSPReader(solver);
        }
        return xmlcsp;
    }

    private Reader getAIGReader() {
        if (aig == null) {
            aig = new AIGReader(solver);
        }
        return aig;
    }

    private Reader getAAGReader() {
        if (aag == null) {
            aag = new AAGReader(solver);
        }
        return aag;
    }

    @Override
    public IProblem parseInstance(String filename)
            throws FileNotFoundException, ParseFormatException, IOException,
            ContradictionException {
        String fname;
        boolean isHttp = false;
        String tempFileName = "";
        String prefix = "";

        if (filename.startsWith("http://")) {
            isHttp = true;
            tempFileName = filename;
            filename = filename.substring(filename.lastIndexOf('/'), filename
                    .length() - 1);
        }

        if (filename.indexOf(':') != -1) {

            String[] parts = filename.split(":");
            filename = parts[1];
            prefix = parts[0].toUpperCase();

        }

        if (filename.endsWith(".gz")) {
            fname = filename.substring(0, filename.lastIndexOf('.'));
        } else {
            fname = filename;
        }
        if ("EZCNF".equals(prefix)) {
            reader = getEZSATReader();
        } else if ("CSP".equals(prefix)) {
            reader = getCSPReader1();
        } else if ("CSP3".equals(prefix)) {
            reader = getCSPReader3();
        } else if (fname.endsWith(".txt") || "CSP2".equals(prefix)) {
            reader = getCSPReader2();
        } else if (fname.endsWith(".opb") || "PB".equals(prefix)) {
            reader = getDefaultOPBReader();
        } else if (fname.endsWith(".edimacs") || fname.endsWith(".ncnf")
                || "EDIMACS".equals(prefix)) {
            reader = getDefaultExtendedDimacsReader();
        } else if (fname.endsWith(".xml")) {
            reader = getXMLCSPReader();
        } else if (fname.endsWith(".aag")) {
            reader = getAAGReader();
        } else if (fname.endsWith(".aig")) {
            reader = getAIGReader();

        } else {
            reader = getDefaultSATReader();
        }

        if (isHttp) {
            return reader.parseInstance((new URL(tempFileName)).openStream());
        }
        return reader.parseInstance(filename);
    }

    @Override
    public String decode(int[] model) {
        return reader.decode(model);
    }

    @Override
    public void decode(int[] model, PrintWriter out) {
        reader.decode(model, out);
    }

    @Override
    public IProblem parseInstance(java.io.Reader in)
            throws ParseFormatException, ContradictionException, IOException {
        throw new UnsupportedOperationException();
    }
}

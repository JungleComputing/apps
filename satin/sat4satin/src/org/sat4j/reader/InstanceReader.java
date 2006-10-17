/*
 * Created on 3 juin 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.reader;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;

/**
 * An reader having the responsability to choose the right reader according to
 * the input.
 * 
 * @author leberre
 */
public class InstanceReader implements Reader {

    // private final DimacsReader dimacs;
    private final LecteurDimacs dimacs;

    private final GoodOPBReader opb;

    private final ExtendedDimacsReader edimacs;

    private final CSPReader csp;

    private final CSPReader csp2;
    
    private Reader reader = null;

    public InstanceReader(ISolver solver) {
        // dimacs = new DimacsReader(solver);
        dimacs = new LecteurDimacs(solver);
        opb = new GoodOPBReader(solver);
        edimacs = new ExtendedDimacsReader(solver);
        csp = new CSPReader(solver);
        csp2 = new CSPSupportReader(solver);
    }

    public IProblem parseInstance(String filename)
            throws FileNotFoundException, ParseFormatException, IOException,
            ContradictionException {
        String fname;
        String prefix = "";
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
        if (prefix.equals("CSP")) {
            reader = csp;
        } else if (fname.endsWith(".txt") || prefix.equals("CSP2")) {
            reader = csp2;
        } else if (fname.endsWith(".opb") || prefix.equals("PB")) {
            reader = opb;
        } else if (fname.endsWith(".edimacs") || fname.endsWith(".ncnf")
                || prefix.equals("EDIMACS")) {
            reader = edimacs;
        } else {
            reader = dimacs;
        }
        return reader.parseInstance(filename);
    }

    public String decode(int[] model) {
        return reader.decode(model);
    }
}

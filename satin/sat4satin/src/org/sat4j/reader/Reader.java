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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;

/**
 * A reader is responsible to feed an ISolver from a text file and to convert
 * the model found by the solver to a textual representation.
 * 
 * @author leberre
 */
public abstract class Reader {

    public IProblem parseInstance(final String filename)
            throws FileNotFoundException, ParseFormatException, IOException,
            ContradictionException {
        InputStream in;
        if (filename.startsWith("http://")) {
            in = (new URL(filename)).openStream();
        } else {
            in = new FileInputStream(filename);
        }
        if (filename.endsWith(".gz")) {
            in = new GZIPInputStream(in);
        }
        return parseInstance(in);
    }

    public IProblem parseInstance(final InputStream in)
            throws ParseFormatException, ContradictionException, IOException {
        return parseInstance(new InputStreamReader(in));
    }

    public abstract IProblem parseInstance(final java.io.Reader in)
            throws ParseFormatException, ContradictionException, IOException;

    /**
     * Produce a model using the reader format.
     * 
     * @param model
     *            a model using the Dimacs format.
     * @return a human readable view of the model.
     */
    @Deprecated
    public abstract String decode(int[] model);

    /**
     * Produce a model using the reader format on a provided printwriter.
     * 
     * @param model
     *            a model using the Dimacs format.
     * @param out
     *            the place where to display the model
     */
    public abstract void decode(int[] model, PrintWriter out);

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbosity(boolean b) {
        verbose = b;
    }

    private boolean verbose = false;
}

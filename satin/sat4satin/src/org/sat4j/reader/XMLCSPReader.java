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
import java.io.Reader;

import javax.xml.parsers.ParserConfigurationException;

import org.sat4j.csp.xml.CspXmlParser;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.xml.sax.SAXException;

public class XMLCSPReader extends org.sat4j.reader.Reader {

    private final CSPReader cspreader;

    public XMLCSPReader(ISolver solver) {
        cspreader = new CSPSupportReader(solver);
    }

    @Override
    public String decode(int[] model) {
        return cspreader.decode(model);
    }

    @Override
    public void decode(int[] model, PrintWriter out) {
        cspreader.decode(model, out);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.reader.Reader#parseInstance(java.lang.String)
     */
    @Override
    public IProblem parseInstance(String filename)
            throws FileNotFoundException, ParseFormatException, IOException,
            ContradictionException {
        try {
            CspXmlParser.parse(cspreader, filename);
        } catch (SAXException e) {
            throw new ParseFormatException(e);
        } catch (ParserConfigurationException e) {
            throw new ParseFormatException(e);
        }
        return cspreader.getProblem();
    }

    @Override
    public IProblem parseInstance(Reader in) throws ParseFormatException,
            ContradictionException, IOException {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.reader.Reader#setVerbosity(boolean)
     */
    @Override
    public void setVerbosity(boolean b) {
        super.setVerbosity(b);
        cspreader.setVerbosity(b);
    }

}

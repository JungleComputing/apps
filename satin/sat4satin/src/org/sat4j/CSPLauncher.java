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
package org.sat4j;

import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.CSPExtSupportReader;
import org.sat4j.reader.Reader;
import org.sat4j.reader.XMLCSPReader;
import org.sat4j.specs.ISolver;

public class CSPLauncher extends AbstractLauncher {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.Lanceur#configureSolver(java.lang.String[])
     */
	@Override
	protected ISolver configureSolver(String[] args) {
		return SolverFactory.newMiniSAT2Heap();
	}

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.Lanceur#createReader(org.sat4j.specs.ISolver)
     */
    @Override
    protected Reader createReader(final ISolver solver, final String problemname) {
        Reader reader;
        if (problemname.endsWith(".txt")) {
            reader = new CSPExtSupportReader(solver);
        } else {
            assert problemname.endsWith(".xml");
            reader = new XMLCSPReader(solver);
        }
        if (System.getProperty("verbose") != null) {
            log("verbose mode on");
            reader.setVerbosity(true);
        }
        return reader;
    }

    public static void main(String[] args) {
        AbstractLauncher lanceur = new CSPLauncher();
        lanceur.run(args);
    }

    @Override
    protected void usage() {
        System.out.println("java -jar sat4jCSP instance-name"); //$NON-NLS-1$
    }

    @Override
    protected String getInstanceName(String[] args) {
        assert args.length == 1;
        return args[0];
    }

}

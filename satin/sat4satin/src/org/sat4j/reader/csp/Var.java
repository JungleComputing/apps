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
package org.sat4j.reader.csp;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

public class Var implements Evaluable {

    private final Domain domain;

    private final String id;

    private final int startid;

    public Var(String idvar, Domain domain, int lastvarnumber) {
        this.domain = domain;
        this.id = idvar;
        this.startid = lastvarnumber + 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.reader.csp.Evaluable#domain()
     */
    public Domain domain() {
        return domain;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.reader.csp.Evaluable#translate(int)
     */
    public int translate(int key) {
        return domain.pos(key) + startid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.reader.csp.Evaluable#toClause(org.sat4j.specs.ISolver)
     */
    public void toClause(ISolver solver) throws ContradictionException {
        IVecInt clause = new VecInt(domain.size());
        for (int i = 0; i < domain.size(); i++)
            clause.push(i + startid);
        solver.addClause(clause);
        solver.addAtMost(clause, 1);
    }

    public int findValue(int[] model) {
        for (int i = 0; i < domain.size(); i++) {
            int varnum = i + startid;
            if (model[varnum - 1] == varnum)
                return domain.get(i);
        }
        throw new RuntimeException("BIG PROBLEM: no value for a var!");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return id;
    }

}
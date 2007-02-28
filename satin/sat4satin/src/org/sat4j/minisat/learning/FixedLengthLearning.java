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

package org.sat4j.minisat.learning;

/**
 * A learning scheme for learning constraints of size smaller than a given
 * constant.
 * 
 * @author leberre
 */
public class FixedLengthLearning extends LimitedLearning {

    private static final long serialVersionUID = 1L;

    private int maxlength;

    public FixedLengthLearning() {
        this(3);
    }

    public FixedLengthLearning(int maxlength) {
        this.maxlength = maxlength;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.LimitedLearning#learningCondition(org.sat4j.minisat.Constr)
     */
    @Override
    public void init() {
        setBound(maxlength);
    }

    public void setMaxLength(int v) {
        maxlength = v;
    }

    public int getMaxLength() {
        return maxlength;
    }

    @Override
    public String toString() {
        return "Limit learning to clauses of size smaller or equal to " //$NON-NLS-1$
                + maxlength;
    }
}

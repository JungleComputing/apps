/*
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas E???n and Niklas S???rensson. Proceedings of
 * the Sixth International Conference on Theory and Applications of
 * Satisfiability Testing, LNCS 2919, pp 502-518, 2003.
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

import java.io.Serializable;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.DataStructureFactory;

/**
 * MiniSAT learning scheme.
 * 
 * @author leberre
 */
public class MiniSATLearning extends AbstractLearning implements Serializable, Cloneable {
    private static final long serialVersionUID = 1L;

    private DataStructureFactory dsf;

    public void setDataStructureFactory(DataStructureFactory dsf) {
        this.dsf = dsf;
    }

    @Override
    public void init() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.LearningScheme#learns(org.sat4j.minisat.Solver,
     *      org.sat4j.minisat.datatype.Vec)
     */
    @Override
    public void learns(Constr constr) {
        // va contenir une nouvelle clause ou null si la clause est unitaire
        claBumpActivity(constr);
        dsf.learnConstraint(constr);
    }

    @Override
    public Object clone()
    {
	MiniSATLearning clone;

	clone = (MiniSATLearning) super.clone();

	return clone;
    }
}

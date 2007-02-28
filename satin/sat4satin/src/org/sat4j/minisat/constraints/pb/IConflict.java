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
package org.sat4j.minisat.constraints.pb;

import java.math.BigInteger;

import org.sat4j.minisat.core.VarActivityListener;

public interface IConflict extends IDataStructurePB {

    /**
     * Effectue une resolution avec une contrainte PB. Met a jour le Conflict.
     * 
     * @param cpb
     *            contrainte avec laquelle on va faire la resolution
     * @param litImplied
     *            litteral devant etre resolu
     * @param val
     *            TODO
     * @return la mise a jour du degre
     */
    BigInteger resolve(PBConstr cpb, int litImplied, VarActivityListener val);

    BigInteger slackConflict();

    boolean isAssertive(int dl);

    /**
     * Reduction d'une contrainte On supprime un litteral non assigne
     * prioritairement, vrai sinon. En aucun cas on ne supprime litImplied.
     * 
     * @return mise a jour du degre
     */
    public BigInteger reduceInConstraint(WatchPb wpb,
            final BigInteger[] coefsBis, final int indLitImplied,
            final BigInteger degreeBis);

    /**
     * retourne le niveau de backtrack : c'est-?-dire le niveau le plus haut
     * pour lequel la contrainte est assertive
     * 
     * @param maxLevel
     *            le plus bas niveau pour lequel la contrainte est assertive
     * @return the highest level (smaller int) for which the constraint is
     *         assertive.
     */
    public int getBacktrackLevel(int maxLevel);

    public void updateSlack(int level);

}

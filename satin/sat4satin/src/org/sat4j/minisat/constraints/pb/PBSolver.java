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

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.AssertingClauseGenerator;
import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.Handle;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.Solver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;

/**
 * @author parrain To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PBSolver extends Solver {

    private static final long serialVersionUID = 1L;

    /**
     * @param acg
     * @param learner
     * @param dsf
     */
    public PBSolver(AssertingClauseGenerator acg, LearningStrategy learner,
            DataStructureFactory dsf, IOrder order) {
        super(acg, learner, dsf, new SearchParams(10000.0, 100), order);
    }

    @Override
    public int analyze(Constr myconfl, Handle<Constr> outLearntRef) {

        // Premier litt?ral impliqu? dans le conflit
        int litImplied = trail.last();
        int currentLevel = voc.getLevel(litImplied);

        IConflict confl = chooseConflict(myconfl, currentLevel);
        BigInteger resDegree = confl.getDegree();
        assert confl.slackConflict().signum() < 0;
        while (!confl.isAssertive(currentLevel)) {
            // On effectue la r?solution

            PBConstr constraint = (PBConstr) voc.getReason(litImplied);
            if (constraint != null) {
                // on effectue la resolution
                // le resultat est dans le conflit
                resDegree = confl.resolve(constraint, litImplied, this);
                assert confl.slackConflict().signum() <= 0;
            }
            // On remonte l'arbre des implications
            if (trail.size() == 1)
                break;
            undoOne();
            assert decisionLevel() > 0;
            litImplied = trail.last();
            if (voc.getLevel(litImplied) != currentLevel) {
                trailLim.pop();
                confl.updateSlack(voc.getLevel(litImplied));
            }
            assert voc.getLevel(litImplied) <= currentLevel;
            currentLevel = voc.getLevel(litImplied);
            assert currentLevel == decisionLevel();
            assert litImplied > 1;
        }

        assert currentLevel == decisionLevel();
        assert decisionLevel() != 0;

        undoOne();

        // On reprend les ?l?ments n?cessaires ? la construction d'une
        // PB-contrainte
        // ? partir du conflit
        IVecInt resLits = new VecInt();
        IVec<BigInteger> resCoefs = new Vec<BigInteger>();
        confl.buildConstraintFromConflict(resLits, resCoefs);

        assert resLits.size() == resCoefs.size();

        if (resLits.size() == 0) {
            outLearntRef.obj = null;
            return -1;
        }

        // On construit la contrainte assertive et on la reference
        PBConstr resConstr = (PBConstr) dsfactory
                .createUnregisteredPseudoBooleanConstraint(resLits, resCoefs,
                        resDegree);

        outLearntRef.obj = resConstr;
        // on recupere le niveau de decision le plus haut qui est inferieur a
        // currentlevel
        assert confl.isAssertive(currentLevel);
        return confl.getBacktrackLevel(currentLevel);
    }

    IConflict chooseConflict(Constr myconfl, int level) {
        return ConflictMap.createConflict((PBConstr) myconfl, level);
    }

    @Override
    public String toString(String prefix) {
        return prefix + "Cutting planes based inference ("
                + this.getClass().getName() + ")\n" + super.toString(prefix);
    }

}
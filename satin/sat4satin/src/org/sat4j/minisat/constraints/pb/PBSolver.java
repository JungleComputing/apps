/*
 * Created on Jun 3, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
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
            DataStructureFactory dsf,IOrder order) {
        super(acg, learner, dsf,order);
    }

    @Override
    public int analyze(Constr myconfl, Handle<Constr> outLearntRef) {
        // Logger logger = Logger.getLogger("org.sat4j.minisat.constraints.pb");

        Conflict confl = ((WatchPb) myconfl).createConflict();
        BigInteger resDegree = confl.getDegree();

        // logger.fine("Analyse");
        // logger.fine("Init " + confl);
        // Premier litt?ral impliqu? dans le conflit

        int litImplied = trail.last();
        int currentLevel = voc.getLevel(litImplied);
        int originalLevel = currentLevel;
        assert confl.slackConflict().signum() < 0;

        while (!confl.isAssertive(currentLevel)) {
            // logger.fine("Resolving on " + Lits.toString(litImplied) + "@"
            // + currentLevel);

            // On effectue la r?solution
            WatchPb constraint = (WatchPb) voc.getReason(litImplied);
            if (constraint != null) {
                // logger.fine("Res with " + Lits.toString(litImplied) + " on "
                // + constraint);

                // on effectue la resolution
                // le resultat est dans le conflit
                resDegree = confl.resolve(constraint, litImplied);
                assert confl.slackConflict().signum() <= 0;
                // assert !confl.isTriviallyUnsat();
                // } else {
                // logger.fine("No reason for " + Lits.toString(litImplied));
            }
            // On remonte l'arbre des implications
            if (trail.size() == 1)
                break;
            undoOne();
            if (decisionLevel() > 0) {
                litImplied = trail.last();
                if (voc.getLevel(litImplied) != currentLevel)
                    trailLim.pop();
                currentLevel = voc.getLevel(litImplied);
            } else
                break;
            assert currentLevel == decisionLevel();
            assert litImplied > 1;
        }
        assert currentLevel == decisionLevel();
        if (decisionLevel() == 0) {
            outLearntRef.obj = null;
            return -1;
        }

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
        WatchPb resConstr = (WatchPb) dsfactory
                .createUnregisteredPseudoBooleanConstraint(resLits, resCoefs,
                        resDegree);

        outLearntRef.obj = resConstr;
        // logger.fine("Contrainte apprise : " + resConstr);
        // on recupere le niveau de decision le plus haut qui est inferieur a
        // currentlevel
        assert confl.isAssertive(currentLevel);
        int bl = resConstr.getBacktrackLevel(currentLevel);
        // logger.fine("Backtrack level : " + bl);
        return bl;
    }

}
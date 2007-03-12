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

package org.sat4j.minisat.core;

import static org.sat4j.minisat.core.LiteralsUtils.neg;
import static org.sat4j.minisat.core.LiteralsUtils.var;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.constraints.cnf.WLClause;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

// SATIN BEGIN
import ibis.satin.SatinObject;
import java.util.Enumeration;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.Properties;
// SATIN END

/**
 * The backbone of the library providing the modular implementation of a MiniSAT
 * (Chaff) like solver.
 * 
 * @author leberre
 *
 * Modified to use Ibis/Satin by Kees Verstoep (versto@cs.vu.nl)
 */

// pre-SATIN: public class Solver implements ISolver, UnitPropagationListener,
//            ActivityListener, Learner {
public class Solver extends SatinObject
                    implements ISolver, UnitPropagationListener,
			       ActivityListener, Learner, Serializable
{

    private static final long serialVersionUID = 1L;

    private static final double CLAUSE_RESCALE_FACTOR = 1e-20;

    private static final double CLAUSE_RESCALE_BOUND = 1 / CLAUSE_RESCALE_FACTOR;

    /**
     * List des contraintes du probl?me.
     */
    // pre-SATIN: private final IVec<Constr> constrs = new Vec<Constr>(); // Constr
    private IVec<Constr> constrs = new Vec<Constr>(); // Constr

    /**
     * Liste des clauses apprises.
     */
    private final IVec<Constr> learnts = new Vec<Constr>(); // Clause

    /**
     * incr?ment pour l'activit? des clauses.
     */
    private double claInc = 1.0;

    /**
     * decay factor pour l'activit? des clauses.
     */
    private double claDecay = 1.0;

    /**
     * Queue de propagation
     */
    // private final IntQueue propQ = new IntQueue(); // Lit
    // head of the queue in trail ... (taken from MiniSAT 1.14)
    private int qhead = 0;

    // queue

    /**
     * affectation en ordre chronologique
     */
    protected final IVecInt trail = new VecInt(); // lit

    // vector

    /**
     * indice des s?parateurs des diff?rents niveau de d?cision dans trail
     */
    protected final IVecInt trailLim = new VecInt(); // int

    // vector

    /**
     * S?pare les hypoth?ses incr?mentale et recherche
     */
    protected int rootLevel;

    private int[] model = null;

    protected ILits voc;

    private IOrder order;

    private final ActivityComparator comparator = new ActivityComparator();

    // pre-SATIN: private final SolverStats stats = new SolverStats();
    protected SolverStats stats = new SolverStats();

    private final LearningStrategy learner;

    protected final AssertingClauseGenerator analyzer;

    private boolean undertimeout;

    private long timeout = Integer.MAX_VALUE;

    protected DataStructureFactory dsfactory;

    private SearchParams params;

    private final IVecInt __dimacs_out = new VecInt();

    private SearchListener slistener = new NullSearchListener();

    protected IVecInt dimacs2internal(IVecInt in) {
        if (voc.nVars() == 0) {
            throw new RuntimeException(
                    "Please set the number of variables (solver.newVar() or solver.newVar(maxvar)) before adding constraints!");
        }
        __dimacs_out.clear();
        __dimacs_out.ensure(in.size());
        for (int i = 0; i < in.size(); i++) {
            assert (in.get(i) != 0) && (Math.abs(in.get(i)) <= voc.nVars());
            __dimacs_out.unsafePush(voc.getFromPool(in.get(i)));
        }
        return __dimacs_out;
    }

    /**
     * creates a Solver without LearningListener. A learningListener must be
     * added to the solver, else it won't backtrack!!! A data structure factory
     * must be provided, else it won't work either.
     * 
     * @param acg
     *            an asserting clause generator
     */

    public Solver(AssertingClauseGenerator acg, LearningStrategy learner,
            DataStructureFactory dsf, IOrder order) {
        this(acg, learner, dsf, new SearchParams(), order);
    }

    public Solver(AssertingClauseGenerator acg, LearningStrategy learner,
            DataStructureFactory dsf, SearchParams params, IOrder order) {
        analyzer = acg;
        this.learner = learner;
        this.order = order;
        this.params = params;
        setDataStructureFactory(dsf);
    }

    /**
     * Change the internatal representation of the contraints. Note that the
     * heuristics must be changed prior to calling that method.
     * 
     * @param dsf
     *            the internal factory
     */
    public final void setDataStructureFactory(DataStructureFactory dsf) {
        dsfactory = dsf;
        dsfactory.setUnitPropagationListener(this);
        dsfactory.setLearner(this);
        voc = dsf.getVocabulary();
        order.setLits(voc);
    }

    public void setSearchListener(SearchListener sl) {
        slistener = sl;
    }

    public void setTimeout(int t) {
        timeout = t*1000L;
    }

    public void setTimeoutMs(long t) {
        timeout = t;
    }
    
    public void setSearchParams(SearchParams sp) {
        params = sp;
    }

    protected int nAssigns() {
        return trail.size();
    }

    public int nConstraints() {
        return constrs.size();
    }

    public void learn(Constr c) {
        learnts.push(c);
        c.setLearnt();
        c.register();
        stats.learnedclauses++;
        switch (c.size()) {
        case 2:
            stats.learnedbinaryclauses++;
            break;
        case 3:
            stats.learnedternaryclauses++;
            break;
        default:
            // do nothing
        }
    }

    public int decisionLevel() {
        return trailLim.size();
    }

    public int newVar() {
        int index = voc.nVars() + 1;
        voc.ensurePool(index);
        mseen = new boolean[index + 1];
        trail.ensure(index);
        trailLim.ensure(index);
        order.newVar();
        return index;
    }

    public int newVar(int howmany) {
        voc.ensurePool(howmany);
        order.newVar(howmany);
        mseen = new boolean[howmany + 1];
        trail.ensure(howmany);
        trailLim.ensure(howmany);
        return voc.nVars();
    }

    public IConstr addClause(IVecInt literals) throws ContradictionException {
        IVecInt vlits = dimacs2internal(literals);
        return addConstr(dsfactory.createClause(vlits));
    }

    public boolean removeConstr(IConstr co) {
        if (co == null) {
            throw new UnsupportedOperationException(
                    "Reference to the constraint to remove needed!"); //$NON-NLS-1$
        }
        Constr c = (Constr) co;
        c.remove();
        constrs.remove(c);
        clearLearntClauses();
        cancelLearntLiterals();
        return true;
    }

    public IConstr addPseudoBoolean(IVecInt literals, IVec<BigInteger> coeffs,
            boolean moreThan, BigInteger degree) throws ContradictionException {
        IVecInt vlits = dimacs2internal(literals);
        assert vlits.size() == literals.size();
        assert literals.size() == coeffs.size();
        return addConstr(dsfactory.createPseudoBooleanConstraint(vlits, coeffs,
                moreThan, degree));
    }

    public void addAllClauses(IVec<IVecInt> clauses)
            throws ContradictionException {
        for (IVecInt clause : clauses) {
            addClause(clause);
        }
    }

    public IConstr addAtMost(IVecInt literals, int degree)
            throws ContradictionException {
        int n = literals.size();
        IVecInt opliterals = new VecInt(n);
        for (int p : literals) {
            opliterals.push(-p);
        }
        return addAtLeast(opliterals, n - degree);
    }

    public IConstr addAtLeast(IVecInt literals, int degree)
            throws ContradictionException {
        IVecInt vlits = dimacs2internal(literals);
        return addConstr(dsfactory.createCardinalityConstraint(vlits, degree));
    }

    @SuppressWarnings("unchecked")
    public boolean simplifyDB() {
        // aucune raison de recommencer un propagate?
        // if (propagate() != null) {
        // // Un conflit est d?couvert, la base est inconsistante
        // return false;
        // }

        // Simplifie la base de clauses apres la premiere propagation des
        // clauses unitaires
        IVec<Constr>[] cs = new IVec[] { constrs, learnts };
        for (int type = 0; type < 2; type++) {
            int j = 0;
            for (int i = 0; i < cs[type].size(); i++) {
                if (cs[type].get(i).simplify()) {
                    // enleve les contraintes satisfaites de la base
                    cs[type].get(i).remove();
                } else {
                    cs[type].moveTo(j++, i);
                }
            }
            cs[type].shrinkTo(j);
        }
        return true;
    }

    /**
     * Si un mod?le est trouv?, ce vecteur contient le mod?le.
     * 
     * @return un mod?le de la formule.
     */
    public int[] model() {
        if (model == null) {
            throw new UnsupportedOperationException(
                    "Call the solve method first!!!"); //$NON-NLS-1$
        }
        int[] nmodel = new int[model.length];
        System.arraycopy(model, 0, nmodel, 0, model.length);
        return nmodel;
    }

    /**
     * Satisfait un litt?ral
     * 
     * @param p
     *            le litt?ral
     * @return true si tout se passe bien, false si un conflit appara?t.
     */
    public boolean enqueue(int p) {
        return enqueue(p, null);
    }

    /**
     * Put the literal on the queue of assignments to be done.
     * 
     * @param p
     *            the literal.
     * @param from
     *            the reason to propagate that literal, else null
     * @return true if the asignment can be made, false if a conflict is
     *         detected.
     */
    public boolean enqueue(int p, Constr from) {
        assert p > 1;
        if (voc.isSatisfied(p)) {
            // literal is already satisfied. Skipping.
            return true;
        }
        if (voc.isFalsified(p)) {
            // conflicting enqueued assignment
            return false;
        }
        // new fact, store it
        voc.satisfies(p);
        voc.setLevel(p, decisionLevel());
        voc.setReason(p, from);
        trail.push(p);
        return true;
    }

    private boolean[] mseen = new boolean[0];

    private final IVecInt preason = new VecInt();

    private final IVecInt outLearnt = new VecInt();

    public int analyze(Constr confl, Handle<Constr> outLearntRef) {
        assert confl != null;
        outLearnt.clear();

        final boolean[] seen = mseen;

        assert outLearnt.size() == 0;
        for (int i = 0; i < seen.length; i++) {
            seen[i] = false;
        }

        analyzer.initAnalyze();
        int p = ILits.UNDEFINED;

        outLearnt.push(ILits.UNDEFINED);
        // reserve de la place pour le litteral falsifie
        int outBtlevel = 0;

	// SATIN BEGIN: update conflict statistics:
	// System.out.println(name + " analyze: ");
	// printTrail();
	if (confl.learnt()) {
	    if (confl.learntGlobal()) {
		stats.learntGlobalFired++;
	    } else {
		stats.learntLocalFired++;
	    }
	} else {
	    stats.conflFired++;
	}
	// SATIN END

        do {
            preason.clear();
            assert confl != null;
            confl.calcReason(p, preason);
            if (confl.learnt())
                claBumpActivity(confl);
            // Trace reason for p
            for (int j = 0; j < preason.size(); j++) {
                int q = preason.get(j);
                order.updateVar(q);
                if (!seen[q >> 1]) {
                    // order.updateVar(q); // MINISAT
                    seen[q >> 1] = true;
                    if (voc.getLevel(q) == decisionLevel()) {
                        analyzer.onCurrentDecisionLevelLiteral(q);
			// SATIN
			if (decisionLevel() == splitLevel) {
			    Constr reason = voc.getReason(q);
			    if (reason != null && reason.size() == 1) {
				// split decision literal
				outLearnt.push(q ^ 1);
			    }
			}
		    // } else if (voc.getLevel(q) > 0) {
		    } else if (voc.getLevel(q) >= 0) { // SATIN needed?!
                        // ajoute les variables depuis le niveau de d?cision 0
                        outLearnt.push(q ^ 1);
                        outBtlevel = Math.max(outBtlevel, voc.getLevel(q));
                    }
                }
            }

            // select next reason to look at
            do {
                p = trail.last();
                // System.err.print((Clause.lastid()+1)+"
                // "+((Clause)confl).getId()+" ");
                confl = voc.getReason(p);
                // System.err.println(((Clause)confl).getId());
                // assert(confl != null) || counter == 1;
                undoOne();
            } while (!seen[p >> 1]);
            // seen[p.var] indique que p se trouve dans outLearnt ou dans
            // le dernier niveau de d?cision
        } while (analyzer.clauseNonAssertive(confl));

        outLearnt.set(0, p ^ 1);
	if (simplifier != null) { // SATIN
	    simplifier.simplify(outLearnt);
	}

        Constr c = dsfactory.createUnregisteredClause(outLearnt);
        slistener.learn(c);

	// SATIN DEBUG:
	if (false && c.size() <= satinMaxGlobalLearnSize) {
	    System.out.print(name + ": learnt small ");
	    for (int j = 0; j < c.size(); j++) {
		System.out.print(" " + c.get(j));
	    }
	    System.out.println();
	}

        outLearntRef.obj = c;

        assert outBtlevel > -1;
        return outBtlevel;
    }

    // SATIN TODO: simplification using local interface causes Solver
    // garbage collection problems?
    interface ISimplifier extends Serializable {
        void simplify(IVecInt outLearnt);
    }

    public static final ISimplifier NO_SIMPLIFICATION = new ISimplifier() {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public void simplify(IVecInt outLearnt) {
        }

        @Override
        public String toString() {
            return "No reason simplification"; //$NON-NLS-1$
        }
    };

    public final ISimplifier SIMPLE_SIMPLIFICATION = new ISimplifier() {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public void simplify(IVecInt outLearnt) {
            // simpleSimplification(outLearnt); // SATIN problem
        }

        @Override
        public String toString() {
            return "Simple reason simplification"; //$NON-NLS-1$
        }
    };

    public final ISimplifier EXPENSIVE_SIMPLIFICATION = new ISimplifier() {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public void simplify(IVecInt outLearnt) {
            // expensiveSimplification(outLearnt); // SATIN problem
        }

        @Override
        public String toString() {
            return "Expensive reason simplification"; //$NON-NLS-1$
        }
    };

    private ISimplifier simplifier = NO_SIMPLIFICATION;

    /**
     * Setup the reason simplification strategy.
     * By default, there is no reason simplification.
     * NOTE THAT REASON SIMPLIFICATION IS ONLY ALLOWED FOR WL 
     * CLAUSAL data structures. USING REASON SIMPLIFICATION
     * ON CB CLAUSES, CARDINALITY CONSTRAINTS OR PB CONSTRAINTS
     * MIGHT RESULT IN INCORRECT RESULTS. 
     * 
     * @param simp the name of the simplifier (one of NO_SIMPLIFICATION, SIMPLE_SIMPLIFICATION, EXPENSIVE_SIMPLIFICATION).
     */
    public void setSimplifier(String simp) {
        Field f;
        try {
            f = Solver.class.getDeclaredField(simp);
            simplifier = (ISimplifier) f.get(this);
        } catch (Exception e) {
            e.printStackTrace();
            simplifier = NO_SIMPLIFICATION;
        }
    }

    /**
     * Setup the reason simplification strategy.
     * By default, there is no reason simplification.
     * NOTE THAT REASON SIMPLIFICATION IS ONLY ALLOWED FOR WL 
     * CLAUSAL data structures. USING REASON SIMPLIFICATION
     * ON CB CLAUSES, CARDINALITY CONSTRAINTS OR PB CONSTRAINTS
     * MIGHT RESULT IN INCORRECT RESULTS. 
     * 
     * @param simp
     */
    public void setSimplifier(ISimplifier simp) {
        simplifier = simp;
    }

    // Taken from MiniSAT 1.14: Simplify conflict clause (a little):
    private void simpleSimplification(IVecInt outLearnt) {
        int i, j;
        final boolean[] seen = mseen;
        for (i = j = 1; i < outLearnt.size(); i++) {
            IConstr r = voc.getReason(outLearnt.get(i));
            assert r instanceof WLClause;
            if (r == null) {
                outLearnt.moveTo(j++, i);
            } else {
                assert r.get(0) == neg(outLearnt.get(i));
                for (int k = 1; k < r.size(); k++)
                    if (!seen[r.get(k) >> 1] && (voc.getLevel(r.get(k)) != 0)) {
                        outLearnt.moveTo(j++, i);
                        break;
                    }
            }
        }
        outLearnt.shrink(i - j);
        stats.reducedliterals += (i - j);
    }

    private final IVecInt analyzetoclear = new VecInt();

    private final IVecInt analyzestack = new VecInt();

    // Taken from MiniSAT 1.14
    private void expensiveSimplification(IVecInt outLearnt) {
        // Simplify conflict clause (a lot):
        //
        int i, j;
        // (maintain an abstraction of levels involved in conflict)
        analyzetoclear.clear();
        outLearnt.copyTo(analyzetoclear);
        for (i = 1, j = 1; i < outLearnt.size(); i++)
            if (voc.getReason(outLearnt.get(i)) == null
                    || !analyzeRemovable(outLearnt.get(i)))
                outLearnt.moveTo(j++, i);
        outLearnt.shrink(i - j);
        stats.reducedliterals += (i - j);
    }

    // Check if 'p' can be removed. 'min_level' is used to abort early if
    // visiting literals at a level that cannot be removed.
    //
    private boolean analyzeRemovable(int p) {
        assert voc.getReason(p) != null;
        analyzestack.clear();
        analyzestack.push(p);
        final boolean[] seen = mseen;
        int top = analyzetoclear.size();
        while (analyzestack.size() > 0) {
            int q = analyzestack.last();
            assert voc.getReason(q) != null;
            Constr c = voc.getReason(q);
            assert c instanceof WLClause;
            analyzestack.pop();
            assert c.get(0) == neg(q);
            for (int i = 1; i < c.size(); i++) {
                int l = c.get(i);
                // if (!seen[var(l)] && voc.getLevel(l) != 0) {
                if (!seen[var(l)] /* && voc.getLevel(l) != 0 */) { // SATIN?!
                    if (voc.getReason(l) == null) {
                        for (int j = top; j < analyzetoclear.size(); j++)
                            seen[analyzetoclear.get(j) >> 1] = false;
                        analyzetoclear.shrink(analyzetoclear.size() - top);
                        return false;
                    }
                    seen[l >> 1] = true;
                    analyzestack.push(l);
                    analyzetoclear.push(l);
                }
            }
        }

        return true;
    }

    /**
     * decode the internal representation of a literal into Dimacs format.
     * 
     * @param p
     *            the literal in internal representation
     * @return the literal in dimacs representation
     */
    public static int decode2dimacs(int p) {
        return ((p & 1) == 0 ? 1 : -1) * (p >> 1);
    }

    /**
     * 
     */
    protected void undoOne() {
        // recupere le dernier litteral affecte
        int p = trail.last();
        assert p > 1;
        assert voc.getLevel(p) >= 0;
        int x = p >> 1;
        // desaffecte la variable
        voc.unassign(p);
        voc.setReason(p, null);
        voc.setLevel(p, -1);
        // met a jour l'heuristique
        order.undo(x);
        // depile le litteral des affectations
        trail.pop();
        // met a jour les contraintes apres desaffectation du litteral :
        // normalement, il n'y a rien a faire ici pour les prouveurs de type
        // Chaff??
        IVec<Undoable> undos = voc.undos(p);
        assert undos != null;
        while (undos.size() > 0) {
            undos.last().undo(p);
            undos.pop();
        }
    }

    /**
     * Propagate activity to a constraint
     * 
     * @param confl
     *            a constraint
     */
    public void claBumpActivity(Constr confl) {
        confl.incActivity(claInc);
        if (confl.getActivity() > CLAUSE_RESCALE_BOUND)
            claRescalActivity();
        // for (int i = 0; i < confl.size(); i++) {
        // varBumpActivity(confl.get(i));
        // }
    }

    public void varBumpActivity(int p) {
        order.updateVar(p);
    }

    private void claRescalActivity() {
        for (int i = 0; i < learnts.size(); i++) {
            learnts.get(i).rescaleBy(CLAUSE_RESCALE_FACTOR);
        }
        claInc *= CLAUSE_RESCALE_FACTOR;
    }

    /**
     * @return null if not conflict is found, else a conflicting constraint.
     */
    public Constr propagate() {
        while (qhead < trail.size()) {
            stats.propagations++;
            int p = trail.get(qhead++);
            slistener.propagating(decode2dimacs(p));
            // p is the literal to propagate
            // Moved original MiniSAT code to dsfactory to avoid
            // watches manipulation in counter Based clauses for instance.
            assert p > 1;
            IVec<Propagatable> constrs = dsfactory.getWatchesFor(p);

            final int size = constrs.size();
            for (int i = 0; i < size; i++) {
                stats.inspects++;
                if (!constrs.get(i).propagate(this, p)) {
                    // Constraint is conflicting: copy remaining watches to
                    // watches[p]
                    // and return constraint
                    dsfactory.conflictDetectedInWatchesFor(p, i);
                    qhead = trail.size(); // propQ.clear();
                    // FIXME enlever le transtypage
                    return (Constr) constrs.get(i);
                }
            }
        }
        return null;
    }

    void record(Constr constr) {
        constr.assertConstraint(this);
        slistener.adding(decode2dimacs(constr.get(0)));
        if (constr.size() == 1) {
            stats.learnedliterals++;
        } else {
            learner.learns(constr);
        }
    }

    /**
     * @return false ssi conflit imm?diat.
     */
    public boolean assume(int p) {
        // Precondition: assume propagation queue is empty
        assert trail.size() == qhead;
        trailLim.push(trail.size());
        return enqueue(p);
    }

    /**
     * Revert to the state before the last push()
     */
    private void cancel() {
        // assert trail.size() == qhead || !undertimeout;
        int decisionvar = trail.unsafeGet(trailLim.last());
        slistener.backtracking(decode2dimacs(decisionvar));
        for (int c = trail.size() - trailLim.last(); c > 0; c--) {
            undoOne();
        }
        trailLim.pop();
    }

    /**
     * Restore literals
     */
    private void cancelLearntLiterals() {
        // assert trail.size() == qhead || !undertimeout;

        for (int c = trail.size() - rootLevel; c > 0; c--) {
            undoOne();
        }
        qhead = trail.size();
    }

    /**
     * Cancel several levels of assumptions
     * 
     * @param level
     */
    protected void cancelUntil(int level) {
        while (decisionLevel() > level) {
            cancel();
        }
        qhead = trail.size();
    }

    private final Handle<Constr> learntConstraint = new Handle<Constr>();

    private boolean[] fullmodel;

    // SATIN BEGIN
    final private static boolean satinGlobalDebug = false; // true;
    final private static boolean analyzeDebug = satinGlobalDebug;
    final private static boolean splitDebug = satinGlobalDebug;

    private int splitLevel = -1;

    // list of all initial constraints:
    protected Vec<VecInt> allConstrs = new Vec<VecInt>();

    // Temporary workarounds/experiments/..
    private static final int DEFAULT_SATIN_FIXES = 0x38; // = 56
    public int satinFixes = DEFAULT_SATIN_FIXES;
    /* Current semantics of satinFixes:
     * 0x01: <not used currently>
     * 0x02: <not used currently>
     * 0x04: <not used currently>
     * 0x08: in external spawner: only clone solver state for one of the
     *       children, and pass on the current one to the other child
     * 0x10: incorporate variable selection statistics from children
     *       (potentially useful during iterative restarts)
     * 0x20: avoid a problem with split vars in analyze()  (default now)
     * 0x40: pass reason vectors separately (and in fact additionally);
     *       seems to trigger an assert in WLClause though, since var
     *       order in reason might be changed from regular one?
     */

    protected String name = "/solver/";

    private int serializedSize(Object obj)
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream ();
            ObjectOutputStream oout = new ObjectOutputStream (out);
            oout.writeObject (obj);
            
	    return out.toByteArray().length;
        }
        catch (Exception e)
        {
            throw new RuntimeException ("cannot clone class [" +
                obj.getClass ().getName () + "] via serialization: " +
                e.toString ());
        }
    }

    private Solver serialClone(Solver obj)
    {
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream ();
            ObjectOutputStream oout = new ObjectOutputStream (out);
            oout.writeObject (obj);
            
            ObjectInputStream in = new ObjectInputStream (
                new ByteArrayInputStream (out.toByteArray ()));
            return (Solver) in.readObject ();
        }
        catch (Exception e)
        {
            throw new RuntimeException ("cannot clone class [" +
                obj.getClass ().getName () + "] via serialization: " +
                e.toString ());
        }
    }

    private Solver serialIbisClone (Solver solver)
    {
	return (Solver) ibis.util.DeepCopy.deepCopy(solver);
    }

    static final boolean reasonDebug = false;

    private VecInt constraintToVecInt(Constr c)
    {
	VecInt cVec;

	cVec = new VecInt();
	for (int i = 0; i < c.size(); i++) {
	    cVec.push(c.get(i));
	}
	return cVec;
    }

    public Vec<VecInt> reasonVec()
    {
	Vec<VecInt> reasons = new Vec<VecInt>();

	if ((satinFixes & 0x40) == 0) {
	    // still experimental stuff, don't do by default
	    return reasons;
	}

	reasons.push(null);
	for (int i = 1; i <= voc.nVars(); i++) {
	    int lit = i << 1;
	    VecInt rVec;

	    Constr reason = voc.getReason(lit);
	    if (reason != null) {
		// Include full reasons, since constraint IDs are only
		// implemented for WLClause, and IDs cannot be trusted
		// for the shared clauses version.  Sigh..
		if (reason instanceof WLClause) {
		    rVec = constraintToVecInt(reason);
		    if (reasonDebug) {
			System.out.println(name + ": " + lit +
					   " has reason " + reason +
					   ": " + rVec);
		    }
		} else {
		    if (reasonDebug) {
			System.out.println(name + ": " + lit +
					   " has non-WL reason " + reason);
		    }
		    rVec = null;
		}
	    } else {
		rVec = null;
	    }
	    reasons.push(rVec);
	}

	return reasons;
    }

    private void setReasons(Vec<VecInt> reasons)
    {
	if ((satinFixes & 0x40) == 0) {
	    // still experimental stuff, don't do by default
	    return;
	}

	// Put the reasons, indexed by variable number, in a hashtable
	// ignoring the order of the literals (UnorderedVec).
	Hashtable reasonHash = new Hashtable();

	for (int i = 0; i < reasons.size(); i++) {
	    VecInt rVec = reasons.get(i);

	    if (rVec != null) {
		UnorderedVec uv = new UnorderedVec(rVec);
		reasonHash.put(uv, (Integer) i);
	    }
	}

	// Scan over the constraints to find matches with the reasons
	// and reconstruct
	for (int i = 0; i < constrs.size(); i++) {
	    Constr c = constrs.get(i);
	    UnorderedVec uv = new UnorderedVec(constraintToVecInt(c));
	    Integer var;

	    if ((var = (Integer) reasonHash.get(uv)) != null) {
		int lit = ((int) var) << 1;

		voc.setReason(lit, c);
		if (reasonDebug) {
		    System.out.println(name + ": lit " + lit +
				       " had reason " + c);
		}
	    }
	}

	// Same for the learnts
	for (int i = 0; i < learnts.size(); i++) {
	    Constr c = learnts.get(i);
	    UnorderedVec uv = new UnorderedVec(constraintToVecInt(c));
	    Integer var;

	    if ((var = (Integer) reasonHash.get(uv)) != null) {
		int lit = ((int) var) << 1;

		voc.setReason(lit, c);
		if (reasonDebug) {
		    System.out.println(name + ": lit " + lit +
				       " had learnt reason " + c);
		}
	    }
	}
    }

    private static final String DEFAULT_CLONE_TYPE = "ibis";
    private String satinCloneType = DEFAULT_CLONE_TYPE;

    public void fixupAfterClone()
    {
	// In childs, learnts are fresh:
	startNewLearnts = 0;

	// Reset statistics; the new stats are added back at the end of
	// the job by the parent.
	// stats.reset();
	stats = new SolverStats();
    }

    public Solver clone_for_satin()
    {
	Solver clone;

	long begintime = System.currentTimeMillis();

	if (satinUseSharedClauses) {
	    // Clone all except the constraints
	    // They will be rebuilt based on the shared clauses
	    // For the moment it is done by first including it in
	    // the clone and then deleting it manually, since the
	    // hooks to do this in one step currently don't exist,
	    // and could lead to old cruft still hanging around
	    // in the cloned Solver object; see below.
	}

	
	if (satinCloneType.equals("sun")) {
	    clone = serialClone(this);
	    // System.out.println("Cloned " + clone.name +
	    //		       " by Sun serialization");
	} else {
	    clone = serialIbisClone(this);
	    // System.out.println("Cloned " + clone.name +
	    //		       " by Ibis serialization");
	}

	if (satinUseSharedClauses) {
	    if (satinCloneType.equals("sun") ||
		satinCloneType.equals("ibis"))
	    {
		if (false) {
		    System.out.println(name + ": before clause removal: " +
				       serializedSize(clone));
		}

		// Now delete the constraints and data structures pointing
		// to them: the watched literal implementation:
		for (int i = 0; i < clone.constrs.size(); i++) {
		    Constr c = clone.constrs.get(i);
		    c.remove();
		}
		clone.constrs = null;
	    }
	    // In the clone() the watched literals are only pointing
	    // to the cloned learned clauses, which is fine.
	}

	// Avoid backtraking beyond current level:
	// clone.rootLevel = decisionLevel();
	// Now done in the children themselves, AFTER the assumption!

	clone.fixupAfterClone();

	if (false) {
	    System.out.println(name + ": size " + serializedSize(this) +
			       " clone size " + serializedSize(clone));
	}

	stats.cloneOverhead +=
	    (System.currentTimeMillis() - begintime) / 1000.0;

	return clone;
    }

    private static int DEFAULT_MAX_GLOBAL_LEARN_SIZE = 0;
    private int satinMaxGlobalLearnSize = DEFAULT_MAX_GLOBAL_LEARN_SIZE;

    // To limit spawning big problems deeper and deeper until memory runs out:
    private static final int DEFAULT_MAX_SPAWN_DEPTH = 100;
    private int satinMaxSpawnDepth = DEFAULT_MAX_SPAWN_DEPTH;

    private static final int DEFAULT_MAX_ITER = 25000;
    private int satinMaxLocalIter = DEFAULT_MAX_ITER; // TODO: need heuristics

    class UnorderedVec
    {
	VecInt theVec;

	UnorderedVec(VecInt vec)
	{
	    theVec = vec;
	}

	
	@Override
	public boolean equals(Object other)
	{
	    VecInt otherVec = ((UnorderedVec) other).theVec;

	    if (false) {
		System.out.print("compare: ");
		for (int i = 0; i < theVec.size(); i++) {
		    System.out.print(" " + theVec.get(i));
		}
		System.out.print("and: ");
		for (int i = 0; i < otherVec.size(); i++) {
		    System.out.print(" " + otherVec.get(i));
		}
		System.out.println();
	    }

	    for (int i = 0; i < theVec.size(); i++) {
		int find = theVec.get(i);
		boolean found = false;
		for (int j = 0; j < otherVec.size(); j++) {
		    if (otherVec.get(j) == find) {
			found = true;
			break;
		    }
		}
		if (! found) {
		    // System.out.println(" other missing " + find);
		    return false;
		}
	    }

	    for (int i = 0; i < otherVec.size(); i++) {
		int find = otherVec.get(i);
		boolean found = false;
		for (int j = 0; j < theVec.size(); j++) {
		    if (theVec.get(j) == find) {
			found = true;
			break;
		    }
		}
		if (! found) {
		    // System.out.println(" this missing " + find);
		    return false;
		}
	    }

	    // System.out.println(" equal! size " + theVec.size());
	    return true;
	}

	@Override
	public int hashCode()
	{
	    int sum = 0x0;

	    for (int i = 0; i < theVec.size(); i++) {
		sum ^= theVec.get(i);
	    }

	    // System.out.print(" hash " + sum + " ");
	    return sum;
	}
    }

    private long nofLearnts;

    private int startNewLearnts = 0;
    private Vec<String> learntFrom;

    private static final boolean learntDebug = false;
    private static final boolean satinDebug = satinGlobalDebug; // false;

    public boolean satinUseSharedObjects = true;
    // SATIN TODO:
    private boolean satinUseSharedClauses = false; // for now
    public int satinSearchIter = 0;

    private void
    processGlobalLearnts(SolverState globalState, int iter)
    {
	Hashtable myLearnedHash = new Hashtable();

	// Too much overhead:
	if (false) {
	    // Initialize the hash with the ones we already know:
	    for (int i = 0; i < learnts.size(); i++) {
		Constr l;

		l = learnts.get(i);
		VecInt cl = new VecInt();
		for (int j = 0; j < l.size(); j++) {
		    cl.push(l.get(j));
		}

		if (learntDebug) {
		    System.out.print(name + ": already learnt " + i + ":");
		    for (int j = 0; j < l.size(); j++) {
			System.out.print(" " + cl.get(j));
		    }
		    System.out.println();
		}

		UnorderedVec uv = new UnorderedVec(cl);
		if (myLearnedHash.get(uv) == null) {
		    myLearnedHash.put(uv, name);
		} else {
		    if (learntDebug) {
			System.out.println("duplicate?!");
		    }
		}
	    }
	}

	int took = 0;
	int avail = 0;
	int jobs = 0;
	int numKeys = 0;

	if (startNewLearnts == 0) {
	    startNewLearnts = learnts.size();
	}
	learntFrom = new Vec<String>();

	Enumeration keys = globalState.learnedHash.keys();
	while (keys.hasMoreElements()) {
	    numKeys = 0;
	    String key = (String) keys.nextElement();
	    // Vec<Constr> learn = null;
	    Vec<VecInt> newLearnt = null;
	    int [][] newLearntarr = null;
	    int nlearnt;

	    learntFrom.push(key);
	    newLearnt = (Vec<VecInt>) globalState.learnedHash.get(key);
	    if (newLearnt == null) {
		continue;
	    }
	    nlearnt = newLearnt.size();
	    if (learntDebug) {
		System.out.println("solver " + name +
				   " learnt " + newLearnt.size() +
				   " from " + key);
	    }

	    if (name.equals(key)) {
		// this is true for the reiterating rootSolver
		if (learntDebug) {
		    System.out.println("solver " + name +
				       " cannot learn from itself");
		}
		continue;
	    }

	    jobs++;
	    avail += nlearnt;

	    for (int i = 0; i < nlearnt; i++) {
		VecInt cl;

		cl = newLearnt.get(i);
		if (cl == null || cl.size() == 0) {
		    // Hack: this can happen when global learnts are
		    // removed by one of the other threads..
		    if (learntDebug) {
			System.out.println(name + ": no learnt from "
					   + key + " at " +  i + "!");
		    }
		    break;
		}

		if (learntDebug) {
		    System.out.print(name + ": learnt " + i +
				     " from " + key + ":");
		    for (int j = 0; j < cl.size(); j++) {
			System.out.print(" " + cl.get(j));
		    }
		    System.out.println();
		}

		UnorderedVec uv = new UnorderedVec(cl);
		if (myLearnedHash.get(uv) == null) {
		    // System.out.println(": new!");
		    myLearnedHash.put(uv, key);
		    took++;
		    
		    // Note: use newConstr instead of createUnregisteredClause
		    // directly, since we need to watch two UNassigned
		    // literals in the clause if possible, and the other
		    // primitive doesn't take care of that.
		    Constr c = newConstr(cl);
		    if (c != null) { // SATIN TODO
			// Subset of functionality of record(c):
			c.setLearnt();
			// Also set a flag to note it was globally learned
			c.setLearntGlobal();
			// NOT: constr.assertConstraint(this);
			slistener.adding(decode2dimacs(c.get(0)));
			if (c.size() == 1) {
			    // NOT: stats.learnedliterals++;
			} else {
			    // Note: next call will also increment locally
			    // learned variable statistics; so overall they
			    // will be added multiple times. Other than that,
			    // no problem.
			    learner.learns(c);
			}
		    }
		}
	    }

	    if (learnts.size() > nofLearnts) {
		// Reduce the set of learnt clauses
		reduceDB();
	    }
	}

	if (took > 0) {
	    stats.learnedglobaljobs++;
	    stats.learnedglobalclauses += took;

	    if (verbose) {
		System.out.println(name + " iter " + iter +
				   " took " + took + " of " +  avail +
				   " from " + jobs + " jobs;" +
				   " now " + learnts.size());
	    }
	}
    }

    private void
    publishLearnts(SolverState globalState, long conflicts)
    {
	Vec<VecInt> sendLearnts = null;
	Vec<int[]> sendLearntsVec = null;

	sendLearnts = new Vec<VecInt>(learnts.size());

	// Only send newly learnt constraints starting at startNewLearnts
	for (int i = startNewLearnts; i < learnts.size(); i++) {
	    Constr l;

	    l = learnts.get(i);
	    if (l.size() <= satinMaxGlobalLearnSize) {
		if (learntDebug) {
		    System.out.print(name + ": publish ");
		    for (int j = 0; j < l.size(); j++) {
			System.out.print(" " + l.get(j));
		    }
		    System.out.println();
		}

		VecInt lVec = new VecInt(l.size());
		for (int j = 0; j < l.size(); j++) {
		    lVec.push(l.get(j));
		}
		sendLearnts.push(lVec);
	    }
	}

	if (sendLearnts.size() > 0) {
	    globalState.addLearnts(name, sendLearnts, conflicts);
	    stats.publishglobalclauses += sendLearnts.size();
	    stats.publishglobaljobs++;
	} else {
	    globalState.addConflicts(conflicts);
	}

	// Avoid processing duplicate info in subsequent jobs:
	if (learntFrom != null) {
	    for (int i = 0; i < learntFrom.size(); i++) {
		if (verbose) {
		    System.out.println(name + ": remove learnt from " +
				       learntFrom.get(i));
		}
		globalState.removeLearnts(learntFrom.get(i));
		stats.removedglobaljobs++;
	    }
	}
    }

    public SolverResult
    searchResult(Lbool result)
    {
	return new SolverResult(result, model, stats, voc, order);
    }

    private Constr newConstr(VecInt theLits)
    {
	// Note: need the clone() since createClause
	// really grabs (and modifies) the lits array.
	// ORIG: VecInt vlits = (VecInt) theLits.clone();
	// NEW: copy instead of clone:
	VecInt vlits = new VecInt();
	for (int i = 0; i < theLits.size(); i++) {
	    vlits.push(theLits.get(i));
	}

	// NOTE: Put the variable with the highest level on place 1
	// (see WLClause), so that after UNassignments, which happen
	// in reverse, we keep watching the right literals.
	int maxi = 1;
	int maxlevel = voc.getLevel(vlits.get(1));
	for (int i = 2; i < vlits.size(); i++) {
	    int level = voc.getLevel(vlits.get(i));
	    if (level > maxlevel) {
		maxi = i;
		maxlevel = level;
	    }
	}
	int l = vlits.get(1);
	vlits.set(1, vlits.get(maxi));
	vlits.set(maxi, l);

	// Also make sure the first (watched) literals can still be made true,
	// else swap them with other unassigned ones.
	// TODO: is this really still necessary, given the precautions above?!
	for (int k = 1; k >= 0; k--) {
	    if (voc.isFalsified(vlits.get(k))) {
		if (satinGlobalDebug) {
		    System.out.println(name + " constr " +
				       vlits + " falsified " +  k + ": " +
				       vlits.get(k));
		}
		// search a free var to be used instead:
		int j;
		for (j = 2; j < vlits.size(); j++) {
		    if (voc.isUnassigned(vlits.get(j))) {
			int lit = vlits.get(j);
			vlits.set(j, vlits.get(k));
			vlits.set(k, lit);
			if (satinGlobalDebug) {
			    System.out.println("swap " + vlits.get(j) +
					       ", " + lit);
			}
			break;
		    }
		}
		if (satinGlobalDebug) {
		    if (j >= vlits.size()) {
			System.out.println("*** no free var for " + k + "!");
		    }
		}
	    }
	}

	// Note: we cannot create a registered clause, since
	// the primitive for that *also* throws out literals
	// based on the current (possibly tentative)
	// variable assignments.
	return dsfactory.createUnregisteredClause(vlits);
    }

    public SolverResult
    satinDoRecSearch(long nofConflicts, int satinDepth,
		     SolverState globalState, Vec<VecInt> reasons)
    {
	long begintime = System.currentTimeMillis();

	try
        {
	    // If solution found elsewhere, stop as quickly as possible
	    if (satinUseSharedObjects && globalState.updated(satinSearchIter)){
		if (verbose) {
		    System.out.println(name + ": pruned");
		}
		return searchResult(Lbool.UNDEFINED); // other branch knows
	    }

	    if (satinUseSharedClauses) {
		// Reconstruct current constraints from initial
		// global constraints
		constrs  = new Vec<Constr>();
		// System.out.println(name + ": Read all constraints: " +
		//		      globalState.allConstrs + "; number " +
		//		      globalState.allConstrs.size());
		int nconstr = 0;
		for (int i = 0; i < globalState.allConstrs.size(); i++) {
		    // System.out.println("creating constraint for " +
		    //		       globalState.allConstrs.get(i));
		    Constr c = newConstr(globalState.allConstrs.get(i));
		    if (c != null) {
			// Previously we did a c.setVoc() but that was only
			// for the native cloning support that is dropped
			c.register();
			if (satinGlobalDebug) {
			    System.out.println("New constr " + i + ": " + c);
			}
			nconstr++;
			addConstr(c);
		    }
		}
		if (satinGlobalDebug) {
		    System.out.println(name +
				       ": Number of live constraints: " +
				       nconstr);
		}
	    }

	    // Apply reasons for assigned variables
	    if (reasons != null) {
		setReasons(reasons);
	    }

	    if (false && splitDebug) {
	        System.out.println(name + ": satinDoRecSearch:");
		printTrail();
	    }

	    if (satinDebug) {
		System.out.println(name + ": spawn level " + satinDepth +
		    " root level " + rootLevel +
		    " decision level " + decisionLevel());
	    }

	    SolverResult res =
		satinRecSearch(nofConflicts, satinDepth, globalState);
	    double secs = (System.currentTimeMillis() - begintime) / 1000.0;
	    if (satinDepth < 6 || secs > 10.0) {
		// System.out.println(name + ": " + res.satin_res);
		System.out.println("c " + name + 
				   (res.seqTimeOut ? " time " : " took ") +
				   secs + " sec");
	    }
	    return res;
	}
	catch(Throwable e) {
	    System.err.println("Got exception: " + e);
	    e.printStackTrace();
	    return new SolverResult(Lbool.FALSE, null, stats, voc, order);
	} 
    }

    private void printTrail()
    {
	int prevLevel = -1;

	System.out.print("trail " +
			 " root level " + rootLevel +
			 " decision level " + decisionLevel() +
			 " size " + trail.size() +
			 // " lim last " + ((trailLim.size() > 0) ?
			 //		 trailLim.last() : -1) +
			 " lim size " + trailLim.size() +
			 ": ");
	for (int i = 0; i < trailLim.size(); i++) {
	    System.out.print(" " + i + "=" + trailLim.get(i));
	}

	for (int i = 0; i < trail.size(); i++) {
	    int p = trail.get(i);
	    int level = voc.getLevel(p);
	    if (level != prevLevel) {
		System.out.println();
		System.out.print("" + level + ": ");
	    }
	    System.out.print(" " + p);
	    Constr reason = voc.getReason(p);
	    if (reason == null) {
		if (level == prevLevel) {
	            System.out.print("@");
		} else {
	            System.out.print("#");
		}
	    }
	    prevLevel = level;
	}
	System.out.println();
    }

    public int splitParent()
    {
	int ret = -1;
	int thisRootLevel = (rootLevel == 0) ? 1 : rootLevel;

	if (false && splitDebug) {
	    System.out.println(name + ": parent trail before split:");
	    printTrail();
	}

 	// GridSAT
	int rootVarIndex = trailLim.get(thisRootLevel);

	// Modify decision levels of variables above root level
	for (int i = rootVarIndex; i < trail.size(); i++) {
	    int p = trail.get(i);
	    if (i == rootVarIndex) {
		ret = p;
		if (voc.getReason(p) == null) {
		    // analyze() by default assumes every assignment at the
		    // same decision level has a proper reason clause that
		    // led to its conclusion.
		    // To honor this, we create a fake reason for our chosen
		    // "split" variable (which is done without a reason in
		    // the formal sense).
		    // The alternative is to recognize this particular case
		    // in analyze() itself, but it seems more messy.
		    VecInt vlits = new VecInt();
		    vlits.push(p);
		    Constr cl = dsfactory.createUnregisteredClause(vlits);
		    voc.setReason(p, cl);
		} else {
		    System.out.println(name + ": *** split lit " + p +
				       " already has a reason?!");
		}
	    }
	    // Don't move level 1 vars to level 0: that has the special
	    // meaning of assumptions made before search was started
	    // (e.g., analyze() does not include them when constructing
	    // conflict clauses)
	    if (voc.getLevel(p) > 1) {
		voc.setLevel(p, voc.getLevel(p) - 1);
	    }
	}
	for (int i = thisRootLevel; i < trailLim.size() - 1; i++) {
	    trailLim.set(i, trailLim.get(i + 1));
	}
	trailLim.pop();

	if (splitDebug) {
	    System.out.println("parent trail: split var " + ret);
	    System.out.print("new trailLim: ");
	    for (int i = 0; i < trailLim.size(); i++) {
		System.out.print(" " + i + "=" + trailLim.get(i));
	    }
	    System.out.println();
	    printTrail();
	}

	splitLevel = 1;

	return ret;
    }

    private boolean inconsistentPropagation = false;

    public void splitChild()
    {
	int thisRootLevel = (rootLevel == 0) ? 1 : rootLevel;
	int rootVarIndex = trailLim.get(thisRootLevel);
	int lit = trail.get(rootVarIndex);

	if (splitDebug) {
	    System.out.println(name + ": child trail before split:");
	    printTrail();
	}

	// cancel back to rootLevel; the split child then negates the
	// assumption of the chosen literal after the previous root
	// For details, see the GridSAT paper.
	cancelUntil(thisRootLevel);

	splitLevel = 1;

	VecInt vlits = new VecInt();
	int neglit = (lit ^ 1);
	vlits.push(neglit);
	Constr cl = dsfactory.createUnregisteredClause(vlits);
	enqueue(neglit, cl);
        Constr confl = propagate();
	if (confl != null) {
	    // System.out.println("*** splitChild conflict: " + neglit);
	    inconsistentPropagation = true;
	}

	if (trailLim.size() > thisRootLevel) {
            trailLim.set(thisRootLevel, trailLim.get(thisRootLevel) + 1);
	}

	if (splitDebug) {
	    System.out.println("child trail after split:");
	    printTrail();

	    if (false) {
	        if (voc.isFalsified(lit)) {
	  	    System.out.println("*** Firstvar falsified: " + lit);
	        }
	        if (voc.isFalsified(neglit)) {
		    System.out.println("*** -Firstvar falsified:  " + neglit);
	        }
	    }
	}
    }

    private static final boolean verbose = satinGlobalDebug; // false;
    private static final boolean jobStats = false;

    public void updateTimingStats(SolverState globalState,
				  long begintime, int iters)
    {
	if (jobStats) {
	    long curtime = System.currentTimeMillis();
	    TimeInfo timeinfo = new TimeInfo(name, curtime,
					     curtime - begintime, iters);
	    globalState.updateTime(timeinfo);
	}
    }

    private void setFinished(SolverState globalState, Lbool bool)
    {
	globalState.setFinished(bool);
    }

    private long initialFreeMem;

    // TODO: need auto-inferring of what constitutes a "big" problem
    // that could improve load balancing by forking off earlier
    // in the initial phase.
    private static final boolean bigProblem = false;

    // Added for spawner:
    public int lastConflicts;

    public SolverResult
    satinRecSearch(long nofConflicts, int satinDepth, SolverState globalState)
    {
	int conflictC = 0;
	int conflictCsav = 0;
	int loop = 0;
	int loop2 = 0;
	long gloConflicts;
 	// increase job length for deap searches with high nofConflicts
	long minLocalConflicts = (satinDepth > 3) ? (nofConflicts / 256) : 0;
	long begintime = System.currentTimeMillis();
	int maxIter;

        final long bound1 = (initialFreeMem / 3);
        final long bound2 = 32 * 1024 * 1024;
        final long memorybound = (bound1 < bound2) ? bound2 : bound1;

	if (inconsistentPropagation) {
	    // Just after the problem was split, the childs may find out
	    // that in fact assertion of the chosen split literal led
	    // to an inconsistency.  Situation is dealt with here.
	    // System.out.println(name + ": splitChild found conflict");
	    return searchResult(Lbool.FALSE);
	}

	if (satinUseSharedObjects) {
	    // Now done in satinDoRecSearch
	    if (false && globalState.updated(satinSearchIter)) {
	    	if (verbose) {
		    System.out.println("c " + name + ": pruned");
		}
		return searchResult(Lbool.FALSE);
	    }
	    gloConflicts = globalState.globalConflicts;
	    // TODO: only learn at slaves?
	    processGlobalLearnts(globalState, 0);
	} else {
	    gloConflicts = 0; // Don't know; got reduced nofConflicts instead
	}

	maxIter = satinMaxLocalIter;
	if (bigProblem) {
	    // Speed up spawns in the beginning to get all nodes busy:
	    if (satinDepth <= 6) {
		maxIter >>= 1;
		if (satinDepth <= 4) {
		    maxIter >>= 1;
		    if (satinDepth <= 2) {
			maxIter >>= 1;
		    }
		}
	    }
	}

	if (satinDepth > 0) {
            // Start with new literal decision
            stats.decisions++;
            int p = order.select();
            assert p > 1;

	    slistener.assuming(decode2dimacs(p));
	    boolean ret = assume(p);
	    assert ret;

	    if (analyzeDebug) {
	        System.out.println("c " + name + ": first assumption:");
	        printTrail();
	    }

	    if (satinDepth > stats.maxSpawnDepth) {
		stats.maxSpawnDepth = satinDepth;
	    }

	    System.out.println("c " + name + " "
			       + (int) (100.0 *
					Runtime.getRuntime().freeMemory() /
					(double) initialFreeMem)
			       + "% memory free");
	}

        do {
            slistener.beginLoop();
	    loop++;

	    if (verbose) {
		System.out.print("c " + name + ": trail size " +
				 trail.size() + ": ");
		for (int i = 0; i < trail.size(); i++) {
		    System.out.print(" " + trail.get(i));
		}
		System.out.println();
	    }

            // propage les clauses unitaires
            Constr confl = propagate();
            assert trail.size() == qhead;

	    if (analyzeDebug && loop < 2) {
		System.out.println("c " + name + ": loop " + loop);
		printTrail();
	    }

            if (confl != null) {
                // un conflit apparait
                stats.conflicts++;
                conflictC++;
                slistener.conflictFound();
                freeMem.newConflict();
                if (decisionLevel() == rootLevel) {
                    // on est a la racine, la formule est inconsistante
		    if (satinDebug) {
			System.out.println("c " + name + ": UNSAT after " +
					   loop + " iterations");
		    }
		    // System.out.println(name + ": " + confl.toString());
		    if (satinUseSharedObjects) {
			updateTimingStats(globalState, begintime, loop);
			publishLearnts(globalState, conflictC);
		    }
                    return searchResult(Lbool.FALSE);
                }

                // analyse la cause du conflit
                assert confl != null;
                int backtrackLevel = analyze(confl, learntConstraint);
                assert backtrackLevel < decisionLevel();
		// System.err.println(name +
		// 		   ": backtrack to " + backtrackLevel +
		//		   ", decisionLevel " + decisionLevel() +
		//		   " root " + rootLevel);
                cancelUntil(Math.max(backtrackLevel, rootLevel));
                assert (decisionLevel() >= rootLevel)
                        && (decisionLevel() >= backtrackLevel);
                if (learntConstraint.obj == null) {
		    if (satinDebug) {
			System.out.println("c " + name +
					   ": no learntConstr: UNSAT");
		    }

		    if (satinUseSharedObjects) {
			updateTimingStats(globalState, begintime, loop);
			publishLearnts(globalState, conflictC);
		    }
                    return searchResult(Lbool.FALSE);
                }
                record(learntConstraint.obj);
                learntConstraint.obj = null;
                decayActivities();
            } else {
                // No conflict found
		/* regular SAT4J code now also uncomments this
                if (decisionLevel() == 0 && (satinFixes & 0x2) == 0) {
                    // Simplify the set of problem clause
                    // iff rootLevel==0

	    	    // Experimental: it appears simplifyDB conflicts with
		    // assignment stack splitting.  Still need to figure
		    // out why exactly..
                    stats.rootSimplifications++;
                    boolean ret = simplifyDB();
                    assert ret;
                }
		*/
                // was learnts.size() - nAssigns() > nofLearnts
                assert nAssigns() <= voc.realnVars();
                if (nAssigns() == voc.realnVars()) {
                    slistener.solutionFound();
                    modelFound();
		    System.out.println("c " + name + ": SAT after " +
				       loop + " iterations");
		    if (satinUseSharedObjects) {
			updateTimingStats(globalState, begintime, loop);
			setFinished(globalState, Lbool.TRUE);
		    }
                    return searchResult(Lbool.TRUE);
                }
                // if (conflictC >= nofConflicts) {
                if (conflictC + gloConflicts >= nofConflicts) {
                    // Reached bound on number of conflicts
                    // Force a restart
                    cancelUntil(rootLevel);
		    if (satinUseSharedObjects) {
			// for non-shared objects, this would be printed
			// MANY times
			System.out.println("c " + name +
					   ": conflict bound reached: UNDEF");
			
			// Publishing learnts is useful in case of restarts:
			publishLearnts(globalState, conflictC);
			updateTimingStats(globalState, begintime, loop);
			setFinished(globalState, Lbool.UNDEFINED);
		    }

                    return searchResult(Lbool.UNDEFINED);
                }
		/* SATIN TODO: new sat4j:
                if (needToReduceDB) {
                    reduceDB();
                    needToReduceDB = false;
                    // Runtime.getRuntime().gc();
                }
		*/
                if (nofLearnts >= 0 && learnts.size() > nofLearnts) {
                    // Reduce the set of learnt clauses
                    reduceDB();
                }

		if (satinUseSharedObjects && (loop2++ & 0x3ff) == 0x3ff) {
		    // Heuristic: occasionally check global state to prevent
		    // getting stuck in deep fruitless subtrees
		    sync(); // poll for updates
		    if (globalState.updated(satinSearchIter)) {
			if (verbose) {
			    System.out.println("c " + name +
					       ": pruned running");
			}
			// Root solver may still want to use our learnts:
			updateTimingStats(globalState, begintime, loop);
			publishLearnts(globalState, conflictC);
			return searchResult(Lbool.FALSE);
		    }
		    if ((loop2 & 0xfff) == 0xfff) {
			// Occasionally publish additional learnts
			publishLearnts(globalState, conflictC);
			conflictCsav += conflictC;
			conflictC = 0;
			// Occasionally also check additional learnts
			processGlobalLearnts(globalState, loop);
		    }
		    gloConflicts = globalState.globalConflicts;
		}

		if ((loop > maxIter)
		    && (conflictC + conflictCsav > minLocalConflicts)
		    && (satinDepth < satinMaxSpawnDepth)
		    && (decisionLevel() > rootLevel + 1) // SATIN TODO!!
		    // also need enough memory left for spawner:
		    && (Runtime.getRuntime().freeMemory() > memorybound)
		    )
		{
		    if (loop > stats.maxIter) {
			stats.maxIter = loop;
		    }

		    // Don't spawn ourselves, but let caller decide
		    SolverResult res = new SolverResult(Lbool.UNDEFINED,
							null, stats,
							voc, order);
		    lastConflicts = conflictC; // feedback for spawner
		    res.seqTimeOut = true;
		    return res;
		}

		// New literal decision
		stats.decisions++;
		int p = order.select();
		assert p > 1;

		slistener.assuming(decode2dimacs(p));
		boolean ret = assume(p);
		assert ret;
            }
        } while (undertimeout);

	System.out.println("c " + name + ": timeout: UNDEFINED");
	if (satinUseSharedObjects) {
	    setFinished(globalState, Lbool.UNDEFINED);
	}
        return searchResult(Lbool.UNDEFINED); // timeout occured
    }

    Lbool satinSearch(long nofConflicts, SolverState globalState)
    {
        assert rootLevel == decisionLevel();
        stats.starts++;

        // varDecay = 1 / params.varDecay;
        order.setVarDecay(1 / params.getVarDecay());
        claDecay = 1 / params.getClaDecay();

	SolverResult res;
	// Use external spawner:
	SolverSpawner spawner = new SolverSpawner();
	res = spawner.recSearch(this, nofConflicts,
				0 /* depth */, globalState, null);
	if (res.model != null) {
	    model = res.model;
	}

	if ((satinFixes & 0x10) != 0 && res.stats.maxSpawnDepth > 0) {
	    // For better variable selection in the next round,
	    // take combined activities from children into account.
	    // Note that the original activities at the parent are
	    // also the starting activities at the children, so total
	    // activities are higher than the sequential version
	    // would report.  However, it is relative activities that
	    // matter, and these will be updated like we want them.
	    for (int i = 1; i < res.activities.length; i++) {
		int lit = i << 1;

		// System.out.println(name + " add to activity " + i +
		// 		   " = " + order.varActivity(lit) +
		// 		   " : " +  res.activities[i]);
		order.addActivity(lit, res.activities[i]);
	    }
	}

	System.out.println("c global conflicts: " +
			   globalState.globalConflicts +
			   " max " + nofConflicts);

	PrintWriter out1 = new PrintWriter(System.out, true);
	stats.printStat(out1, "c ");

	// Print job stats
	Enumeration jobs = globalState.timeVec.elements();
	long firsttime = -1;
	long maxtime = 0;
	TimeInfo tiMax = null;
	long totjobs = 0;
	while (jobs.hasMoreElements()) {
	    TimeInfo ti = (TimeInfo) jobs.nextElement();
	    if (firsttime == -1) {
		firsttime = ti.endtime;
	    }

	    totjobs++;
	    if (ti.seqtime > maxtime && ti.iters > 0) {
		tiMax = ti;
	    }
	    if (false) {
		System.out.println("job " + ti.solver +
			       " at " + ((ti.endtime - firsttime) / 1000.0) +
			       ": " + (ti.seqtime / 1000.0) + " s" +
			       " iters " + ti.iters);
	    }
	}
	
	if (tiMax != null) {
	    System.out.println(
		"maxtime " + (tiMax.seqtime / 1000.0) +
		" for job " + tiMax.solver +
		" of " + totjobs +
		" at " + ((tiMax.endtime - firsttime) / 1000.0) +
		" iters " + tiMax.iters);
	}

	if (res.satin_res == Lbool.UNDEFINED) {
	    // Retaining learned clauses only useful in case we are restarted:
	    processGlobalLearnts(globalState, 0);
	}

	return res.satin_res;
    }

    public void satinSolverInit() {
	Properties props = new Properties(System.getProperties());

	satinCloneType = props.getProperty("clone", DEFAULT_CLONE_TYPE);
	System.out.println("Using clone: " + satinCloneType);

	satinFixes =
	    Integer.parseInt(props.getProperty("fixes",
					       "" + DEFAULT_SATIN_FIXES));
	System.out.println("Using fixes: " + satinFixes);

	satinMaxSpawnDepth =
	    Integer.parseInt(props.getProperty("maxspawndepth",
					       "" + DEFAULT_MAX_SPAWN_DEPTH));
	System.out.println("Using maxSpawnDepth: " + satinMaxSpawnDepth);

	satinMaxGlobalLearnSize =
	    Integer.parseInt(props.getProperty("maxlearnsize",
					  "" + DEFAULT_MAX_GLOBAL_LEARN_SIZE));
	System.out.println("Using maxGlobalLearnSize: " +
			   satinMaxGlobalLearnSize);

	int doUseSharedObjects =
	    Integer.parseInt(props.getProperty("sharedobjects", "1"));
	System.out.println("Using sharedobjects: " + doUseSharedObjects);
	satinUseSharedObjects = (doUseSharedObjects != 0);

	if (satinUseSharedObjects) {
	    int doUseSharedClauses =
		Integer.parseInt(props.getProperty("sharedclauses", "0"));
	    System.out.println("Using sharedclauses: " + doUseSharedClauses);
	    satinUseSharedClauses = (doUseSharedClauses != 0);
	}

	satinMaxLocalIter =
	    Integer.parseInt(props.getProperty("maxiter",
					       "" + DEFAULT_MAX_ITER));
	System.out.println("Using maxLocalIter: " + satinMaxLocalIter);

	// Get estimate of remaining free memory after proper startup
	Runtime.getRuntime().gc();
	initialFreeMem = Runtime.getRuntime().freeMemory();
    }
    // SATIN END

    Lbool search(long nofConflicts) {
        assert rootLevel == decisionLevel();
        stats.starts++;
        int conflictC = 0;

        // varDecay = 1 / params.varDecay;
        order.setVarDecay(1 / params.getVarDecay());
        claDecay = 1 / params.getClaDecay();

        do {
            slistener.beginLoop();
            // propage les clauses unitaires
            Constr confl = propagate();
            assert trail.size() == qhead;

            if (confl == null) {
                // No conflict found
                // simpliFYDB() prevents a correct use of
                // constraints removal.
                // if (decisionLevel() == 0) {
                // // Simplify the set of problem clause
                // // iff rootLevel==0
                // stats.rootSimplifications++;
                // boolean ret = simplifyDB();
                // assert ret;
                // }
                // was learnts.size() - nAssigns() > nofLearnts
                // if (nofLearnts.obj >= 0 && learnts.size() > nofLearnts.obj) {
                assert nAssigns() <= voc.realnVars();
                if (nAssigns() == voc.realnVars()) {
                    slistener.solutionFound();
                    modelFound();
                    return Lbool.TRUE;
                }
                if (conflictC >= nofConflicts) {
                    // Reached bound on number of conflicts
                    // Force a restart
                    cancelUntil(rootLevel);
                    return Lbool.UNDEFINED;
                }
                if (needToReduceDB) {
                    reduceDB();
                    needToReduceDB = false;
                    // Runtime.getRuntime().gc();
                }
                // New variable decision
                stats.decisions++;
                int p = order.select();
                assert p > 1;
                slistener.assuming(decode2dimacs(p));
                boolean ret = assume(p);
                assert ret;
            } else {
                // un conflit apparait
                stats.conflicts++;
                conflictC++;
                slistener.conflictFound();
                freeMem.newConflict();
                if (decisionLevel() == rootLevel) {
                    // on est a la racine, la formule est inconsistante
                    return Lbool.FALSE;
                }
                // analyse la cause du conflit
                assert confl != null;
                int backtrackLevel = analyze(confl, learntConstraint);
                assert backtrackLevel < decisionLevel();
                cancelUntil(Math.max(backtrackLevel, rootLevel));
                assert (decisionLevel() >= rootLevel)
                        && (decisionLevel() >= backtrackLevel);
                if (learntConstraint.obj == null) {
                    return Lbool.FALSE;
                }
                record(learntConstraint.obj);
                learntConstraint.obj = null;
                decayActivities();
            }
        } while (undertimeout);
        return Lbool.UNDEFINED; // timeout occured
    }

    /**
     * 
     */
    void modelFound() {
        model = new int[trail.size()];
        fullmodel = new boolean[nVars()];
        int index = 0;
        for (int i = 1; i <= voc.nVars(); i++) {
            if (voc.belongsToPool(i)) {
                int p = voc.getFromPool(i);
                if (!voc.isUnassigned(p)) {
                    model[index++] = voc.isSatisfied(p) ? i : -i;
                    fullmodel[i - 1] = voc.isSatisfied(p);
                }
            }
        }
        assert index == model.length;

	// SATIN BEGIN
	// Added sanity checks: all vars assigned and constraints indeed
	// satisfied.
	// E.g., useful protection against problems in watched-literals
	// implementation, cloning, etc
        if (index != model.length) {
	    System.out.println("*** modelFound but only " + 
			       index + " of " + voc.nVars() +
			       " vars assigned! ***");
	} else {
	    for (int i = 0; i < constrs.size(); i++) {
		Constr c = constrs.get(i);
		VecInt cVec = new VecInt(c.size());

		int res = 0;
		for (int j = 0; j < c.size(); j++) {
		    int lit = c.get(j);
		    boolean sign = ((lit % 2) == 1);
		    int var = lit / 2;
		    if (voc.isSatisfied(voc.getFromPool(var)) != sign) {
			res++;
		    }
		}

		if (res == 0) {
		    System.out.print("*** constraint " + i + "(" + c + ")" +
				     " NOT satisfied: ");
		    for (int j = 0; j < c.size(); j++) {
			System.out.print(" " + c.get(j));
		    }
		    System.out.println();
		} else if (false) {
		    System.out.print("constr " + i + " satisfied: " + res);
		    System.out.print(" (");
		    for (int j = 0; j < c.size(); j++) {
			System.out.print(" " + c.get(j));
		    }
		    System.out.println(")");
		}
	    }
	}
	// SATIN END

        cancelUntil(rootLevel);
    }

    public boolean model(int var) {
        if (var <= 0 || var > nVars()) {
            throw new IllegalArgumentException(
                    "Use a valid Dimacs var id as argument!"); //$NON-NLS-1$
        }
        if (fullmodel == null) {
            throw new UnsupportedOperationException(
                    "Call the solve method first!!!"); //$NON-NLS-1$
        }
        return fullmodel[var - 1];
    }

    /**
     * 
     */
    protected void reduceDB() {
        reduceDB(claInc / learnts.size());
    }

    public void clearLearntClauses() {
        for (Constr c : learnts)
            c.remove();
        learnts.clear();
    }

    protected void reduceDB(double lim) {
        int i, j;
        sortOnActivity();
        stats.reduceddb++;
        for (i = j = 0; i < learnts.size() / 2; i++) {
            Constr c = learnts.get(i);
            if (c.locked()) {
                learnts.set(j++, learnts.get(i));
            } else {
                c.remove();
            }
        }
        for (; i < learnts.size(); i++) {
            // Constr c = learnts.get(i);
            // if (!c.locked() && (c.getActivity() < lim)) {
            // c.remove();
            // } else {
            learnts.set(j++, learnts.get(i));
            // }
        }
        // System.out.println("c cleaning " + (learnts.size() - j) //$NON-NLS-1$
        //        + " clauses out of " + learnts.size() + " for limit " + lim); //$NON-NLS-1$ //$NON-NLS-2$

        System.out.println("c " + name + " cleaning " + (learnts.size() - j) //$NON-NLS-1$
                + " clauses out of " + learnts.size() + ", "
		+ (int) (100.0 * (double) Runtime.getRuntime().freeMemory() /
			 (double) initialFreeMem)
		+ "% free mem"); //$NON-NLS-1$ //$NON-NLS-2$
        learnts.shrinkTo(j);
    }

    /**
     * @param learnts
     */
    private void sortOnActivity() {
        learnts.sort(comparator);
    }

    /**
     * 
     */
    protected void decayActivities() {
        order.varDecayActivity();
        claDecayActivity();
    }

    /**
     * 
     */
    private void claDecayActivity() {
        claInc *= claDecay;
    }

    /**
     * @return true iff the set of constraints is satisfiable, else false.
     */
    public boolean isSatisfiable() throws TimeoutException {
        return isSatisfiable(VecInt.EMPTY);
    }

    private double timebegin = 0;

    private boolean needToReduceDB;

    private ConflictTimer freeMem;

    public boolean isSatisfiable(IVecInt assumps) throws TimeoutException {
        Lbool status = Lbool.UNDEFINED;
        double nofConflicts = params.getInitConflictBound();
        // nofLearnts = Math.min(nofLearnts,10000);
        order.init();
        learner.init();
        timebegin = System.currentTimeMillis();
        slistener.start();
        model = null; // forget about previous model
        fullmodel = null;

	// SATIN BEGIN
	satinSolverInit();
	double scaledNofLearnts = nConstraints()
	    * params.getInitLearntBoundConstraintFactor();
	SolverState globalState = new SolverState();
        // Ceriel: changed order. Did setAllConstraints first, but that
        // causes two broadcasts of the allConstrs value: one in the
        // shared method invocation and one as part of the exportObject.
	if (satinUseSharedObjects) {
	    globalState.exportObject();
	}
	if (satinUseSharedClauses) {
	    // export allConstrs as part of the shared object
	    globalState.setAllConstraints(allConstrs);
	}
	// but don't pass it on as part of the dynamic Solver state:
	allConstrs = null;
	// SATIN END

        // propagate constraints
        if (propagate() != null) {
            slistener.end(Lbool.FALSE);
            cancelUntil(0);
            return false;
        }

        // push incremental assumptions
        for (int q : assumps) {
            if (!assume(voc.getFromPool(q)) || (propagate() != null)) {
                slistener.end(Lbool.FALSE);
                cancelUntil(0);
                return false;
            }
        }
        // StringBuffer stb = new StringBuffer();
        // stb.append("%RESA ");
        // stb.append(nVars());
        // stb.append(" ");
        // stb.append(nConstraints());
        // while(stb.length()<255) {
        // stb.append(' ');
        // }
        // System.err.println(stb);
        rootLevel = decisionLevel();

        TimerTask stopMe = new TimerTask() {
            @Override
            public void run() {
                undertimeout = false;
            }
        };
        undertimeout = true;
        Timer timer = new Timer(true);
        timer.schedule(stopMe, timeout);
        needToReduceDB = false;

        // final long memorybound = Runtime.getRuntime().freeMemory() / 10;
        final long bound1 = Runtime.getRuntime().freeMemory() / 10;
        final long bound2 = 4 * 1024 * 1024; // at least 4 MB
        final long memorybound = (bound1 < bound2) ? bound2 : bound1;
        freeMem = new ConflictTimer(500) {
                    void run() {
                        long freemem = Runtime.getRuntime().freeMemory();
                        // System.out.println("c Free memory "+freemem);
                        // System.out.println("c Free memory "+freemem +
			//		   " bound " + memorybound);
                        if (freemem < memorybound) {
                            // Reduce the set of learnt clauses
                            needToReduceDB = true;
                        }
                    }
                };
        // Solve
        while ((status == Lbool.UNDEFINED) && undertimeout) {
	    if (satinMaxSpawnDepth < 0) { // Original, non-SATIN
		status = search(Math.round(nofConflicts));
		nofConflicts *= params.getConflictBoundIncFactor();
		// order.mirror(stats.starts);
	    } else {
		// SATIN BEGIN
		nofLearnts = Math.round(scaledNofLearnts);
		satinSearchIter++;
		status = satinSearch(Math.round(nofConflicts), globalState);
		if (status == Lbool.UNDEFINED) {
		    // Need to explicitly cancel back everything because
		    // - some implementations modify the root solver now
		    // - may want to start with whole new path based on latest
		    //   statistics anyway
		    cancelUntil(0);
		}

		globalState.reinit(false);

		System.out.println("c search status " + status);
		nofConflicts *= params.getConflictBoundIncFactor();
		scaledNofLearnts *= params.getLearntBoundIncFactor();
		// order.mirror(stats.starts);
		Runtime.getRuntime().gc(); // TODO: useful?
		// SATIN END
	    }
        }

        cancelUntil(0);
        timer.cancel();
        slistener.end(status);
        if (!undertimeout) {
            throw new TimeoutException(" Timeout (" + timeout + "s) exceeded"); //$NON-NLS-1$//$NON-NLS-2$
        }
        return status == Lbool.TRUE;
    }

    public SolverStats getStats() {
        return stats;
    }

    public IOrder getOrder() {
        return order;
    }

    public void setOrder(IOrder h) {
        order = h;
        order.setLits(voc);
    }

    public ILits getVocabulary() {
        return voc;
    }

    public void reset() {
        // FIXME verify that cleanup is OK
        voc.resetPool();
        dsfactory.reset();
        constrs.clear();
        learnts.clear();
        stats.reset();
    }

    public int nVars() {
        return voc.nVars();
    }

    /**
     * @param constr
     *            a constraint implementing the Constr interface.
     * @return a reference to the constraint for external use.
     */
    protected IConstr addConstr(Constr constr) {
        if (constr != null) {
            constrs.push(constr);
        }
        return constr;
    }

    public DataStructureFactory getDSFactory() {
        return dsfactory;
    }

    public IVecInt getOutLearnt() {
        return outLearnt;
    }

    /**
     * returns the ith constraint in the solver.
     * 
     * @param i
     *            the constraint number (begins at 0)
     * @return the ith constraint
     */
    public IConstr getIthConstr(int i) {
        return constrs.get(i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.specs.ISolver#printStat(java.io.PrintStream,
     *      java.lang.String)
     */
    public void printStat(PrintStream out, String prefix) {
        printStat(new PrintWriter(out), prefix);
    }

    public void printStat(PrintWriter out, String prefix) {
        stats.printStat(out, prefix);
        double cputime = (System.currentTimeMillis() - timebegin) / 1000;
        out.println(prefix + "speed (decisions/second)\t: " + stats.decisions //$NON-NLS-1$
                / cputime);
        order.printStat(out, prefix);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString(String prefix) {
        StringBuilder stb = new StringBuilder();
        Object[] objs = { analyzer, dsfactory, learner, params, order,
			  simplifier
	};
        // stb.append(prefix);
        stb.append("--- Begin Solver configuration ---"); //$NON-NLS-1$
        stb.append("\n"); //$NON-NLS-1$
        for (Object o : objs) {
            stb.append(prefix);
            stb.append(o.toString());
            stb.append("\n"); //$NON-NLS-1$
        }
        stb.append(prefix);
        stb.append("--- End Solver configuration ---"); //$NON-NLS-1$
        return stb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return toString(""); //$NON-NLS-1$
    }

    public int getTimeout() {
        return (int)(timeout/1000);
    }

    public void setExpectedNumberOfClauses(int nb) {
        constrs.ensure(nb);
    }

    public Map<String, Number> getStat() {
        return stats.toMap();
    }

    public int[] findModel() throws TimeoutException {
        if (isSatisfiable()) {
            return model();
        }
        return null;
    }

    public int[] findModel(IVecInt assumps) throws TimeoutException {
        if (isSatisfiable(assumps)) {
            return model();
        }
        return null;
    }

}

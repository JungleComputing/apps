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

package org.sat4j.minisat;

import java.io.Serializable;

import org.sat4j.core.ASolverFactory;
import org.sat4j.minisat.constraints.CardinalityDataStructure;
import org.sat4j.minisat.constraints.ClausalDataStructureCB;
import org.sat4j.minisat.constraints.ClausalDataStructureCBWL;
import org.sat4j.minisat.constraints.MixedDataStructureDaniel;
import org.sat4j.minisat.constraints.MixedDataStructureWithBinary;
import org.sat4j.minisat.constraints.MixedDataStructureWithBinaryAndTernary;
import org.sat4j.minisat.constraints.PBMaxCBClauseCardConstrDataStructure;
import org.sat4j.minisat.constraints.PBMaxClauseAtLeastConstrDataStructure;
import org.sat4j.minisat.constraints.PBMaxClauseCardConstrDataStructure;
import org.sat4j.minisat.constraints.PBMaxDataStructure;
import org.sat4j.minisat.constraints.PBMinClauseCardConstrDataStructure;
import org.sat4j.minisat.constraints.PBMinDataStructure;
import org.sat4j.minisat.constraints.PuebloPBMinClauseAtLeastConstrDataStructure;
import org.sat4j.minisat.constraints.PuebloPBMinClauseCardConstrDataStructure;
import org.sat4j.minisat.constraints.PuebloPBMinDataStructure;
import org.sat4j.minisat.constraints.pb.PBSolver;
import org.sat4j.minisat.constraints.pb.PBSolverClause;
import org.sat4j.minisat.constraints.pb.PBSolverWithImpliedClause;
import org.sat4j.minisat.core.DataStructureFactory;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.SearchParams;
import org.sat4j.minisat.core.Solver;
import org.sat4j.minisat.learning.ActiveLearning;
import org.sat4j.minisat.learning.FixedLengthLearning;
import org.sat4j.minisat.learning.LimitedLearning;
import org.sat4j.minisat.learning.MiniSATLearning;
import org.sat4j.minisat.learning.NoLearningButHeuristics;
import org.sat4j.minisat.orders.JWOrder;
import org.sat4j.minisat.orders.MyOrder;
import org.sat4j.minisat.orders.PureOrder;
import org.sat4j.minisat.orders.VarOrder;
import org.sat4j.minisat.orders.VarOrderHeap;
import org.sat4j.minisat.orders.VarOrderHeapObjective;
import org.sat4j.minisat.uip.DecisionUIP;
import org.sat4j.minisat.uip.FirstUIP;
import org.sat4j.specs.ISolver;
import org.sat4j.tools.DimacsOutputSolver;

// SATIN:
import java.util.Properties;
import java.lang.Double;
import java.lang.Integer;


/**
 * User friendly access to pre-constructed solvers.
 * 
 * @author leberre
 */
public class SolverFactory extends ASolverFactory implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // thread safe implementation of the singleton design pattern
    private static SolverFactory instance;

    /**
     * Private contructor. Use singleton method instance() instead.
     * 
     * @see #instance()
     */
    private SolverFactory() {
        super();
    }

    private static synchronized void createInstance() {
        if (instance == null) {
            instance = new SolverFactory();
        }
    }

    /**
     * Access to the single instance of the factory.
     * 
     * @return the singleton of that class.
     */
    public static SolverFactory instance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    /**
     * @return a "default" "minilearning" solver learning clauses of size
     *         smaller than 10 % of the total number of variables
     */
    public static ISolver newMiniLearning() {
        return newMiniLearning(10);
    }

    /**
     * @return a "default" "minilearning" solver learning clauses of size
     *         smaller than 10 % of the total number of variables with a heap
     *         based var order.
     */
    public static ISolver newMiniLearningHeap() {
        return newMiniLearningHeap(new MixedDataStructureDaniel());
    }

    public static ISolver newMiniLearningHeapEZSimp() {
        Solver solver = (Solver) newMiniLearningHeap();
        solver.setSimplifier(solver.SIMPLE_SIMPLIFICATION);
        return solver;
    }

    public static ISolver newMiniLearningHeapExpSimp() {
        Solver solver = (Solver) newMiniLearningHeap();
        solver.setSimplifier(solver.EXPENSIVE_SIMPLIFICATION);
        return solver;
    }

    /**
     * @param n
     *            the maximal size of the clauses to learn as a percentage of
     *            the initial number of variables
     * @return a "minilearning" solver learning clauses of size smaller than n
     *         of the total number of variables
     */
    public static ISolver newMiniLearning(int n) {
        return newMiniLearning(new MixedDataStructureDaniel(), n);
    }

    /**
     * @param dsf
     *            a specific data structure factory
     * @return a default "minilearning" solver using a specific data structure
     *         factory, learning clauses of length smaller or equals to 10 % of
     *         the number of variables.
     */
    public static ISolver newMiniLearning(DataStructureFactory dsf) {
        return newMiniLearning(dsf, 10);
    }

    /**
     * @param dsf
     *            a specific data structure factory
     * @return a default "minilearning" solver using a specific data structure
     *         factory, learning clauses of length smaller or equals to 10 % of
     *         the number of variables and a heap based VSIDS heuristics
     */
    public static ISolver newMiniLearningHeap(DataStructureFactory dsf) {
        return newMiniLearning(dsf, new VarOrderHeap());
    }

    /**
     * @return a default minilearning solver using a specific data structure
     *         described in Lawrence Ryan thesis to handle binary clauses.
     * @see #newMiniLearning
     */
    public static ISolver newMiniLearning2() {
        return newMiniLearning(new MixedDataStructureWithBinary());
    }

    public static ISolver newMiniLearning2Heap() {
        return newMiniLearningHeap(new MixedDataStructureWithBinary());
    }

    /**
     * @return a default minilearning solver using a specific data structures
     *         described in Lawrence Ryan thesis to handle binary and ternary
     *         clauses.
     * @see #newMiniLearning
     */
    public static ISolver newMiniLearning23() {
        return newMiniLearning(new MixedDataStructureWithBinaryAndTernary());
    }

    /**
     * @return a default minilearning SAT solver using counter-based clause
     *         representation (i.e. all the literals of a clause are watched)
     */
    public static ISolver newMiniLearningCB() {
        return newMiniLearning(new ClausalDataStructureCB());
    }

    /**
     * @return a default minilearning SAT solver using counter-based clause
     *         representation (i.e. all the literals of a clause are watched)
     *         for the ORIGINAL clauses and watched-literals clause
     *         representation for learnt clauses.
     */
    public static ISolver newMiniLearningCBWL() {
        return newMiniLearning(new ClausalDataStructureCBWL());
    }

    public static ISolver newMiniLearning2NewOrder() {
        return newMiniLearning(new MixedDataStructureWithBinary(),
                new MyOrder());
    }

    /**
     * @return a default minilearning SAT solver choosing periodically to branch
     *         on "pure watched" literals if any. (a pure watched literal l is a
     *         literal that is watched on at least one clause such that its
     *         negation is not watched at all. It is not necessarily a watched
     *         literal.)
     */
    public static ISolver newMiniLearningPure() {
        return newMiniLearning(new MixedDataStructureDaniel(), new PureOrder());
    }

    /**
     * @return a default minilearning SAT solver choosing periodically to branch
     *         on literal "pure in the original set of clauses" if any.
     */
    public static ISolver newMiniLearningCBWLPure() {
        return newMiniLearning(new ClausalDataStructureCBWL(), new PureOrder());
    }

    /**
     * @param dsf
     *            the data structure factory used to represent literals and
     *            clauses
     * @param n
     *            the maximum size of learnt clauses as percentage of the
     *            original number of variables.
     * @return a SAT solver with learning limited to clauses of length smaller
     *         or equal to n, the dsf data structure, the FirstUIP clause
     *         generator and a sort of VSIDS heuristics.
     */
    public static ISolver newMiniLearning(DataStructureFactory dsf, int n) {
        LimitedLearning learning = new LimitedLearning(n);
        Solver solver = new Solver(new FirstUIP(), learning, dsf,
                new VarOrder());
        learning.setSolver(solver);
        return solver;
    }

    /**
     * @param dsf
     *            the data structure factory used to represent literals and
     *            clauses
     * @param order
     *            the heuristics
     * @return a SAT solver with learning limited to clauses of length smaller
     *         or equal to 10 percent of the total number of variables, the dsf
     *         data structure, the FirstUIP clause generator and order as
     *         heuristics.
     */
    public static ISolver newMiniLearning(DataStructureFactory dsf, IOrder order) {
        LimitedLearning learning = new LimitedLearning(10);
        Solver solver = new Solver(new FirstUIP(), learning, dsf, order);
        learning.setSolver(solver);
        return solver;
    }

    public static ISolver newMiniLearningEZSimp() {
        return newMiniLearningEZSimp(new MixedDataStructureDaniel());
    }

    // public static ISolver newMiniLearning2EZSimp() {
    // return newMiniLearningEZSimp(new MixedDataStructureWithBinary());
    // }

    public static ISolver newMiniLearningEZSimp(DataStructureFactory dsf) {
        LimitedLearning learning = new LimitedLearning(10);
        Solver solver = new Solver(new FirstUIP(), learning, dsf,
                new VarOrder());
        learning.setSolver(solver);
        solver.setSimplifier(solver.SIMPLE_SIMPLIFICATION);
        return solver;
    }

    /**
     * @return a default MiniLearning without restarts.
     */
    public static ISolver newMiniLearningHeapEZSimpNoRestarts() {
        LimitedLearning learning = new LimitedLearning(10);
        Solver solver = new Solver(new FirstUIP(), learning,
                new MixedDataStructureDaniel(), new SearchParams(
                        Integer.MAX_VALUE), new VarOrderHeap());
        learning.setSolver(solver);
        solver.setSimplifier(solver.SIMPLE_SIMPLIFICATION);
        return solver;
    }

    /**
     * @return a default MiniLearning with restarts beginning at 1000 conflicts.
     */
    public static ISolver newMiniLearningHeapEZSimpLongRestarts() {
        LimitedLearning learning = new LimitedLearning(10);
        Solver solver = new Solver(new FirstUIP(), learning,
                new MixedDataStructureDaniel(), new SearchParams(1000),
                new VarOrderHeap());
        learning.setSolver(solver);
        solver.setSimplifier(solver.SIMPLE_SIMPLIFICATION);
        return solver;
    }

    /**
     * @return a SAT solver using First UIP clause generator, watched literals,
     *         VSIDS like heuristics learning only clauses having a great number
     *         of active variables, i.e. variables with an activity strictly
     *         greater than one.
     */
    public static ISolver newActiveLearning() {
        ActiveLearning learning = new ActiveLearning();
        Solver s = new Solver(new FirstUIP(), learning,
                new MixedDataStructureDaniel(), new VarOrder());
        learning.setOrder(s.getOrder());
        learning.setSolver(s);
        return s;
    }

    /**
     * @return a SAT solver very close to the original MiniSAT sat solver.
     */
    public static ISolver newMiniSAT() {
        return newMiniSAT(new MixedDataStructureDaniel());
    }

    /**
     * @return MiniSAT without restarts.
     */
    public static ISolver newMiniSATNoRestarts() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new Solver(new FirstUIP(), learning,
                new MixedDataStructureDaniel(), new SearchParams(
                        Integer.MAX_VALUE), new VarOrder());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;

    }

    // START SATIN
    /**
     * @return MiniSAT with limited restarts, as specified using attributes
     */
    public static ISolver newMiniSATLimitedRestarts() {
	SearchParams pars;

	Properties props = new Properties(System.getProperties());

	double varDecay =
	    (new Double(props.getProperty("varDecay", "0.95"))).doubleValue();
	System.out.println("Using varDecay: " + varDecay);

	double claDecay =
	    (new Double(props.getProperty("claDecay", "0.999"))).doubleValue();
	System.out.println("Using claDecay: " + claDecay);

	double conflictInc =
	   (new Double(props.getProperty("conflictInc", "1.5"))).doubleValue();
	System.out.println("Using conflictInc: " + conflictInc);

	double learntInc =
	    (new Double(props.getProperty("learntInc", "1.1"))).doubleValue();
	System.out.println("Using learntInc: " + learntInc);

	double learntConstraint =
	    (new Double(props.getProperty("learntConstraint",
					  "0.5"))).doubleValue();
	System.out.println("Using learntConstraint: " + learntConstraint);

	int conflictInit = 
	    Integer.parseInt(props.getProperty("conflictInit", "100"));
	System.out.println("Using conflictInit: " + conflictInit);

	String varOrder = props.getProperty("varOrder", "default");
	System.out.println("Using varOrder: " + varOrder);

	IOrder order;
	DataStructureFactory dsf;
	if (varOrder.equals("heap")) {
	    dsf = new MixedDataStructureDaniel();
	    order = new VarOrderHeap();
	} else if (varOrder.equals("myorder")) {
	    order = new MyOrder();
	    dsf = new MixedDataStructureWithBinary();
	} else if (varOrder.equals("pure")) {
	    dsf = new MixedDataStructureDaniel();
	    order = new PureOrder();
	} else if (varOrder.equals("jw")) {
	    order = new JWOrder();
	    dsf = new MixedDataStructureWithBinaryAndTernary();
	} else if (varOrder.equals("varorder")) {
	    dsf = new MixedDataStructureWithBinary();
	    order = new VarOrder();
	} else { // default like MiniSAT2Heap:
	    dsf = new MixedDataStructureWithBinary();
	    order = new VarOrderHeap();
	}

	pars = new SearchParams(varDecay, claDecay,
				conflictInc, conflictInit,
				learntInc, learntConstraint);

        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new Solver(new FirstUIP(),
				   learning,
				   dsf,
				   pars,
				   order);
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }
    // END SATIN

    /**
     * @return MiniSAT with a special data structure from Lawrence Ryan thesis
     *         for managing binary clauses.
     */
    public static ISolver newMiniSAT2() {
        return newMiniSAT(new MixedDataStructureWithBinary());
    }

    /**
     * @return MiniSAT with a special data structure from Lawrence Ryan thesis
     *         for managing binary and ternary clauses.
     */
    public static ISolver newMiniSAT23() {
        return newMiniSAT(new MixedDataStructureWithBinaryAndTernary());
    }

    /**
     * @param dsf
     *            the data structure used for representing clauses and lits
     * @return MiniSAT the data structure dsf.
     */
    public static ISolver newMiniSAT(DataStructureFactory dsf) {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new Solver(new FirstUIP(), learning, dsf,
                new VarOrder());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return a SAT solver very close to the original MiniSAT sat solver.
     */
    public static ISolver newMiniSATHeap() {
        return newMiniSATHeap(new MixedDataStructureDaniel());
    }

    /**
     * @return a SAT solver very close to the original MiniSAT sat solver
     *         including easy reason simplification.
     */
    public static ISolver newMiniSATHeapEZSimp() {
        Solver solver = (Solver) newMiniSATHeap();
        solver.setSimplifier(solver.SIMPLE_SIMPLIFICATION);
        return solver;
    }

    public static ISolver newMiniSATHeapExpSimp() {
        Solver solver = (Solver) newMiniSATHeap();
        solver.setSimplifier(solver.EXPENSIVE_SIMPLIFICATION);
        return solver;
    }

    /**
     * @return MiniSAT with a special data structure from Lawrence Ryan thesis
     *         for managing binary clauses.
     */
    public static ISolver newMiniSAT2Heap() {
        return newMiniSATHeap(new MixedDataStructureWithBinary());
    }

    /**
     * @return MiniSAT with a special data structure from Lawrence Ryan thesis
     *         for managing binary and ternary clauses.
     */
    public static ISolver newMiniSAT23Heap() {
        return newMiniSATHeap(new MixedDataStructureWithBinaryAndTernary());
    }

    public static ISolver newMiniSATHeap(DataStructureFactory dsf) {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new Solver(new FirstUIP(), learning, dsf,
                new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with data structures to handle cardinality constraints.
     */
    public static ISolver newMiniCard() {
        return newMiniSAT(new CardinalityDataStructure());
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and clause
     *         learning.
     */
    public static ISolver newMinimalOPBMax() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new Solver(new FirstUIP(), learning,
                new PBMaxDataStructure(), new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and
     *         constraint learning.
     */
    public static ISolver newMiniOPBMax() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new PBSolver(new FirstUIP(), learning,
                new PBMaxDataStructure(), new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and
     *         constraint learning. Clauses and cardinalities with watched
     *         literals are also handled (and learnt).
     */
    public static ISolver newMiniOPBClauseCardConstrMax() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new PBSolver(new FirstUIP(), learning,
                new PBMaxClauseCardConstrDataStructure(), new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and
     *         constraint learning. Clauses and cardinalities with watched
     *         literals are also handled (and learnt). A specific heuristics
     *         taking into account the objective value is used.
     */
    public static ISolver newMiniOPBClauseCardConstrMaxSpecificOrder() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new PBSolver(new FirstUIP(), learning,
                new PBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniLearning with Counter-based pseudo boolean constraints and
     *         constraint learning. Clauses and cardinalities with watched
     *         literals are also handled (and learnt). A specific heuristics
     *         taking into account the objective value is used.
     */
    public static ISolver newMiniLearningOPBClauseCardConstrMaxSpecificOrderIncremental() {
        // LimitedLearning learning = new LimitedLearning(10);
        MiniSATLearning learning = new MiniSATLearning();
        // LearningStrategy learning = new NoLearningButHeuristics();
        Solver solver = new PBSolver(new FirstUIP(), learning,
                new PBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    public static ISolver newMinimalOPBClauseCardConstrMaxSpecificOrder() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new Solver(new FirstUIP(), learning,
                new PBMaxClauseCardConstrDataStructure(),
                new VarOrderHeapObjective());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and
     *         constraint learning. Clauses and cardinalities with watched
     *         literals are also handled (and learnt). A reduction of
     *         PB-constraints to clauses is made in order to simplify cutting
     *         planes.
     */
    public static ISolver newMiniOPBClauseCardConstrMaxReduceToClause() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new PBSolverClause(new FirstUIP(), learning,
                new PBMaxClauseCardConstrDataStructure(), new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and
     *         constraint learning. Clauses and cardinalities with watched
     *         literals are also handled (and learnt). A reduction of
     *         PB-constraints to cardinalities is made in order to simplify
     *         cutting planes.
     */
    // public static ISolver newMiniOPBClauseCardConstrMaxReduceToCard() {
    // MiniSATLearning learning = new MiniSATLearning();
    // Solver solver = new PBSolverCard(new FirstUIP(), learning,
    // new PBMaxClauseCardConstrDataStructure(), new VarOrderHeap());
    // learning.setDataStructureFactory(solver.getDSFactory());
    // learning.setVarActivityListener(solver);
    // return solver;
    // }
    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and
     *         constraint learning. Clauses and cardinalities with watched
     *         literals are also handled (and learnt). a pre-processing is
     *         applied which adds implied clauses from PB-constraints.
     */
    public static ISolver newMiniOPBClauseCardConstrMaxImplied() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new PBSolverWithImpliedClause(new FirstUIP(), learning,
                new PBMaxClauseCardConstrDataStructure(), new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints,
     *         counter-based cardinalities, watched clauses and constraint
     *         learning. methods isAssertive() and getBacktrackLevel() are
     *         totally incremental. Conflicts for PB-constraints use a Map
     *         structure
     */
    public static ISolver newMiniOPBClauseAtLeastConstrMax() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new PBSolver(new FirstUIP(), learning,
                new PBMaxClauseAtLeastConstrDataStructure(), new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with Counter-based pseudo boolean constraints and
     *         clauses, watched cardinalities, and constraint learning.
     */
    public static ISolver newMiniOPBCounterBasedClauseCardConstrMax() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new PBSolver(new FirstUIP(), learning,
                new PBMaxCBClauseCardConstrDataStructure(), new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with WL-based pseudo boolean constraints and clause
     *         learning.
     */
    public static ISolver newMinimalOPBMin() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new Solver(new FirstUIP(), learning,
                new PBMinDataStructure(), new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with WL-based pseudo boolean constraints and constraint
     *         learning.
     */
    public static ISolver newMiniOPBMin() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new PBSolver(new FirstUIP(), learning,
                new PBMinDataStructure(), new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with WL-based pseudo boolean constraints and clause
     *         learning.
     */
    public static ISolver newMinimalOPBMinPueblo() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new Solver(new FirstUIP(), learning,
                new PuebloPBMinDataStructure(), new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with WL-based pseudo boolean constraints and constraint
     *         learning.
     */
    public static ISolver newMiniOPBMinPueblo() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new PBSolver(new FirstUIP(), learning,
                new PuebloPBMinDataStructure(), new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with WL-based pseudo boolean constraints and clauses,
     *         cardinalities, and constraint learning.
     */
    public static ISolver newMiniOPBClauseCardMinPueblo() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new PBSolver(new FirstUIP(), learning,
                new PuebloPBMinClauseCardConstrDataStructure(),
                new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with WL-based pseudo boolean constraints and clauses,
     *         cardinalities, and constraint learning.
     */
    public static ISolver newMiniOPBClauseCardMin() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new PBSolver(new FirstUIP(), learning,
                new PBMinClauseCardConstrDataStructure(), new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with WL-based pseudo boolean constraints and clauses,
     *         counter-based cardinalities, and constraint learning.
     */
    public static ISolver newMiniOPBClauseAtLeastMinPueblo() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new PBSolver(new FirstUIP(), learning,
                new PuebloPBMinClauseAtLeastConstrDataStructure(),
                new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with decision UIP clause generator.
     */
    public static ISolver newRelsat() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new Solver(new DecisionUIP(), learning,
                new MixedDataStructureDaniel(), new VarOrderHeap());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return MiniSAT with VSIDS heuristics, FirstUIP clause generator for
     *         backjumping but no learning.
     */
    public static ISolver newBackjumping() {
        NoLearningButHeuristics learning = new NoLearningButHeuristics();
        Solver solver = new Solver(new FirstUIP(), learning,
                new MixedDataStructureDaniel(), new VarOrderHeap());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * @return a SAT solver with learning limited to clauses of length smaller
     *         or equals to 3, with a specific data structure for binary and
     *         ternary clauses as found in Lawrence Ryan thesis, without
     *         restarts, with a Jeroslow/Wang kind of heuristics.
     */
    public static ISolver newMini3SAT() {
        LimitedLearning learning = new FixedLengthLearning(3);
        Solver solver = new Solver(new FirstUIP(), learning,
                new MixedDataStructureWithBinaryAndTernary(), new SearchParams(
                        Integer.MAX_VALUE), new JWOrder());
        learning.setSolver(solver);
        return solver;
    }

    /**
     * @return a Mini3SAT with full learning.
     * @see #newMini3SAT()
     */
    public static ISolver newMini3SATb() {
        MiniSATLearning learning = new MiniSATLearning();
        Solver solver = new Solver(new FirstUIP(), learning,
                new MixedDataStructureWithBinaryAndTernary(), new SearchParams(
                        Integer.MAX_VALUE), new JWOrder());
        learning.setDataStructureFactory(solver.getDSFactory());
        learning.setVarActivityListener(solver);
        return solver;
    }

    /**
     * Default solver of the SolverFactory. This solver is meant to be used on
     * challenging SAT benchmarks.
     * 
     * @return the best "general purpose" SAT solver available in the factory.
     * @see #defaultSolver() the same method, polymorphic, to be called from an
     *      instance of ASolverFactory.
     */
    public static ISolver newDefault() {
        return newMiniLearningHeapExpSimp();
    }

    @Override
    public ISolver defaultSolver() {
        return newDefault();
    }

    /**
     * Small footprint SAT solver.
     * 
     * @return a SAT solver suitable for solving small/easy SAT benchmarks.
     * @see #lightSolver() the same method, polymorphic, to be called from an
     *      instance of ASolverFactory.
     */
    public static ISolver newLight() {
        return newMini3SAT();
    }

    @Override
    public ISolver lightSolver() {
        return newLight();
    }

    public static ISolver newDimacsOutput() {
        return new DimacsOutputSolver();
    }

}

/*
 * SAT4J: a SATisfiability library for Java   
 * Copyright (C) 2004 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas E�n and Niklas S�rensson.
 * Proceedings of the Sixth International Conference on Theory 
 * and Applications of Satisfiability Testing, LNCS 2919, 
 * pp 502-518, 2003.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 */

package org.sat4j.minisat.orders;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Random;

import org.sat4j.minisat.core.Heap;
import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.IOrder;

/*
 * Created on 16 oct. 2003
 */

/**
 * @author leberre Heuristique du prouveur. Changement par rapport au MiniSAT
 *         original : la gestion activity est faite ici et non plus dans Solver.
 */
public class VarOrderHeap implements IOrder, Serializable, Cloneable {

    private static final long serialVersionUID = 1L;

    private static final double VAR_RESCALE_FACTOR = 1e-100;

    private static final double VAR_RESCALE_BOUND = 1 / VAR_RESCALE_FACTOR;

    /**
     * mesure heuristique de l'activit� d'une variable.
     */
    protected double[] activity = new double[1];

    private double varDecay = 1.0;

    /**
     * incr�ment pour l'activit� des variables.
     */
    private double varInc = 1.0;

    protected ILits lits;

    private long nullchoice = 0;

    private long randchoice = 0;

    private Random rand = new Random(12345);

    private final static double RANDOM_WALK = 0.05;

    private Heap heap;

    private int [] phase;
    
    public void setLits(ILits lits) {
        this.lits = lits;
    }

    /**
     * Appel�e quand une nouvelle variable est cr��e.
     */
    public void newVar() {
        newVar(1);
    }

    /**
     * Appel�e lorsque plusieurs variables sont cr��es
     * 
     * @param howmany
     *            le nombre de variables cr��es
     */
    public void newVar(int howmany) {
    }

    /**
     * S�lectionne une nouvelle variable, non affect�e, ayant l'activit�
     * la plus �lev�e.
     * 
     * @return Lit.UNDEFINED si aucune variable n'est trouv�e
     */
    public int select() {
        while (!heap.empty()) {
            int var = heap.getmin();
            int next = phase[var];
            if (lits.isUnassigned(next)) {
                if (activity[var] < 0.0001) {
                    nullchoice++;
                }
                return next;
            }
        }

	// Satin debug:
	System.out.println("*** order.select(): UNDEFINED");
	System.out.println("*** order.select(): heap " + heap);
        int nlength = lits.nVars() + 1;
        for (int i = 1; i < nlength; i++) {
            if (lits.belongsToPool(i)) {
		if (lits.isUnassigned(i << 1)) {
		    System.out.println("might still have chosen " + i +
				       " phase " + phase[i] +
				       " activity " + activity[i]);
		}
            } else {
		System.out.println("not in pool " + i);
            }
        }

        return ILits.UNDEFINED;
    }

    /**
     * Change la valeur de varDecay.
     * 
     * @param d
     *            la nouvelle valeur de varDecay
     */
    public void setVarDecay(double d) {
        varDecay = d;
    }

    /**
     * M�thode appel�e quand la variable x est d�saffect�e.
     * 
     * @param x
     */
    public void undo(int x) {
        if (!heap.inHeap(x))
            heap.insert(x);
    }

    /**
     * Appel�e lorsque l'activit� de la variable x a chang�.
     * 
     * @param p a literal
     */
    public void updateVar(int p) {
        int var = p >> 1;
        updateActivity(var);
        phase[var] = p;
        if (heap.inHeap(var))
            heap.increase(var);
    }

    protected void updateActivity(final int var) {
        if ((activity[var] += varInc) > VAR_RESCALE_BOUND) {
            varRescaleActivity();
        }
    }

    // For satin:
    public void setActivity(int p, double val) {
	int var = p >> 1;
        activity[var] = val;
        if (heap.inHeap(var)) {
            heap.increase(var);
	}
    }

    /**
     * 
     */
    public void varDecayActivity() {
        varInc *= varDecay;
    }

    /**
     * 
     */
    private void varRescaleActivity() {
        for (int i = 1; i < activity.length; i++) {
            activity[i] *= VAR_RESCALE_FACTOR;
        }
        varInc *= VAR_RESCALE_FACTOR;
    }

    public double varActivity(int p) {
        return activity[p >> 1];
    }

    /**
     * 
     */
    public int numberOfInterestingVariables() {
        int cpt = 0;
        for (int i = 1; i < activity.length; i++) {
            if (activity[i] > 1.0) {
                cpt++;
            }
        }
        return cpt;
    }

    /**
     * that method has the responsability to initialize all arrays in the
     * heuristics. PLEASE CALL super.init() IF YOU OVERRIDE THAT METHOD.
     */
    public void init() {
        int nlength = lits.nVars() + 1;
        activity = new double[nlength];
        phase = new int[nlength];
        activity[0] = -1;
        heap = new Heap(activity);
        heap.setBounds(nlength);
        for (int i = 1; i < nlength; i++) {
            assert i > 0;
            assert i <= lits.nVars() : "" + lits.nVars() + "/" + i;
            activity[i] = 0.0;
            if (lits.belongsToPool(i)) {
                heap.insert(i);
                phase[i] = (i<<1)^1;
            } else {
                phase[i] = ILits.UNDEFINED;
            }
        }
    }

    public Object clone() {
	VarOrderHeap clone;

	try {
	    clone = (VarOrderHeap) super.clone();
	}
	catch (CloneNotSupportedException e) {
	    throw new InternalError(e.toString());
	}
	
	clone.activity = (double[]) this.activity.clone();
	clone.phase    = (int []) this.phase.clone();
	clone.heap     = (Heap) this.heap.clone();
	clone.heap.setActivity(clone.activity);
	// lits is set by setLits() after cloning

	return clone;
    }

    public String toString() {
        return "VSIDS like heuristics from MiniSAT using a heap";
    }

    public ILits getVocabulary() {
        return lits;
    }

    public void printStat(PrintStream out, String prefix) {
        out.println(prefix + "non guided choices\t" + nullchoice);
        out.println(prefix + "random choices\t" + randchoice);
    }
}

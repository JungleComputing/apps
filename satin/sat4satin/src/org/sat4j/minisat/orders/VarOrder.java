/*
 * SAT4J: a SATisfiability library for Java   
 * Copyright (C) 2004 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas Een and Niklas Serensson.
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

import org.sat4j.minisat.core.ILits;
import org.sat4j.minisat.core.IOrder;

/*
 * Created on 16 oct. 2003
 */

/**
 * @author leberre Heuristique du prouveur. Changement par rapport au MiniSAT
 *         original : la gestion activity est faite ici et non plus dans Solver.
 */
public class VarOrder implements Serializable, Cloneable, IOrder {

    private static final long serialVersionUID = 1L;

    /**
     * Comparateur permettant de trier les variables
     */
    private static final double VAR_RESCALE_FACTOR = 1e-100;

    private static final double VAR_RESCALE_BOUND = 1 / VAR_RESCALE_FACTOR;

    /**
     * mesure heuristique de l'activite d'une variable.
     */
    protected double[] activity = new double[1];

    /**
     * Derniere variable choisie
     */
    protected int lastVar = 1;

    /**
     * Ordre des variables
     */
    protected int[] order = new int[1];

    private double varDecay = 1.0;

    /**
     * increment pour l'activite des variables.
     */
    private double varInc = 1.0;

    /**
     * position des variables
     */
    protected int[] varpos = new int[1];

    protected ILits lits;

    private long nullchoice = 0;

    private long randchoice = 0;

    private Random rand = new Random(12345);

    private final static double RANDOM_WALK = 0.05;

    /* (non-Javadoc)
     * @see org.sat4j.minisat.core.IOrder#setLits(org.sat4j.minisat.core.ILits)
     */
    public void setLits(ILits lits) {
	// System.err.println("VarOrder " + this + " setLits " +  lits +
	//		   " was " + this.lits);
	// " at 47 now: " + lits.valueToString(47));
        this.lits = lits;
    }

    /* (non-Javadoc)
     * @see org.sat4j.minisat.core.IOrder#newVar()
     */
    public void newVar() {
        newVar(1);
    }

    /* (non-Javadoc)
     * @see org.sat4j.minisat.core.IOrder#newVar(int)
     */
    public void newVar(int howmany) {
    }

    /* (non-Javadoc)
     * @see org.sat4j.minisat.core.IOrder#select()
     */
    public int select() {
        assert lastVar > 0;
	// System.err.println("*** lastVar " + lastVar);
	// System.err.println("*** lits " + lits);
	// System.err.println("*** order " + order);
	// System.err.println("*** order.length " + order.length);
        for (int i = lastVar; i < order.length; i++) {
            assert i > 0;
            if (lits.isUnassigned(order[i])) {
                lastVar = i;
                if (activity[i] < 0.0001) {
                    // if (rand.nextDouble() <= RANDOM_WALK) {
                    // int randomchoice = rand.nextInt(order.length - i) + i;
                    // assert randomchoice >= i;
                    // if ((randomchoice > i)
                    // && lits.isUnassigned(order[randomchoice])) {
                    // randchoice++;
                    // return order[randomchoice];
                    // }
                    // }
                    nullchoice++;
		    // System.err.println("** nullchoice " + nullchoice +
		    //		       ": " + order[i] +
		    //		       " act " + activity[i]);
                }
                return order[i];
            }
	    // System.err.println("*** select: lits " + lits + " order " +
	    //                    order +
	    //                    " order["+ i + "] = " + order[i] +
	    //                    " lits["+ order[i] + "] = " +
	    //                    lits.valueToString(order[i]));
        }
	// Satin debug:
	System.err.println("*** VarOrder.order.select(): UNDEFINED, lits " +
			   lits);
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

    /* (non-Javadoc)
     * @see org.sat4j.minisat.core.IOrder#undo(int)
     */
    public void undo(int x) {
        assert x > 0;
        assert x < order.length;
        int pos = varpos[x];
        if (pos < lastVar) {
            lastVar = pos;
        }
        assert lastVar > 0;
    }

    /* (non-Javadoc)
     * @see org.sat4j.minisat.core.IOrder#updateVar(int)
     */
    public void updateVar(int p) {
        assert p > 1;
        final int var = p >> 1;

        updateActivity(var);
        int i = varpos[var];
        for (; i > 1 // because there is nothing at i=0
                && (activity[order[i - 1] >> 1] < activity[var]); i--) {
            assert i > 1;
            // echange p avec son predecesseur
            final int orderpm1 = order[i - 1];
            assert varpos[orderpm1 >> 1] == i - 1;
            varpos[orderpm1 >> 1] = i;
            order[i] = orderpm1;
        }
        assert i >= 1;
        varpos[var] = i;
        order[i] = p;

        if (i < lastVar) {
            lastVar = i;
        }
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

    /* (non-Javadoc)
     * @see org.sat4j.minisat.core.IOrder#init()
     */
    public void init() {
        int nlength = lits.nVars() + 1;
        int reallength = lits.realnVars()+1;
        int[] nvarpos = new int[nlength];
        double[] nactivity = new double[nlength];
        int[] norder = new int[reallength];
        nvarpos[0] = -1;
        nactivity[0] = -1;
        norder[0] = ILits.UNDEFINED;
        for (int i = 1,j=1; i < nlength; i++) {
            assert i > 0;
            assert i <= lits.nVars() : "" + lits.nVars() + "/" + i;
            if (lits.belongsToPool(i)) {
               norder[j] = lits.getFromPool(i) ^ 1; // Looks a
               // promising
               // approach
               nvarpos[i] = j++;
            }
            nactivity[i] = 0.0;
        }
        varpos = nvarpos;
        activity = nactivity;
        order = norder;
        lastVar = 1;
    }

    @Override
    public Object clone() {
	VarOrder clone;

	try {
	    clone = (VarOrder) super.clone();
	}
	catch (CloneNotSupportedException e) {
	    throw new InternalError(e.toString());
	}

	clone.activity = this.activity.clone();
	clone.order    = this.order.clone();
	clone.varpos   = this.varpos.clone();
	// lits is set by setLits() after cloning

	return clone;
    }

    /**
     * Affiche les litteraux dans l'ordre de l'heuristique, la valeur de
     * l'activite entre ().
     * 
     * @return les litteraux dans l'ordre courant.
     */
    @Override
    public String toString() {
        return "VSIDS like heuristics from MiniSAT using a sorted array";
    }

    public ILits getVocabulary() {
        return lits;
    }

    /* (non-Javadoc)
     * @see org.sat4j.minisat.core.IOrder#printStat(java.io.PrintStream, java.lang.String)
     */
    public void printStat(PrintStream out, String prefix) {
        out.println(prefix + "non guided choices\t" + nullchoice);
        out.println(prefix + "random choices\t" + randchoice);
    }

}

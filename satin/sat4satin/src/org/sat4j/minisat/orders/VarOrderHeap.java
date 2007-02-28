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

package org.sat4j.minisat.orders;

import java.io.PrintWriter;
import java.io.Serializable;

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
public class VarOrderHeap implements IOrder, Serializable {

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

    protected Heap heap;

    protected int[] phase;

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
     * @param p
     *            a literal
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

    // Added for SATIN:
    public void addActivity(int p, double val) {
        int var = p >> 1;
        activity[var] += val; // Don't rescale at this point
        // Also don't update phase[var]
        if (heap.inHeap(var))
            heap.increase(var);
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
            assert i <= lits.nVars() : "" + lits.nVars() + "/" + i; //$NON-NLS-1$ //$NON-NLS-2$
            activity[i] = 0.0;
            if (lits.belongsToPool(i)) {
                heap.insert(i);
                phase[i] = (i << 1) ^ 1;
            } else {
                phase[i] = ILits.UNDEFINED;
            }
        }
    }

    @Override
    public String toString() {
        return "VSIDS like heuristics from MiniSAT using a heap"; //$NON-NLS-1$
    }

    public ILits getVocabulary() {
        return lits;
    }

    public void printStat(PrintWriter out, String prefix) {
        out.println(prefix + "non guided choices\t" + nullchoice); //$NON-NLS-1$
    }
}

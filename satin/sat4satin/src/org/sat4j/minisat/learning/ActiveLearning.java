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

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.IOrder;
import org.sat4j.minisat.core.Solver;

/**
 * Learn clauses with a great number of active variables.
 * 
 * @author leberre
 */
public class ActiveLearning extends LimitedLearning {

    private static final long serialVersionUID = 1L;

    private double percent;

    private IOrder order;

    public ActiveLearning() {
        this(0.95);
    }

    public ActiveLearning(double d) {
        this.percent = d;
    }

    public void setOrder(IOrder order) {
        this.order = order;
    }

    @Override
    public void setSolver(Solver s) {
        super.setSolver(s);
        this.order = s.getOrder();
    }

    public void setActivityPercent(double d) {
        percent = d;
    }

    public double getActivityPercent() {
        return percent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.minisat.LimitedLearning#learningCondition(org.sat4j.minisat.Constr)
     */
    @Override
    protected boolean learningCondition(Constr clause) {
        int nbactivevars = 0;
        for (int i = 0; i < clause.size(); i++) {
            if (order.varActivity(clause.get(i)) > 1) {
                nbactivevars++;
            }
        }
        return nbactivevars > clause.size() * percent;
    }

    @Override
    public String toString() {
        return "Limit learning to clauses containing active literals ("+percent*100+"%)"; //$NON-NLS-1$
    }
}

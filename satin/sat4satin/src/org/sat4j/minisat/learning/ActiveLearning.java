/*
 * Created on 1 juin 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.learning;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.IOrder;

/**
 * Learn clauses with a great number of active variables.
 * 
 * @author leberre
 */
public class ActiveLearning extends LimitedLearning {

    private static final long serialVersionUID = 1L;

    private final double percent;

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
        return "Limit learning to clauses containing active literals";
    }
}

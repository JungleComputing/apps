package org.sat4j.minisat.learning;

import java.io.Serializable;

import org.sat4j.minisat.core.Constr;
import org.sat4j.minisat.core.LearningStrategy;
import org.sat4j.minisat.core.VarActivityListener;

abstract class AbstractLearning implements LearningStrategy, Serializable,
        Cloneable {

    private VarActivityListener val;

    public void init() {
        // TODO Auto-generated method stub

    }

    public void learns(Constr constr) {
        // TODO Auto-generated method stub

    }

    public void setVarActivityListener(VarActivityListener s) {
        this.val = s;
    }

    public final void claBumpActivity(Constr reason) {
        for (int i = 0; i < reason.size(); i++) {
            int q = reason.get(i);
            assert q > 1;
            val.varBumpActivity(q);
        }
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
}

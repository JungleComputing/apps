package org.sat4j.minisat.core;

import java.io.Serializable;
import java.util.Comparator;
import org.sat4j.specs.IConstr;

public class ActivityComparator implements Comparator<Constr>, Serializable {

    private static final long serialVersionUID = 1L;

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Constr c1, Constr c2) {
        return (int) Math.round(c1.getActivity() - c2.getActivity());
    }
}

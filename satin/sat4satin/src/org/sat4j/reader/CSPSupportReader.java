package org.sat4j.reader;
import java.util.HashMap;
import java.util.Map;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

class CSPSupportReader extends CSPReader {
    
    public CSPSupportReader(ISolver solver) {
        super(solver);
    }

    @Override
    protected void manageAllowedTuples(int relnum, int[] domains, int nbtuples) {
        if (domains.length==2) {
            relations[relnum] = new SupportAllowedRelation(domains, nbtuples);
        } else {
            relations[relnum] = new AllowedRelation(domains, nbtuples);
        }
    }
}

class SupportAllowedRelation extends AllowedRelation {
    SupportAllowedRelation(int[] domains, int nbtuples) {
        super(domains, nbtuples);
        if (domains.length!=2) {
            throw new UnsupportedOperationException("Works only for binary constraints");
        }
    }

    @Override
    public void toClause(ISolver solver, Var[] vars) throws ContradictionException {
        Map<Integer,IVecInt> supportsa = new HashMap<Integer,IVecInt>();
        Map<Integer,IVecInt> supportsb = new HashMap<Integer,IVecInt>();
 
        for (int i = 0; i < tuples.length; i++) {
            assert domains.length==2;
            int va = tuples[i][0];
            int vb = tuples[i][1];
            addSupport(va,vars[1],vb,supportsa);
            addSupport(vb,vars[0],va,supportsb);
        }
                
        generateClauses(vars[0],supportsa,solver);
        generateClauses(vars[1],supportsb,solver);
    }
    
    private void addSupport(int head, Var v, int value, Map<Integer,IVecInt> supports) {
        IVecInt sup = supports.get(head);
        if (sup==null) {
            sup = new VecInt();
            supports.put(head,sup);
        }
        sup.push(v.translate(value));
    }
    
    private void generateClauses(Var v, Map<Integer, IVecInt> supports, ISolver solver) throws ContradictionException {
        IVecInt clause = new VecInt();
        for (Map.Entry<Integer,IVecInt> entry : supports.entrySet()) {
            clause.clear();
            clause.push(-v.translate(entry.getKey()));
            for (int i : entry.getValue()) {
                clause.push(i);
            }
            solver.addClause(clause);
        }
    }
}
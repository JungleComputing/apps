package geneSequencing.divideAndConquer;

import geneSequencing.Dsearch;
import geneSequencing.ResSeq;
import geneSequencing.WorkUnit;

import java.util.ArrayList;

public class DivCon extends ibis.satin.SatinObject implements DivConInterface {

    public DivCon() {
    }

    @SuppressWarnings("unchecked")
    public ArrayList<ResSeq> spawn_splitSequences(WorkUnit workUnit) {
        int querySize = workUnit.querySequences.size();
        int databaseSize = workUnit.databaseSequences.size();
        int size = querySize > databaseSize ? querySize : databaseSize;
        if (size <= workUnit.threshold) {
            return Dsearch.createTrivialResult(workUnit);
        }

        ArrayList<ResSeq>[] sub;

        // if (querySize > databaseSize) {
        // No, just first split the database. Database entries are larger,
        // so just looking at the number of entries does not work.
        if (databaseSize <= workUnit.threshold) {
            int newSplitSize = querySize / 2;
            sub = (ArrayList<ResSeq>[])(new ArrayList[2]);
            sub[0] = spawn_splitSequences(workUnit.splitQuerySequences(0,
                        newSplitSize));
            sub[1] = spawn_splitSequences(workUnit.splitQuerySequences(
                        newSplitSize, querySize));
        } else {
            int split;
            /*
            if (databaseSize >= 8 * workUnit.threshold) {
                split = 8;
            } else if (databaseSize >= 4 * workUnit.threshold) {
                split = 4;
            } else {
            */
                split = 2;
            /*
            }
            */
            sub = (ArrayList<ResSeq>[])(new ArrayList[split]);
            int newSplitSize = databaseSize / split;
            for (int i = 0; i < split; i++) {
                int start = i * newSplitSize;
                int end = (i == split - 1)
                        ? databaseSize : (start + newSplitSize);
                sub[i] = spawn_splitSequences(
                        workUnit.splitDatabaseSequences(start, end));
            }
        }
        sync();
        return Dsearch.combineSubResults(sub);
    }
}

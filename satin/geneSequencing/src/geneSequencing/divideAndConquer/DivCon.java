package geneSequencing.divideAndConquer;

import geneSequencing.ResSeq;

import java.util.ArrayList;

public class DivCon extends ibis.satin.SatinObject implements DivConInterface {

    public DivCon() {
    }

    public ArrayList<ResSeq> spawn_splitQuerySequences(WorkUnit workUnit) {
        ArrayList<ResSeq> result;

        if (workUnit.querySequences.size() <= workUnit.threshold) {
            result = spawn_splitDatabaseSequences(workUnit);
            sync();
        } else {
            int newSplitSize = workUnit.querySequences.size() / 2;

            ArrayList<ResSeq> subRes1 =
                    spawn_splitQuerySequences(workUnit.splitQuerySequences(0,
                            newSplitSize));
            ArrayList<ResSeq> subRes2 =
                    spawn_splitQuerySequences(workUnit.splitQuerySequences(
                            newSplitSize, workUnit.querySequences.size()));

            sync();

            result = DsearchDC.combineSubResults(subRes1, subRes2);
        }

        return result;
    }

    public ArrayList<ResSeq> spawn_splitDatabaseSequences(WorkUnit workUnit) {
        ArrayList<ResSeq> result;
        int newSplitSize;

        if (workUnit.databaseSequences.size() <= workUnit.threshold) {
            result = DsearchDC.createTrivialResult(workUnit);
        } else {
            newSplitSize = workUnit.databaseSequences.size() / 2;

            ArrayList<ResSeq> subResult1 =
                    spawn_splitDatabaseSequences(workUnit
                            .splitDatabaseSequences(0, newSplitSize));
            ArrayList<ResSeq> subResult2 =
                    spawn_splitDatabaseSequences(workUnit
                            .splitDatabaseSequences(newSplitSize,
                                    workUnit.databaseSequences.size()));

            sync();

            result = DsearchDC.combineSubResults(subResult1, subResult2);
        }

        return result;
    }
}

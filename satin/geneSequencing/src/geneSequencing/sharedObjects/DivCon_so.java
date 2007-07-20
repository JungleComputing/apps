package geneSequencing.sharedObjects;

import geneSequencing.Dsearch;
import geneSequencing.ResSeq;
import geneSequencing.WorkUnit;

import java.util.ArrayList;

public class DivCon_so extends ibis.satin.SatinObject implements
        DivCon_soInterface {

    public DivCon_so() {
    }

    public ArrayList<ResSeq> spawn_splitQuerySequences(WorkUnit workUnit,
            SharedData sharedData) {
        ArrayList<ResSeq> result;

        if (workUnit.querySequences.size() <= workUnit.threshold) {
            result = spawn_splitDatabaseSequences(workUnit, sharedData);
            sync();
        } else {
            int newSplitSize = workUnit.querySequences.size() / 2;

            ArrayList<ResSeq> subRes1 =
                    spawn_splitQuerySequences(workUnit.splitQuerySequences(0,
                            newSplitSize), sharedData);
            ArrayList<ResSeq> subRes2 =
                    spawn_splitQuerySequences(workUnit.splitQuerySequences(
                            newSplitSize, workUnit.querySequences.size()),
                            sharedData);

            sync();

            result = Dsearch.combineSubResults(subRes1, subRes2);
        }

        return result;
    }

    public ArrayList<ResSeq> spawn_splitDatabaseSequences(WorkUnit workUnit,
            SharedData sharedData) {
        ArrayList<ResSeq> result;
        int newSplitSize;

        if (workUnit.databaseSequences.size() <= workUnit.threshold) {
            result = Dsearch.createTrivialResult(workUnit);
        } else {
            newSplitSize = workUnit.databaseSequences.size() / 2;

            ArrayList<ResSeq> subResult1 =
                    spawn_splitDatabaseSequences(workUnit
                            .splitDatabaseSequences(0, newSplitSize),
                            sharedData);
            ArrayList<ResSeq> subResult2 =
                    spawn_splitDatabaseSequences(workUnit
                            .splitDatabaseSequences(newSplitSize,
                                    workUnit.databaseSequences.size()),
                            sharedData);

            sync();

            result = Dsearch.combineSubResults(subResult1, subResult2);
        }

        return result;
    }
}

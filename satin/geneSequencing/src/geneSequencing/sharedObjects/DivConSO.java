package geneSequencing.sharedObjects;

import geneSequencing.Dsearch;
import geneSequencing.ResSeq;
import geneSequencing.WorkUnit;

import java.util.ArrayList;

public class DivConSO extends ibis.satin.SatinObject implements
        DivConSOInterface {

    public DivConSO() {
    }

    public ArrayList<ResSeq> spawn_splitQuerySequences(WorkUnit workUnit,
        SharedData sharedData, int startQuery, int endQuery, int startDatabase,
        int EndDatabase) {
        
        ArrayList<ResSeq> result;

        if (endQuery - startQuery <= workUnit.threshold) {
            result = spawn_splitDatabaseSequences(workUnit, sharedData,
                startQuery, endQuery, startDatabase, EndDatabase);
            sync();
        } else {
            int newSplitSize = (endQuery - startQuery) / 2;
            
            ArrayList<ResSeq> subRes1 = spawn_splitQuerySequences(workUnit,
                sharedData, startQuery, startQuery + newSplitSize,
                startDatabase, EndDatabase);

            ArrayList<ResSeq> subRes2 = spawn_splitQuerySequences(workUnit,
                sharedData, startQuery + newSplitSize, endQuery, startDatabase, EndDatabase);

            sync();

            result = Dsearch.combineSubResults(subRes1, subRes2);
        }

        return result;
    }

    public ArrayList<ResSeq> spawn_splitDatabaseSequences(WorkUnit workUnit,
        SharedData sharedData, int startQuery, int endQuery, int startDatabase,
        int EndDatabase) {
        ArrayList<ResSeq> result;

        if (EndDatabase - startDatabase <= workUnit.threshold) {
            result = Dsearch.createTrivialResult(workUnit, sharedData,
                startQuery, endQuery, startDatabase, EndDatabase);
        } else {
            int newSplitSize = (EndDatabase - startDatabase) / 2;

            ArrayList<ResSeq> subResult1 = spawn_splitDatabaseSequences(
                workUnit, sharedData, startQuery, endQuery, startDatabase,
                startDatabase + newSplitSize);

            ArrayList<ResSeq> subResult2 = spawn_splitDatabaseSequences(
                workUnit, sharedData, startQuery, endQuery, startDatabase
                    + newSplitSize, EndDatabase);

            sync();

            result = Dsearch.combineSubResults(subResult1, subResult2);
        }

        return result;
    }
}

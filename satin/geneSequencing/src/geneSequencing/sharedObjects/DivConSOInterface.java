package geneSequencing.sharedObjects;

import geneSequencing.ResSeq;
import geneSequencing.WorkUnit;

import java.util.ArrayList;

public interface DivConSOInterface extends ibis.satin.Spawnable {
    public ArrayList<ResSeq> spawn_splitQuerySequences(WorkUnit workUnit,
        SharedData sharedData, int startQuery, int endQuery, int startDatabase,
        int EndDatabase);

    public ArrayList<ResSeq> spawn_splitDatabaseSequences(WorkUnit workUnit,
        SharedData sharedData, int startQuery, int endQuery, int startDatabase,
        int EndDatabase);
}

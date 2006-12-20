package geneSequencing.divideAndConquer;

import geneSequencing.ResSeq;

import java.util.ArrayList;

public interface DivConInterface extends ibis.satin.Spawnable {
    public ArrayList<ResSeq> spawn_splitQuerySequences(WorkUnit workUnit);

    public ArrayList<ResSeq> spawn_splitDatabaseSequences(WorkUnit workUnit);
}

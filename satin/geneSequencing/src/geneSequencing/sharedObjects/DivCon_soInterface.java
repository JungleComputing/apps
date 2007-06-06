package geneSequencing.sharedObjects;

import geneSequencing.ResSeq;
import geneSequencing.divideAndConquer.WorkUnit;

import java.util.ArrayList;

public interface DivCon_soInterface extends ibis.satin.Spawnable
{
    public ArrayList<ResSeq> spawn_splitQuerySequences(WorkUnit workUnit, SharedData sharedData);
    public ArrayList<ResSeq> spawn_splitDatabaseSequences(WorkUnit workUnit, SharedData sharedData);
}
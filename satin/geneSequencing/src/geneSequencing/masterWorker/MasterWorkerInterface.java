package geneSequencing.masterWorker;

import geneSequencing.ResSeq;
import geneSequencing.WorkUnit;

import java.util.ArrayList;

public interface MasterWorkerInterface extends ibis.satin.Spawnable {
    public ArrayList<ResSeq> spawn_createResultUnit(WorkUnit workUnit);
}

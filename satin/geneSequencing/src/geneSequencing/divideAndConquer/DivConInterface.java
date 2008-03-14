package geneSequencing.divideAndConquer;

import geneSequencing.ResSeq;
import geneSequencing.WorkUnit;

import java.util.ArrayList;

public interface DivConInterface extends ibis.satin.Spawnable {
    public ArrayList<ResSeq> spawn_splitSequences(WorkUnit workUnit);
}

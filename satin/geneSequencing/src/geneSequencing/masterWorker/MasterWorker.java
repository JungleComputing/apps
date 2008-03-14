package geneSequencing.masterWorker;

import geneSequencing.Dsearch;
import geneSequencing.ResSeq;
import geneSequencing.WorkUnit;

import java.util.ArrayList;

public class MasterWorker extends ibis.satin.SatinObject implements
        MasterWorkerInterface {

    public MasterWorker() {
    }

    public ArrayList<ResSeq> generateResult(WorkUnit workUnit) {
        ArrayList<WorkUnit> workUnits;
        ArrayList<ResSeq>[] resultUnits;

        workUnits = workUnit.generateWorkUnits(workUnit.threshold);
        System.out.println("work units         = " + workUnits.size());

        resultUnits = generateResultUnits(workUnits);

        return Dsearch.combineSubResults(resultUnits);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<ResSeq>[] generateResultUnits(
            ArrayList<WorkUnit> workUnits) {
        ArrayList<ResSeq> resultUnitsArray[]
                = (ArrayList<ResSeq>[]) (new ArrayList[workUnits.size()]);

        for (int i = 0; i < resultUnitsArray.length; i++) {
            resultUnitsArray[i] = spawn_createResultUnit(workUnits.get(i));
        }

        sync();

        return resultUnitsArray;
    }

    public ArrayList<ResSeq> spawn_createResultUnit(WorkUnit workUnit) {
        return Dsearch.createTrivialResult(workUnit);
    }
}

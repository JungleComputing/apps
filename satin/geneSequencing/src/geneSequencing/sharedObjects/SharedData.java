package geneSequencing.sharedObjects;

import geneSequencing.Sequence;

import java.util.ArrayList;

public class SharedData extends ibis.satin.SharedObject implements
        SharedDataInterface {

    private ArrayList<Sequence> querySequences;

    private ArrayList<Sequence> databaseSequences;

    public SharedData(ArrayList<Sequence> querySequences, ArrayList<Sequence> databaseSequences) {
        this.querySequences = querySequences;
        this.databaseSequences = databaseSequences;
    }

    /**
     * @return the databaseSequences
     */
    public ArrayList<Sequence> getDatabaseSequences() {
        return databaseSequences;
    }

    /**
     * @return the querySequences
     */
    public ArrayList<Sequence> getQuerySequences() {
        return querySequences;
    }
}
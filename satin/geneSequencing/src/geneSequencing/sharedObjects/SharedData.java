package geneSequencing.sharedObjects;

import geneSequencing.FileSequences;
import geneSequencing.Sequence;

import java.util.Vector;

public class SharedData extends ibis.satin.SharedObject implements
        SharedDataInterface {
    private FileSequences querySequences;

    private FileSequences databaseSequences;

    public SharedData(FileSequences querySequences, FileSequences databaseSequences) {
        this.querySequences = querySequences;
        this.databaseSequences = databaseSequences;
    }

    public Vector getQuerySeqs(Vector seqsPointers) {
        Vector qS = new Vector();

        for (int i = 0; i < seqsPointers.size(); i++) {
            int position = (Integer) seqsPointers.get(i);
            Sequence currentSeq =
                    (Sequence) querySequences.getSequences().get(position);

            qS.add(currentSeq);
        }

        return qS;
    }

    public Vector getDatabaseSeqs(Vector seqsPointers) {
        Vector dbS = new Vector();

        for (int i = 0; i < seqsPointers.size(); i++) {
            int position = (Integer) seqsPointers.get(i);
            Sequence currentSeq =
                    (Sequence) databaseSequences.getSequences().get(position);

            dbS.add(currentSeq);
        }

        return dbS;
    }
}
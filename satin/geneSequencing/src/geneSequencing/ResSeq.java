package geneSequencing;

import java.io.Serializable;
import java.util.TreeSet;

public class ResSeq implements Serializable {
    private int maxScores;

    private Sequence querySequence;

    private TreeSet<Sequence> databaseSequences;
    
    public ResSeq(Sequence querySequence, int maxScores) {
        this.querySequence = querySequence;
        this.maxScores = maxScores;
        databaseSequences = new TreeSet<Sequence>();
    }

    public void updateDatabaseSequences(TreeSet<Sequence> newDatabaseSequences) {
        for (Sequence sequence : newDatabaseSequences) {
            databaseSequences.add(sequence);
            databaseSequences.remove(databaseSequences.last());
        }
    }

    public void processDatabaseSeqs() {
        while (databaseSequences.size() > maxScores) {
            databaseSequences.remove(databaseSequences.last());
        }
    }

    public String toString() {
        String str = "";
        str = str + querySequence.getSequenceName() + "\n\n";

        int i=1;
        for (Sequence sequence : databaseSequences) {
            int sequenceScore = sequence.getSequenceScore();
            String sequenceName = sequence.getSequenceName();
            String sequenceAlignment = sequence.getSequenceAlignment();

            if (sequenceAlignment.equals("not calculated")) {
                str = str + "   " + "[" + i + "] : " + sequenceScore
                    + " : " + sequenceName + "\n";
            } else {
                str = str + "   " + "[" + i + "] : " + sequenceName
                    + "\n";
                str = str + "\n" + sequenceAlignment + "\n\n";
            }
            i++;
        }

        return str;
    }

    public Sequence getQuerySequence() {
        return querySequence;
    }

    public TreeSet<Sequence> getDatabaseSequences() {
        return databaseSequences;
    }

    public void addDatabaseSequences(Sequence databaseSequence) {
        databaseSequences.add(new Sequence(databaseSequence)); // this copy is needed for correctness
    }
}

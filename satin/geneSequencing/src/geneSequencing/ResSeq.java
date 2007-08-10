package geneSequencing;

import java.io.Serializable;
import java.util.ArrayList;

public class ResSeq implements Serializable {
    private int maxScores;

    private Sequence querySequence;

    private ArrayList<Sequence> databaseSequences;

    public ResSeq() {
        querySequence = new Sequence();
        databaseSequences = new ArrayList<Sequence>();
    }

    public void updateDatabaseSequences(ArrayList<Sequence> newDatabaseSequences) {
        databaseSequences.addAll(newDatabaseSequences);
        processDatabaseSeqs();
    }

    private void getMaximumElements() {
        while (databaseSequences.size() > maxScores) {
            databaseSequences.remove(databaseSequences.size() - 1);
        }
    }

    private void sortDatabaseSequences() {
        for (int i = 0; i < databaseSequences.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (compareSeqs(databaseSequences.get(i), databaseSequences
                    .get(j)) > 0) {
                    Sequence elementI = databaseSequences.get(i);
                    Sequence elementJ = databaseSequences.get(j);

                    databaseSequences.set(j, elementI);
                    databaseSequences.set(i, elementJ);
                }
            }
        }
    }

    public void processDatabaseSeqs() {
        sortDatabaseSequences();
        getMaximumElements();
    }

    private int compareSeqs(Sequence i, Sequence j) {
        int first_i_score = i.getSequenceScore();
        int first_j_score = j.getSequenceScore();

        if (first_i_score > first_j_score) return 1;

        return first_i_score != first_j_score ? -1 : 0;
    }

    public String toString() {
        String str = new String();
        str = str + querySequence.getSequenceName() + "\n\n";

        for (int i = 0; i < databaseSequences.size(); i++) {
            Sequence sequence = databaseSequences.get(i);
            int sequenceScore = sequence.getSequenceScore();
            String sequenceName = sequence.getSequenceName();
            String sequenceAlignment = sequence.getSequenceAlignment();

            if (sequenceAlignment.equals("not calculated")) {
                str = str + "   " + "[" + (i + 1) + "] : " + sequenceScore
                    + " : " + sequenceName + "\n";
            } else {
                str = str + "   " + "[" + (i + 1) + "] : " + sequenceName
                    + "\n";
                str = str + "\n" + sequenceAlignment + "\n\n";
            }
        }

        return str;
    }

    public Sequence getQuerySequence() {
        return querySequence;
    }

    public ArrayList<Sequence> getDatabaseSequences() {
        return databaseSequences;
    }

    public void addDatabaseSequences(Sequence databaseSequence) {
        databaseSequences.add(new Sequence(databaseSequence)); // this copy is needed for correctness
    }

    public void setQuerySequence(Sequence querySequence) {
        this.querySequence = querySequence;
    }

    public void setMaximumScores(int maxScores) {
        this.maxScores = maxScores;
    }
}

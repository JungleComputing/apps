//package dsearchDC;

import java.util.Vector;
import java.io.Serializable;

public class ResSeq implements Serializable {
    private int maxScores;

    private Sequence querySequence;

    private Vector databaseSequences;

    public ResSeq() {
        querySequence = new Sequence();
        databaseSequences = new Vector();
        this.maxScores = 0;
    }

    public void updateDatabaseSequences(Vector newDatabaseSequences) {
        for (int i = 0; i < newDatabaseSequences.size(); i++) {
            Sequence sequence = (Sequence) newDatabaseSequences.get(i);
            databaseSequences.add(sequence);
        }

        sortDatabaseSequnces();
        getMaximumElements();
    }

    private void getMaximumElements() {
        if (databaseSequences.size() >= maxScores) {
            Vector newDatabaseSequences = new Vector();

            for (int i = 0; i < maxScores; i++) {
                newDatabaseSequences.add(databaseSequences.get(i));
            }

            databaseSequences = newDatabaseSequences;
        }
    }

    private void sortDatabaseSequnces() {
        for (int i = 0; i < databaseSequences.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (compareSeqs((Sequence) databaseSequences.get(i),
                        (Sequence) databaseSequences.get(j)) > 0) {
                    Sequence elementI = (Sequence) databaseSequences.get(i);
                    Sequence elementJ = (Sequence) databaseSequences.get(j);

                    databaseSequences.setElementAt(elementI, j);
                    databaseSequences.setElementAt(elementJ, i);
                }
            }
        }
    }

    public void processDatabaseSeqs() {
        sortDatabaseSequnces();
        getMaximumElements();
    }

    private int compareSeqs(Sequence i, Sequence j) {
        int first_i_score = i.getSequenceScore();
        int first_j_score = j.getSequenceScore();

        if (first_i_score > first_j_score)
            return 1;

        return first_i_score != first_j_score ? -1 : 0;
    }

    public String toString() {
        String str = new String();
        str = str + querySequence.getSequenceName() + "\n\n";

        for (int i = 0; i < databaseSequences.size(); i++) {
            Sequence sequence = (Sequence) databaseSequences.get(i);
            int sequenceScore = sequence.getSequenceScore();
            String sequenceName = sequence.getSequenceName();
            String sequenceAlignment = sequence.getSequenceAlignment();

            if (sequenceAlignment.equals("not calculated"))
                str =
                        str + "   " + "[" + (i + 1) + "] : " + sequenceScore
                                + " : " + sequenceName + "\n";
            else {
                str =
                        str + "   " + "[" + (i + 1) + "] : " + sequenceName
                                + "\n";
                str = str + "\n" + sequenceAlignment + "\n\n";
            }
        }

        return str;
    }

    public Sequence getQuerySequence() {
        return querySequence;
    }

    public Vector getDatabaseSequences() {
        return databaseSequences;
    }

    public void addDatabaseSequences(Sequence databaseSequence) {
        databaseSequences.add(new Sequence(databaseSequence));
    }

    public void setQuerySequence(Sequence querySequence) {
        this.querySequence = new Sequence(querySequence);
    }

    public void setMaximumScores(int maxScores) {
        this.maxScores = maxScores;
    }
}

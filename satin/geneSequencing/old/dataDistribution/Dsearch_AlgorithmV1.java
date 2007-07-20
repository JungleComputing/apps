//package dsearchDC;

import java.io.CharArrayReader;
import java.util.ArrayList;

public class Dsearch_AlgorithmV1 {
    public final int ALIGN_GET_SCORES = 1;

    public final int ALIGN_GET_ALIGNMENTS = 2;

    public ArrayList<ResSeq> processUnit(WorkUnit workUnit) throws Throwable {
        ArrayList<ResSeq> results = new ArrayList<ResSeq>();

        for (int i = 0; i < workUnit.querySequences.size(); i++) {
            Sequence querySequence = (Sequence) workUnit.querySequences.get(i);
            String querySequenceBody = querySequence.createSequenceBody();

            char[] currentQuerySeq = querySequenceBody.toCharArray();

            //create an array to record the scores
            ResSeq resSeq = new ResSeq();
            resSeq.setQuerySequence(querySequence); //add the name of the current query sequence

            for (int j = 0; j < workUnit.databaseSequences.size(); j++) {
                Sequence databaseSequence =
                        (Sequence) workUnit.databaseSequences.get(j);
                String databaseSequenceBody =
                        databaseSequence.createSequenceBody();

                char[] currentDatabaseSeq = databaseSequenceBody.toCharArray();

                CharArrayReader qSequence =
                        new CharArrayReader(currentQuerySeq);
                CharArrayReader dbSequence =
                        new CharArrayReader(currentDatabaseSeq);

                int score = 0;
                String alignment = "not calculated";

                try {
                    if (workUnit.scoresOrAlignments == ALIGN_GET_ALIGNMENTS) {
                        alignment =
                                Sequence_Aligner.computeAlignment(
                                        workUnit.alignmentAlgorithm, qSequence,
                                        dbSequence, workUnit.scoringScheme);
                        score = getScore(alignment);
                    } else {
                        score =
                                Sequence_Aligner.computeAlignmentScore(
                                        workUnit.alignmentAlgorithm, qSequence,
                                        dbSequence, workUnit.scoringScheme);
                    }
                } catch (Exception e) {
                    System.out.println("My Exception in processUnit: "
                            + e.toString());
                }

                if (score > 0) {
                    databaseSequence.setSequenceScore(score);
                    databaseSequence.setSequenceAlignment(alignment);

                    resSeq.addDatabaseSequences(databaseSequence);
                }
            }
            results.add(resSeq);
        }
        return results;
    }

    private int getScore(String scoreF) {
        int score;

        String[] splitScoreS = scoreF.split("\n");
        String[] split3element = (splitScoreS[3]).split(":");
        String x = split3element[1].substring(1);

        score = Integer.parseInt(x);

        return score;
    }
}

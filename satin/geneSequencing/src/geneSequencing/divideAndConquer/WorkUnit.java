package geneSequencing.divideAndConquer;

import java.util.Vector;

import neobio.alignment.ScoringScheme;

/*
 * Created on Nov 17, 2006
 */

public class WorkUnit implements java.io.Serializable {
    public String alignmentAlgorithm;

    public int scoresOrAlignments;

    public ScoringScheme scoringScheme;

    public Vector querySequences;

    public Vector databaseSequences;

    public int maxScores;

    public int threshold;

    public WorkUnit(String alignmentAlgorithm, int scoresOrAlignments,
            ScoringScheme scoringScheme, Vector querySequences,
            Vector databaseSequences, int maxScores, int threshold) {
        super();
        this.alignmentAlgorithm = alignmentAlgorithm;
        this.scoresOrAlignments = scoresOrAlignments;
        this.scoringScheme = scoringScheme;
        this.querySequences = querySequences;
        this.databaseSequences = databaseSequences;
        this.maxScores = maxScores;
        this.threshold = threshold;
    }

    public WorkUnit splitQuerySequences(int begin, int end) {
        Vector newQuerySequences = new Vector();

        for (int i = begin; i < end; i++)
            newQuerySequences.add(querySequences.get(i));

        return new WorkUnit(alignmentAlgorithm, scoresOrAlignments,
                scoringScheme, newQuerySequences, databaseSequences, maxScores,
                threshold);
    }

    public WorkUnit splitDatabaseSequences(int begin, int end) {
        Vector newDatabaseSequences = new Vector();

        for (int i = begin; i < end; i++)
            newDatabaseSequences.add(databaseSequences.get(i));

        return new WorkUnit(alignmentAlgorithm, scoresOrAlignments,
                scoringScheme, querySequences, newDatabaseSequences, maxScores,
                threshold);
    }
}

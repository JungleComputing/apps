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

    public WorkUnit(String alignmentAlgorithm, int scoresOrAlignments, ScoringScheme scoringScheme, Vector querySequences, Vector databaseSequences) {
        super();
        this.alignmentAlgorithm = alignmentAlgorithm;
        this.scoresOrAlignments = scoresOrAlignments;
        this.scoringScheme = scoringScheme;
        this.querySequences = querySequences;
        this.databaseSequences = databaseSequences;
    }
}

package geneSequencing;

import java.util.ArrayList;

import neobio.alignment.ScoringScheme;

/*
 * Created on Nov 17, 2006
 */

public class WorkUnit implements java.io.Serializable {
    public String alignmentAlgorithm;

    public int scoresOrAlignments;

    public ScoringScheme scoringScheme;

    public ArrayList<Sequence> querySequences;

    public ArrayList<Sequence> databaseSequences;

    public int maxScores;

    public int threshold;

    public WorkUnit(String alignmentAlgorithm, int scoresOrAlignments,
            ScoringScheme scoringScheme, ArrayList<Sequence> querySequences,
            ArrayList<Sequence> databaseSequences, int maxScores, int threshold) {
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
        ArrayList<Sequence> newQuerySequences = new ArrayList<Sequence>();

        for (int i = begin; i < end; i++) {
            newQuerySequences.add(querySequences.get(i));
        }
        return new WorkUnit(alignmentAlgorithm, scoresOrAlignments,
                scoringScheme, newQuerySequences, databaseSequences, maxScores,
                threshold);
    }

    public WorkUnit splitDatabaseSequences(int begin, int end) {
        ArrayList<Sequence> newDatabaseSequences = new ArrayList<Sequence>();

        for (int i = begin; i < end; i++) {
            newDatabaseSequences.add(databaseSequences.get(i));
        }
        return new WorkUnit(alignmentAlgorithm, scoresOrAlignments,
                scoringScheme, querySequences, newDatabaseSequences, maxScores,
                threshold);
    }
    
    public ArrayList<WorkUnit> generateWorkUnits(int threshold) {
        ArrayList<WorkUnit> res = new ArrayList<WorkUnit>();
        
        int queryParts = querySequences.size() / threshold;
        if(querySequences.size() % threshold > 0) {
            queryParts++;
        }
        
        int databaseParts = databaseSequences.size() / threshold;
        if(databaseSequences.size() % threshold > 0) {
            databaseParts++;
        }

        for (int i = 0; i < queryParts; i++) {
            for (int j = 0; j < databaseParts; j++) {
                ArrayList<Sequence> newQuerySequences = new ArrayList<Sequence>();
                ArrayList<Sequence> newDatabaseSequences = new ArrayList<Sequence>();

                for(int x=0;x<threshold;x++) {
                    int pos = i * threshold + x;
                    if(pos >= querySequences.size()) continue;
                    newQuerySequences.add(querySequences.get(pos));
                }

                for(int x=0;x<threshold;x++) {
                    int pos = j * threshold + x;
                    if(pos >= databaseSequences.size()) continue;
                    newDatabaseSequences.add(databaseSequences.get(pos));
                }
                
                res.add(new WorkUnit(alignmentAlgorithm, scoresOrAlignments,
                        scoringScheme, newQuerySequences, newDatabaseSequences, maxScores,
                        threshold));
            }
        }
        
        return res;
    }
    
    public String toString() {
        String res = "workunit: alg = " + alignmentAlgorithm
        + " sOrA = " + scoresOrAlignments
        + " scheme = " + scoringScheme
        + " maxScores = " + maxScores
        + " threshold = " + threshold
        + " queries = " + querySequences.size()
        + " databases = " + databaseSequences.size();
        return res;
   }
}
 
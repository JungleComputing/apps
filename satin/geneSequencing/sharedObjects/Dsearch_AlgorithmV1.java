//package dsearchDC_so;

import neobio.alignment.*;

import java.io.*;
import java.util.*;

public class Dsearch_AlgorithmV1
{
	public final int ALIGN_GET_SCORES = 1;
	public final int ALIGN_GET_ALIGNMENTS = 2;

	public Vector processUnit(Vector workUnit, SharedData sharedData) throws Throwable
	{
        Vector queryPointers    = (Vector)workUnit.get(3);
        Vector databasePointers = (Vector)workUnit.get(4);

        String alignmentAlgorithm = (String) workUnit.get(0);
		int scoresOrAlignments = ((Integer) workUnit.get(1)).intValue();
		ScoringScheme scoringScheme = (ScoringScheme) workUnit.get(2);

        Vector querySequences     = sharedData.getQuerySeqs(queryPointers);
        Vector databaseSequences = sharedData.getDatabaseSeqs(databasePointers);

		Vector results = new Vector();

        for(int i = 0; i < querySequences.size(); i++)
        {
            Sequence querySequence = (Sequence)querySequences.get(i);
            String querySequenceBody = querySequence.getSequenceBody();

            char[] currentQuerySeq = querySequenceBody.toCharArray();

            //create an array to record the scores
			ResSeq resSeq = new ResSeq();
			resSeq.setQuerySequence(querySequence); //add the name of the current query sequence

            for(int j = 0; j < databaseSequences.size(); j++)
            {
                Sequence databaseSequence = (Sequence) databaseSequences.get(j);
                String databaseSequenceBody = databaseSequence.getSequenceBody();

                char[] currentDatabaseSeq = databaseSequenceBody.toCharArray();

                CharArrayReader qSequence = new CharArrayReader(currentQuerySeq);
                CharArrayReader dbSequence = new CharArrayReader(currentDatabaseSeq);

                int score = 0;
                String alignment = "not calculated";

                try
                {
                    if(scoresOrAlignments == ALIGN_GET_ALIGNMENTS)
                    {
                        alignment = Sequence_Aligner.computeAlignment(alignmentAlgorithm, qSequence, dbSequence, scoringScheme);
                        score = getScore(alignment);
                    }
                    else
                    {
                        score = Sequence_Aligner.computeAlignmentScore(alignmentAlgorithm, qSequence, dbSequence, scoringScheme);
                    }
                }
				catch(Exception e)
                {
                    System.out.println("My Exception in processUnit: " + e.toString());
                }

                if( score > 0 )
                {
                    databaseSequence.setSequenceScore(score);
                    databaseSequence.setSequenceAlignment(alignment);

                    resSeq.addDatabaseSequences(databaseSequence);
                }
            }
			results.add(resSeq);
        }
        return results;
    }

    private int getScore(String scoreF)
    {
        int score;

        String[] splitScoreS   = scoreF.split("\n");
        String[] split3element = (splitScoreS[3]).split(":");
        String x  = split3element[1].substring(1);

        score = Integer.parseInt(x);

        return score;
    }    
}




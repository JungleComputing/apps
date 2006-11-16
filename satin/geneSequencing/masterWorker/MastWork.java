//package dsearchMW;

import neobio.alignment.ScoringScheme;
import java.io.*;
import java.util.Vector;

public class MastWork extends ibis.satin.SatinObject implements MastWorkInterface, java.io.Serializable
{
    private int threshold;

    private Vector theResult;
    private FileSequences querySequences;
    private FileSequences databaseSequences;

    private int    maxScores;
    private String alignmentAlgorithm;
    private int    scoresOrAlignments;
    private ScoringScheme scoringScheme;

    public MastWork()
    {
        theResult = new Vector();
    }

    private Vector generateResultUnits(Vector workUnits)
    {
        Vector resultUnitsArray[] = new Vector[workUnits.size()];

        for (int i = 0; i < workUnits.size(); i++)
        {
            try
            {
                resultUnitsArray[i]  = spawn_createResultUnit((Vector) workUnits.get(i));
            }
            catch (Throwable th)
            {
                System.out.println("Exception in generateResultUnits(): " + th.toString());
            }
        }

        sync();

        Vector resultUnits = new Vector();

        for(int j = 0; j < resultUnitsArray.length; j ++)
        {
            resultUnits.add(resultUnitsArray[j]);
        }

        return resultUnits;
    }

    public void generateTheResult()
    {
        Vector workUnits, resultUnits;

        workUnits = generateWorkUnits();
        System.out.println("work units         = " + workUnits.size());
        resultUnits = generateResultUnits(workUnits);

        processResultUnits(resultUnits);        
    }

    private void processResultUnits(Vector resultUnits)
    {
        for(int i = 0; i < resultUnits.size(); i++)
            theResult = getRes1Res2(theResult, (Vector)resultUnits.get(i));

    }
    private Vector getRes1Res2(Vector subResult1, Vector subResult2)
    {
        Vector main = subResult1;
        Vector additional = subResult2;

        for(int i = 0; i < additional.size(); i++)
            main = processSubResults((ResSeq)additional.get(i), main);

        Vector res = new Vector(main);

        return res;
    }

    private Vector processSubResults(ResSeq resSeq, Vector main)
    {
        boolean flag = false;
        for(int i = 0; i < main.size(); i++)
        {
            ResSeq resSeqMain = (ResSeq)main.get(i);

            String name     = resSeq.getQuerySequence().getSequenceName();
            String nameMain = resSeqMain.getQuerySequence().getSequenceName();

            if(nameMain.equals(name))
            {
                flag = true;
                Vector newDatabaseSeqs =  resSeq.getDatabaseSequences();
                resSeqMain.updateDatabaseSequences(newDatabaseSeqs);
            }
        }
        if(!flag)
            main.add(resSeq);

        return main;
    }

    private Vector generateWorkUnits()
    {
        Vector workUnits = new Vector();
        Vector querySeqs = querySequences.splitSequencesInParts(threshold);
        Vector databaseSeqs = databaseSequences.splitSequencesInParts(threshold);

        for(int i = 0; i < querySeqs.size(); i++)
        {
            Vector querySequencesPart = (Vector)querySeqs.get(i);

            for(int j = 0; j < databaseSeqs.size(); j++)
            {
                Vector databaseSequencesPart = (Vector)databaseSeqs.get(j);

                Vector workUnit = new Vector();
                workUnit.add(alignmentAlgorithm);
                workUnit.add(scoresOrAlignments);
                workUnit.add(scoringScheme);
                workUnit.add(querySequencesPart);
                workUnit.add(databaseSequencesPart);

                workUnits.add(workUnit);
            }
        }

        return workUnits;
    }

    public Vector getTheResult()
    {
        return theResult;
    }

    public Vector spawn_createResultUnit(Vector workUnit) throws Throwable
    {
        Dsearch_AlgorithmV1 dA = new Dsearch_AlgorithmV1();
        Vector resultUnit      = dA.processUnit(workUnit);

        Vector subResult = processResultUnit(resultUnit);

        return subResult;
    }

    private Vector processResultUnit(Vector resultUnit)
    {
        Vector subResult = new Vector();

        for(int i = 0; i < resultUnit.size(); i++)
        {
            ResSeq resSeq;

            resSeq = (ResSeq)resultUnit.get(i);
            resSeq.setMaximumScores(maxScores);
            resSeq.processDatabaseSeqs();

            subResult.add(resSeq);
        }

        return subResult;
    }

    public void setAlignmentAlgorithm(String alignmentAlgorithm)
    {
        this.alignmentAlgorithm = alignmentAlgorithm;
    }

    public void setScoresOrAlignments(int scoresOrAlignments)
    {
        this.scoresOrAlignments = scoresOrAlignments;
    }

    public void setScoringScheme(ScoringScheme scoringScheme)
    {
        this.scoringScheme = scoringScheme;
    }

    public void setQueryFile(String queryFile)
    {
        querySequences = new FileSequences(queryFile);
        querySequences.createFileSequnces();
    }

    public void setDatabaseFile(String databaseFile)
    {
        databaseSequences = new FileSequences(databaseFile);
        databaseSequences.createFileSequnces();
    }

    public void setMaximumScore(int maxScores)
    {
        this.maxScores = maxScores;
    }

    public void setThreshold(int threshold)
    {
        this.threshold = threshold;
    }

    public int getThreshold()
    {
        return threshold;
    }

    public Vector getQuerySequences()
    {
        return querySequences.getSequences();
    }

    public Vector getDatabaseSequences()
    {
        return databaseSequences.getSequences();
    }

    private String findHostname()
        {
            String cmd = "hostname ";
            String hostname = null;

            try
            {
                Process p = Runtime.getRuntime().exec(cmd);
                int ii = p.waitFor();

                if (ii == 0)
                {
                    BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    if((hostname = stdInput.readLine()) != null)
                    {
                        stdInput.close();
                        return hostname;

                    }
                }
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
            return "unnown";
        }

}
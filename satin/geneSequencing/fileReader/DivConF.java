//package dsearchDCf;

import neobio.alignment.ScoringScheme;
import java.util.Vector;
import java.io.*;

public class DivConF extends ibis.satin.SatinObject implements DivConInterface, Serializable
{
    private int threshold;

    private Vector theResult;

    private FileSequences querySequencesPointers;
    private FileSequences databaseSequencesPointers;

    private int    maxScores;
    private String alignmentAlgorithm;
    private int    scoresOrAlignments;
    private ScoringScheme scoringScheme;

    public DivConF()
    {
        theResult = new Vector();
    }

    public Vector spawn_splitQuerySequences(Vector workUnit)
    {
        Vector result;

        Vector querySequences  = (Vector)workUnit.get(3);

        if(querySequences.size() <= threshold)
        {
            result = spawn_splitDatabaseSequences(workUnit);

            sync();
        }
        else
        {
            int newSplitSize = querySequences.size()/2;

            Vector subRes1 = spawn_splitQuerySequences(splitQuerySequencesFirstPart(newSplitSize, workUnit));
            Vector subRes2 = spawn_splitQuerySequences(splitQuerySequencesSecondPart(newSplitSize, workUnit));

            sync();

            result = combineSubResults(subRes1,subRes2);

        }

        return result;
    }

    public Vector spawn_splitDatabaseSequences(Vector workUnit)
    {
        Vector result;
        int newSplitSize;

        Vector databaseSequences  = (Vector)workUnit.get(4);

        if(databaseSequences.size() <= threshold)
        {
            result = createTrivialResult(workUnit);
        }
        else
        {
            newSplitSize = databaseSequences.size()/2;

            Vector subResult1 = spawn_splitDatabaseSequences(splitDatabaseSequencesFirstPart(newSplitSize, workUnit));
            Vector subResult2 = spawn_splitDatabaseSequences(splitDatabaseSequencesSecondPart(newSplitSize, workUnit));

            sync();

            result = combineSubResults(subResult1,subResult2);
        }

        return result;
    }

    private Vector createTrivialResult(Vector workUnit)
    {
        Dsearch_AlgorithmV1 dA          = new Dsearch_AlgorithmV1();
        Vector subRes = new Vector();

        try
        {
            Vector resultUnit;

            resultUnit = dA.processUnit(workUnit);
            subRes     = processResultUnit(resultUnit);
        }
        catch(Throwable thr)
        {
                System.out.println("Exception in createTrivialResult in DivCon class: " + thr.toString());
        }

        return subRes;
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

    public void generateTheResult()
    {             
        Vector workUnit = new Vector();

        workUnit.add(alignmentAlgorithm);
        workUnit.add(scoresOrAlignments);
        workUnit.add(scoringScheme);
        workUnit.add(querySequencesPointers.getFileSequencesPointers());
        workUnit.add(databaseSequencesPointers.getFileSequencesPointers());

        theResult = spawn_splitQuerySequences(workUnit);

        sync();        
    }

    private Vector combineSubResults(Vector subResult1, Vector subResult2)
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

    public Vector getTheResult()
    {
        return theResult;
    }

    private Vector splitQuerySequencesFirstPart(int size, Vector workUnit)
    {
        Vector newQuerySequences  = new Vector();

        Vector querySequences    = (Vector)workUnit.get(3);
        Vector databaseSequences = (Vector)workUnit.get(4);

        for(int i = 0; i < size; i++)
            newQuerySequences.add(querySequences.get(i));

        Vector newWorkUnit = new Vector();

        newWorkUnit.add(workUnit.get(0));
        newWorkUnit.add(workUnit.get(1));
        newWorkUnit.add(workUnit.get(2));

        newWorkUnit.add(newQuerySequences);
        newWorkUnit.add(databaseSequences);

        return newWorkUnit;
    }

    private Vector splitDatabaseSequencesFirstPart(int size, Vector workUnit)
    {

        Vector newDatabaseSequences  = new Vector();
        Vector querySequences        = (Vector)workUnit.get(3);
        Vector databaseSequences     = (Vector)workUnit.get(4);

        for(int i = 0; i < size; i++)
            newDatabaseSequences.add(databaseSequences.get(i));

        Vector newWorkUnit = new Vector();

        newWorkUnit.add(workUnit.get(0));
        newWorkUnit.add(workUnit.get(1));
        newWorkUnit.add(workUnit.get(2));

        newWorkUnit.add(querySequences);
        newWorkUnit.add(newDatabaseSequences);

        return newWorkUnit;
    }

    private Vector splitQuerySequencesSecondPart(int size, Vector workUnit)
    {
        Vector newQuerySequences  = new Vector();

        Vector querySequences    = (Vector)workUnit.get(3);
        Vector databaseSequences = (Vector)workUnit.get(4);


        for(int i = size; i < querySequences.size(); i++)
            newQuerySequences.add(querySequences.get(i));

        Vector newWorkUnit = new Vector();

        newWorkUnit.add(workUnit.get(0));
        newWorkUnit.add(workUnit.get(1));
        newWorkUnit.add(workUnit.get(2));

        newWorkUnit.add(newQuerySequences);
        newWorkUnit.add(databaseSequences);

        return newWorkUnit;
    }

    private Vector splitDatabaseSequencesSecondPart(int size, Vector workUnit)
    {
        Vector newDatabaseSequences  = new Vector();

        Vector querySequences    = (Vector)workUnit.get(3);
        Vector databaseSequences = (Vector)workUnit.get(4);

        for(int i = size; i < databaseSequences.size(); i++)
            newDatabaseSequences.add(databaseSequences.get(i));

        Vector newWorkUnit = new Vector();

        newWorkUnit.add(workUnit.get(0));
        newWorkUnit.add(workUnit.get(1));
        newWorkUnit.add(workUnit.get(2));

        newWorkUnit.add(querySequences);
        newWorkUnit.add(newDatabaseSequences);

        return newWorkUnit;
    }

    public void setAlignmentAlgorithm(String alignmentAlgorithm) {
        this.alignmentAlgorithm = alignmentAlgorithm;
    }

    public void setScoresOrAlignments(int scoresOrAlignments) {
        this.scoresOrAlignments = scoresOrAlignments;
    }

    public void setScoringScheme(ScoringScheme scoringScheme)
    {
        this.scoringScheme = scoringScheme;
    }

    public void setQueryFile(String queryFile)
    {
        querySequencesPointers = new FileSequences(queryFile);
        querySequencesPointers.createFileSequncesPointers();
    }

    public void setDatabaseFile(String databaseFile)
    {
        databaseSequencesPointers = new FileSequences(databaseFile);
        databaseSequencesPointers.createFileSequncesPointers();
    }

    public void setThreshold(int threshold)
    {
        this.threshold = threshold;
    }

    public void setMaxScore(int maxScores)
    {
        this.maxScores = maxScores;
    }

     public int getThreshold()
    {
        return threshold;
    }

    public Vector getQuerySequences()
    {
        return querySequencesPointers.getFileSequencesPointers();
    }

    public Vector getDatabaseSequences()
    {
        return databaseSequencesPointers.getFileSequencesPointers();
    }
}

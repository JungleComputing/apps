//package dsearchDC_so;

import neobio.alignment.ScoringScheme;
import java.util.Vector;
import java.io.*;

public class DivCon_so extends ibis.satin.SatinObject implements DivCon_soInterface, Serializable
{
    private int threshold;

    private Vector theResult;

    private Vector databaseSeqsPointers;
    private Vector querySeqsPointers;

    private int maxScores;
    private String delimiter;
    private String alignmentAlgorithm;
    private int scoresOrAlignments;
    private ScoringScheme scoringScheme;

    private SharedData sharedData;

    public DivCon_so()
    {
        theResult = new Vector();
        sharedData = new SharedData();
      sharedData.exportObject();

    }

    public Vector spawn_splitQuerySeqs(Vector workUnit, SharedData sharedData)
    {
        Vector result;
        int mid_size;

        Vector queryPointers  = (Vector)workUnit.get(3);

        if(queryPointers.size() <= threshold)
        {
            result = spawn_splitDatabaseSeqs(workUnit, sharedData);
            sync();
        }
        else
        {
            mid_size = queryPointers.size()/2;

            Vector subRes1 = spawn_splitQuerySeqs(splitQueryPointersFirstPart(mid_size, workUnit), sharedData);
            Vector subRes2 = spawn_splitQuerySeqs(splitQueryPointersSecondPart(mid_size,workUnit), sharedData);

            sync();
            result = combineSubResults(subRes1,subRes2);

        }
        return result;
    }

    public Vector spawn_splitDatabaseSeqs(Vector workUnit, SharedData sharedData)
    {
        Vector result;
        int mid_size;

        Vector databasePointers  = (Vector)workUnit.get(4);

        if(databasePointers.size() <= threshold)
        {
            result = createTrivialResult(workUnit, sharedData);
            sync();
        }
        else
        {
            mid_size = databasePointers.size()/2;

            Vector subRes1 = spawn_splitDatabaseSeqs(splitDatabasePointersFirstPart(mid_size, workUnit), sharedData);
            Vector subRes2 = spawn_splitDatabaseSeqs(splitDatabasePointersSecondPart(mid_size,workUnit), sharedData);

            sync();
            result = combineSubResults(subRes1,subRes2);

        }
        return result;
    }

    public String toString()
    {
        String str = new String();

        for(int i = 0; i < theResult.size(); i++)
            str = str + (theResult.get(i)).toString() +  "\n\n";

        return str;
    }

    private Vector createTrivialResult(Vector workUnit, SharedData sharedData)
    {
        Dsearch_AlgorithmV1 dA          = new Dsearch_AlgorithmV1();
        Vector subResult = new Vector();

        try
        {
            Vector resultUnit = dA.processUnit(workUnit, sharedData);
            subResult         = processResultUnit(resultUnit);
        }
        catch(Throwable thr)
        {
            System.out.println("Exception in createTrivialResult in DivCon class: " + thr.toString());
        }

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

    public void generateTheResult()
    {
        Vector workUnit = new Vector();

        workUnit.add(alignmentAlgorithm);
        workUnit.add(scoresOrAlignments);
        workUnit.add(scoringScheme);
        workUnit.add(querySeqsPointers);
        workUnit.add(databaseSeqsPointers);

        theResult = spawn_splitQuerySeqs(workUnit, sharedData);

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

    private Vector splitQueryPointersFirstPart(int size, Vector workUnit)
    {
        Vector sub_queryPointes  = new Vector();

        Vector queryPointers      = (Vector)workUnit.get(3);
        Vector databasePointers   = (Vector)workUnit.get(4);

        for(int i = 0; i < size; i++)
            sub_queryPointes.add(queryPointers.get(i));

        Vector newWorkUnit = new Vector();

        newWorkUnit.add(workUnit.get(0));
        newWorkUnit.add(workUnit.get(1));
        newWorkUnit.add(workUnit.get(2));

        newWorkUnit.add(sub_queryPointes);
        newWorkUnit.add(databasePointers);

        return newWorkUnit;
    }

    private Vector splitDatabasePointersFirstPart(int size, Vector workUnit)
    {
        Vector sub_databasePointers  = new Vector();

        Vector queryPointers      = (Vector)workUnit.get(3);
        Vector databasePointers   = (Vector)workUnit.get(4);

        for(int i = 0; i < size; i++)
            sub_databasePointers.add(databasePointers.get(i));

        Vector newWorkUnit = new Vector();

        newWorkUnit.add(workUnit.get(0));
        newWorkUnit.add(workUnit.get(1));
        newWorkUnit.add(workUnit.get(2));

        newWorkUnit.add(queryPointers);
        newWorkUnit.add(sub_databasePointers);

        return newWorkUnit;
    }

    private Vector splitQueryPointersSecondPart(int size, Vector workUnit)
    {
        Vector sub_queryPointers  = new Vector();

        Vector queryPointers   = (Vector)workUnit.get(3);
        Vector databasePointes = (Vector)workUnit.get(4);


        for(int i = size; i < queryPointers.size(); i++)
            sub_queryPointers.add(queryPointers.get(i));

        Vector newWorkUnit = new Vector();

        newWorkUnit.add(workUnit.get(0));
        newWorkUnit.add(workUnit.get(1));
        newWorkUnit.add(workUnit.get(2));

        newWorkUnit.add(sub_queryPointers);
        newWorkUnit.add(databasePointes);

        return newWorkUnit;
    }

    private Vector splitDatabasePointersSecondPart(int size, Vector workUnit)
    {
        Vector sub_databasePointers  = new Vector();

        Vector queryPointers    = (Vector)workUnit.get(3);
        Vector databasePointers = (Vector)workUnit.get(4);


        for(int i = size; i < databasePointers.size(); i++)
            sub_databasePointers.add(databasePointers.get(i));

        Vector newWorkUnit = new Vector();

        newWorkUnit.add(workUnit.get(0));
        newWorkUnit.add(workUnit.get(1));
        newWorkUnit.add(workUnit.get(2));

        newWorkUnit.add(queryPointers);
        newWorkUnit.add(sub_databasePointers);

        return newWorkUnit;
    }

    private Vector processFile(String fileName, int part)
    {
        Vector seqs = new Vector();

        try{
            BufferedReader bf = new BufferedReader(new FileReader(fileName));

            String line = bf.readLine();

            while (line != null)
            {
                if(line.charAt(0) == '>')
                {
                    Vector seq = new Vector();
                    seq.add(line);

                    Vector tail = new Vector();
                    while((line = bf.readLine()) != null && line.charAt(0) != '>')
                    {
                        tail.add(line);
                    }
                    seq.add(tail);

                    seqs.add(seq);
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("MyException in processFile: " + e.toString());
        }

        Vector partSeqs = new Vector();
        int partSeqsSize = seqs.size() / part;

        for(int j=0; j < partSeqsSize; j++)
            partSeqs.add(seqs.get(j));

        return partSeqs;
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

    public void setThreshold(int threshold)
    {
        this.threshold = threshold;
    }

    public void setMaximumScore(int maxScores)
    {
        this.maxScores = maxScores;
    }

    public void setDelimiter(String delimiter)
    {
        this.delimiter = delimiter;
    }

    public void setSharedObject(String queryFile, String databaseFile)
    {
        FileSequences querySequences, databaseSequences;

        querySequences= new FileSequences(queryFile);
        querySequences.createFileSequnces();

        databaseSequences = new FileSequences(databaseFile);
        databaseSequences.createFileSequnces();

        querySeqsPointers    = createSeqsPointers(querySequences.getSequences().size());
        databaseSeqsPointers = createSeqsPointers(databaseSequences.getSequences().size());

        sharedData.updateQuerySeqs(querySequences);
        sharedData.updateDatabaseSeqs(databaseSequences);
    }

    private Vector createSeqsPointers(int size)
    {
        Vector pointers = new Vector();

        for(int i = 0; i < size; i++)
            pointers.add(i);

        return pointers;
    }

    public Vector getQuerySeqPointers()
    {
        return querySeqsPointers;
    }

    public Vector getDatabaseSeqPointers()
    {
        return databaseSeqsPointers;
    }

    public int getThreshold()
    {
        return threshold;
    }
}

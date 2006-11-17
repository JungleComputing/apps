//package dsearchDC;

import neobio.alignment.ScoringScheme;

import java.util.ArrayList;
import java.util.Vector;
import java.io.*;

public class DivCon extends ibis.satin.SatinObject implements DivConInterface,
        Serializable {
    private int threshold;

    private ArrayList<ResSeq> theResult;

    private FileSequences querySequences;

    private FileSequences databaseSequences;

    private int maxScores;

    private String alignmentAlgorithm;

    private int scoresOrAlignments;

    private ScoringScheme scoringScheme;

    public DivCon() {
        theResult = new ArrayList<ResSeq>();
    }

    public ArrayList<ResSeq> spawn_splitQuerySequences(Vector workUnit) {
        ArrayList<ResSeq> result;

        Vector querySequences = (Vector) workUnit.get(3);

        if (querySequences.size() <= threshold) {
            result = spawn_splitDatabaseSequences(workUnit);
            sync();
        } else {
            int newSplitSize = querySequences.size() / 2;

            ArrayList<ResSeq> subRes1 =
                    spawn_splitQuerySequences(
                        splitQuerySequences(0, newSplitSize, workUnit));
            ArrayList<ResSeq> subRes2 =
                    spawn_splitQuerySequences(splitQuerySequences(
                        newSplitSize, querySequences.size(), workUnit));

            sync();

            result = combineSubResults(subRes1, subRes2);
        }

        return result;
    }

    public ArrayList<ResSeq> spawn_splitDatabaseSequences(Vector workUnit) {
        ArrayList<ResSeq> result;
        int newSplitSize;

        Vector databaseSequences = (Vector) workUnit.get(4);

        if (databaseSequences.size() <= threshold) {
            result = createTrivialResult(workUnit);
        } else {
            newSplitSize = databaseSequences.size() / 2;

            ArrayList<ResSeq> subResult1 =
                    spawn_splitDatabaseSequences(splitDatabaseSequencesFirstPart(
                        newSplitSize, workUnit));
            ArrayList<ResSeq> subResult2 =
                    spawn_splitDatabaseSequences(splitDatabaseSequencesSecondPart(
                        newSplitSize, workUnit));

            sync();

            result = combineSubResults(subResult1, subResult2);
        }

        return result;
    }

    private ArrayList<ResSeq> createTrivialResult(Vector workUnit) {
        Dsearch_AlgorithmV1 dA = new Dsearch_AlgorithmV1();
        ArrayList<ResSeq> subResult = null;

        try {
            ArrayList<ResSeq> resultUnit = dA.processUnit(workUnit);
            subResult = processResultUnit(resultUnit);
      } catch (Throwable thr) {
            System.out
                .println("Exception in createTrivialResult in DivCon class: "
                    + thr.toString());
      }

        return subResult;
    }

    private ArrayList<ResSeq> processResultUnit(ArrayList<ResSeq> resultUnit) {
        ArrayList<ResSeq> subResult = new ArrayList<ResSeq>();

        for (int i = 0; i < resultUnit.size(); i++) {
            ResSeq resSeq;

            resSeq = resultUnit.get(i);
            resSeq.setMaximumScores(maxScores);
            resSeq.processDatabaseSeqs();

            subResult.add(resSeq);
        }

        return subResult;
    }

    public void generateTheResult() {
        Vector workUnit = new Vector();

        workUnit.add(alignmentAlgorithm);
        workUnit.add(scoresOrAlignments);
        workUnit.add(scoringScheme);
        workUnit.add(querySequences.getSequences());
        workUnit.add(databaseSequences.getSequences());

        querySequences = null;
        databaseSequences = null;

        theResult = spawn_splitQuerySequences(workUnit);

        sync();
    }

    private ArrayList<ResSeq> combineSubResults(ArrayList<ResSeq> subResult1, ArrayList<ResSeq> subResult2) {
        ArrayList<ResSeq> main = subResult1;
        ArrayList<ResSeq> additional = subResult2;

        for (int i = 0; i < additional.size(); i++)
            main = processSubResults(additional.get(i), main);

        ArrayList<ResSeq> res = new ArrayList<ResSeq>(main);

        return res;
    }

    private ArrayList<ResSeq> processSubResults(ResSeq resSeq, ArrayList<ResSeq> main) {
        boolean flag = false;
        for (int i = 0; i < main.size(); i++) {
            ResSeq resSeqMain = main.get(i);

            String name = resSeq.getQuerySequence().getSequenceName();
            String nameMain = resSeqMain.getQuerySequence().getSequenceName();

            if (nameMain.equals(name)) {
                flag = true;
                Vector newDatabaseSeqs = resSeq.getDatabaseSequences();
                resSeqMain.updateDatabaseSequences(newDatabaseSeqs);
            }
        }
        if (!flag) main.add(resSeq);

        return main;
    }

    public ArrayList<ResSeq> getTheResult() {
        return theResult;
    }

    private Vector splitQuerySequences(int begin, int end, Vector workUnit) {
        Vector newQuerySequences = new Vector();

        Vector querySequences = (Vector) workUnit.get(3);
        Vector databaseSequences = (Vector) workUnit.get(4);

        for (int i = begin; i < end; i++)
            newQuerySequences.add(querySequences.get(i));

        Vector newWorkUnit = new Vector();

        newWorkUnit.add(workUnit.get(0));
        newWorkUnit.add(workUnit.get(1));
        newWorkUnit.add(workUnit.get(2));

        newWorkUnit.add(newQuerySequences);
        newWorkUnit.add(databaseSequences);

        return newWorkUnit;
    }

    private Vector splitDatabaseSequencesFirstPart(int size, Vector workUnit) {

        Vector newDatabaseSequences = new Vector();
        Vector querySequences = (Vector) workUnit.get(3);
        Vector databaseSequences = (Vector) workUnit.get(4);

        for (int i = 0; i < size; i++)
            newDatabaseSequences.add(databaseSequences.get(i));

        Vector newWorkUnit = new Vector();

        newWorkUnit.add(workUnit.get(0));
        newWorkUnit.add(workUnit.get(1));
        newWorkUnit.add(workUnit.get(2));

        newWorkUnit.add(querySequences);
        newWorkUnit.add(newDatabaseSequences);

        return newWorkUnit;
    }

    private Vector splitDatabaseSequencesSecondPart(int size, Vector workUnit) {
        Vector newDatabaseSequences = new Vector();

        Vector querySequences = (Vector) workUnit.get(3);
        Vector databaseSequences = (Vector) workUnit.get(4);

        for (int i = size; i < databaseSequences.size(); i++)
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

    public void setScoringScheme(ScoringScheme scoringScheme) {
        this.scoringScheme = scoringScheme;
    }

    public void setQueryFile(String queryFile) {
        querySequences = new FileSequences(queryFile);
        querySequences.createFileSequnces();
    }

    public void setDatabaseFile(String databaseFile) {
        databaseSequences = new FileSequences(databaseFile);
        databaseSequences.createFileSequnces();
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void setMaximumScore(int maxScores) {
        this.maxScores = maxScores;
    }

    public int getThreshold() {
        return threshold;
    }

    public Vector getQuerySequences() {
        return querySequences.getSequences();
    }

    public Vector getDatabaseSequences() {
        return databaseSequences.getSequences();
    }
}

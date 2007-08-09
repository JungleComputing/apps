package geneSequencing;

import geneSequencing.divideAndConquer.DivCon;
import geneSequencing.masterWorker.MasterWorker;
import geneSequencing.sharedObjects.DivCon_so;
import geneSequencing.sharedObjects.SharedData;
import ibis.satin.impl.Satin;

import java.util.*;
import java.util.zip.GZIPOutputStream;
import java.io.*;

import neobio.alignment.ScoringScheme;

public class Dsearch {
    private PrintStream psRes;

    private boolean dump = false;

    private String[] args;

    private int threshold;

    private ArrayList<ResSeq> result = new ArrayList<ResSeq>();

    private FileSequences querySequences;

    private FileSequences databaseSequences;

    private int maxScores;

    private String alignmentAlgorithm;

    private int scoresOrAlignments;

    private ScoringScheme scoringScheme;

    private String implementationName = "none";

    public Dsearch(String[] args) {
        Satin.pause();
        this.args = args;

        if (args.length < 2) {
            throw new Error("Usage: java Dsearch <input file> <implementation name (dc, so, ...)> [-dump]");
        }

        implementationName = args[1];
        
        if (args.length == 3) {
            if (args[2].equals("-dump")) {
                dump = true;
            }
        }

        InputReader iR = null;

        try {
            iR = new InputReader(args[0]);
        } catch (Throwable e) {
            throw new Error("An error occurred: " + e.toString());
        }

        alignmentAlgorithm = iR.getAlignmentAlgorithm();
        scoresOrAlignments = iR.getScoresOrAlignments();
        scoringScheme = iR.getScoringScheme();
        maxScores = iR.getMaxScores();
        threshold = iR.getValueOfThreshold();

        String queryFile = iR.getQueryFile();
        querySequences = new FileSequences(queryFile);

        String databaseFile = iR.getDatabaseFile();
        databaseSequences = new FileSequences(databaseFile);
        Satin.resume();
    }

    private void saveResult() {
        try {
            File tmp = new File(args[0]);
            FileOutputStream fos =
                    new FileOutputStream("result_" + tmp.getName() + ".gz");
            BufferedOutputStream buf = new BufferedOutputStream(fos);
            GZIPOutputStream zip = new GZIPOutputStream(buf);
            psRes = new PrintStream(zip);

            for (int i = 0; i < result.size(); i++) {
                psRes.println((result.get(i)).toString() + "\n\n");
            }

            psRes.close();
        } catch (Exception e) {
            System.out.println("Exception in createResultFile(): "
                    + e.toString());
        }
    }

    public static ArrayList<ResSeq> createTrivialResult(WorkUnit workUnit) {
        Dsearch_AlgorithmV1 dA = new Dsearch_AlgorithmV1();
        ArrayList<ResSeq> subResult = null;

        try {
            ArrayList<ResSeq> resultUnit =
                    dA.processUnit(workUnit.querySequences,
                                    workUnit.databaseSequences,
                                    workUnit.scoresOrAlignments,
                                    workUnit.scoringScheme,
                                    workUnit.alignmentAlgorithm);
            subResult = processResultUnit(resultUnit, workUnit.maxScores);
        } catch (Throwable thr) {
            System.out.println("Exception in createTrivialResult: "
                    + thr.toString());
        }

        return subResult;
    }

    private static ArrayList<ResSeq> processResultUnit(
            ArrayList<ResSeq> resultUnit, int maxScores) {
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

    public void generateResult(WorkUnit workUnit) {
        if (implementationName.equals("dc")) {
            System.out.println("using divide and conquer implementation");
            DivCon dC = new DivCon();
            result = dC.spawn_splitQuerySequences(workUnit);
            dC.sync();
        } else if (implementationName.equals("so")) {
            System.out.println("using shared objects implementation");
            SharedData sharedData = new SharedData(querySequences, databaseSequences);
            sharedData.exportObject();
            
            DivCon_so so = new DivCon_so();
            result = so.spawn_splitQuerySequences(workUnit, sharedData);
            so.sync();
        } else if (implementationName.equals("mw")) {
            System.out.println("using master worker implementation");
            MasterWorker mw = new MasterWorker();
            result = mw.generateResult(workUnit);
        } else {
            throw new Error("illegal implementation name");
        }
    }

    public static ArrayList<ResSeq> combineSubResults(
            ArrayList<ResSeq> subResult1, ArrayList<ResSeq> subResult2) {
        ArrayList<ResSeq> main = subResult1;
        ArrayList<ResSeq> additional = subResult2;

        for (int i = 0; i < additional.size(); i++) {
            main = processSubResults(additional.get(i), main);
        }
        return main;
    }

    private static ArrayList<ResSeq> processSubResults(ResSeq resSeq,
            ArrayList<ResSeq> main) {
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
        if (!flag)
            main.add(resSeq);

        return main;
    }

    public void start() {
        System.out.println("\n---> START <---");

        System.out.println();
        System.out.println("query sequences    = " + querySequences.size());
        System.out.println("database sequences = " + databaseSequences.size());
        System.out.println("threshold          = " + threshold);
        System.out.println();

        double startTime = System.currentTimeMillis();

        WorkUnit workUnit =
                new WorkUnit(alignmentAlgorithm, scoresOrAlignments,
                        scoringScheme, querySequences.getSequences(),
                        databaseSequences.getSequences(), maxScores, threshold);

        generateResult(workUnit);

        System.out.println("application genesequencing_" + implementationName + " took "
                + (System.currentTimeMillis() - startTime) / 1000.0 + " sec");

        if (dump) {
            Satin.pause();
            double start1 = System.currentTimeMillis();
            saveResult();
            double end1 = System.currentTimeMillis() - start1;
            System.out.println("\nThe result has been printed in " + end1
                    / 1000.0 + " sec");
            Satin.resume();
        }

        System.out.println("\n---> FINISH <---");
    }

    public static void main(String[] args) {
        new Dsearch(args).start();
    }
}

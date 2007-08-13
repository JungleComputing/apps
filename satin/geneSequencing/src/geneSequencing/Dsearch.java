package geneSequencing;

import geneSequencing.divideAndConquer.DivCon;
import geneSequencing.masterWorker.MasterWorker;
import geneSequencing.sharedObjects.DivConSO;
import geneSequencing.sharedObjects.SharedData;
import ibis.satin.impl.Satin;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import neobio.alignment.ScoringScheme;

public class Dsearch {
    private PrintStream psRes;

    private boolean dump = false;

    private String inputFileName;

    private String implementationName = "none";

    public Dsearch(String[] args) {
        if (args.length < 2) {
            throw new Error(
                "Usage: java Dsearch <input file> <implementation name (dc, so, ...)> [-dump]");
        }

        inputFileName = args[0];
        implementationName = args[1];

        if (args.length == 3) {
            if (args[2].equals("-dump")) {
                dump = true;
            }
        }
    }

    private void saveResult(ArrayList<ResSeq> result) {
        try {
            File tmp = new File(inputFileName);
            FileOutputStream fos = new FileOutputStream("result_"
                + tmp.getName() + ".gz");
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

    public static ArrayList<ResSeq> createTrivialResult(WorkUnit workUnit,
        SharedData shared, int startQuery, int endQuery, int startDatabase,
        int EndDatabase) {

        ArrayList<Sequence> querySequences = getSequences(shared
            .getQuerySequences(), startQuery, endQuery);
        ArrayList<Sequence> databaseSequences = getSequences(shared
            .getDatabaseSequences(), startDatabase, EndDatabase);

        return createTrivialResult(workUnit.alignmentAlgorithm,
            workUnit.scoresOrAlignments, workUnit.scoringScheme,
            querySequences, databaseSequences, workUnit.maxScores);
    }

    public static ArrayList<Sequence> getSequences(ArrayList<Sequence> seq,
        int start, int end) {
        ArrayList<Sequence> res = new ArrayList<Sequence>();

        for (int i = start; i < end; i++) {
            res.add(seq.get(i));
        }

        return res;
    }

    public static ArrayList<ResSeq> createTrivialResult(WorkUnit workUnit) {
        return createTrivialResult(workUnit.alignmentAlgorithm,
            workUnit.scoresOrAlignments, workUnit.scoringScheme,
            workUnit.querySequences, workUnit.databaseSequences,
            workUnit.maxScores);
    }

    private static ArrayList<ResSeq> createTrivialResult(
        String alignmentAlgorithm, int scoresOrAlignments,
        ScoringScheme scoringScheme, ArrayList<Sequence> querySequences,
        ArrayList<Sequence> databaseSequences, int maxScores) {
        Dsearch_AlgorithmV1 dA = new Dsearch_AlgorithmV1();

            ArrayList<ResSeq> resultUnit = dA.processUnit(querySequences,
                databaseSequences, scoresOrAlignments, scoringScheme,
                alignmentAlgorithm);
            processResultUnit(resultUnit, maxScores);
            return resultUnit;
    }

    private static void processResultUnit(
            ArrayList<ResSeq> resultUnit, int maxScores) {
            
            for (int i = 0; i < resultUnit.size(); i++) {
                ResSeq resSeq = resultUnit.get(i);
                resSeq.setMaximumScores(maxScores);
                resSeq.processDatabaseSeqs();
            }
    }

    public ArrayList<ResSeq> generateResult(WorkUnit workUnit) {
        ArrayList<ResSeq> result;
        
        if (implementationName.equals("dc")) {
            System.out.println("using divide and conquer implementation");
            DivCon dC = new DivCon();
            result = dC.spawn_splitQuerySequences(workUnit);
            dC.sync();
        } else if (implementationName.equals("so")) {
            System.out.println("using shared objects implementation");
            SharedData sharedData = new SharedData(workUnit.querySequences,
                workUnit.databaseSequences);
            sharedData.exportObject();

            int qSize = workUnit.querySequences.size();
            int dbSize = workUnit.databaseSequences.size();
            workUnit.databaseSequences = null;
            workUnit.querySequences = null;

            DivConSO so = new DivConSO();
            result = so.spawn_splitQuerySequences(workUnit, sharedData, 0,
                qSize, 0, dbSize);
            so.sync();
        } else if (implementationName.equals("mw")) {
            System.out.println("using master worker implementation");
            MasterWorker mw = new MasterWorker();
            result = mw.generateResult(workUnit);
        } else {
            throw new Error("illegal implementation name");
        }
        
        return result;
    }

    public static ArrayList<ResSeq> combineSubResults(
            ArrayList<ResSeq> main, ArrayList<ResSeq> additional) {
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
                ArrayList<Sequence> newDatabaseSeqs = resSeq.getDatabaseSequences();
                resSeqMain.updateDatabaseSequences(newDatabaseSeqs);
            }
        }
        if (!flag) {
            main.add(resSeq);
        }

        return main;
    }

    public void start() {
        Satin.pause();

        InputReader iR = null;

        try {
            iR = new InputReader(inputFileName);
        } catch (Throwable e) {
            throw new Error("An error occurred: " + e.toString());
        }

        String alignmentAlgorithm = iR.getAlignmentAlgorithm();
        int scoresOrAlignments = iR.getScoresOrAlignments();
        ScoringScheme scoringScheme = iR.getScoringScheme();
        int maxScores = iR.getMaxScores();
        int threshold = iR.getValueOfThreshold();

        String queryFile = iR.getQueryFile();
        FileSequences querySequences = new FileSequences(queryFile);

        String databaseFile = iR.getDatabaseFile();
        FileSequences databaseSequences = new FileSequences(databaseFile);

        Satin.resume();

        System.out.println(databaseSequences.size() + " database Sequences, "
            + querySequences.size() + " query sequences, "
            + "threshold " + threshold
            + ", implementation: " + implementationName);

        double startTime = System.currentTimeMillis();

        WorkUnit workUnit = new WorkUnit(alignmentAlgorithm,
            scoresOrAlignments, scoringScheme, querySequences.getSequences(),
            databaseSequences.getSequences(), maxScores, threshold);

        ArrayList<ResSeq> result = generateResult(workUnit);

        System.out.println("application genesequencing_" + implementationName
            + " took " + (System.currentTimeMillis() - startTime) / 1000.0
            + " sec");

        if (dump) {
            Satin.pause();
            double start1 = System.currentTimeMillis();
            saveResult(result);
            double end1 = System.currentTimeMillis() - start1;
            System.out.println("\nThe result has been printed in " + end1
                / 1000.0 + " sec");
            Satin.resume();
        }
    }

    public static void main(String[] args) {
        new Dsearch(args).start();
    }
}

package geneSequencing.sharedObjects;

import geneSequencing.InputReader;
import geneSequencing.ResSeq;

import java.util.*;
import java.io.*;

public class DsearchDC_so {
    private PrintStream psRes;

    private InputReader iR;

    private DivCon_so dC;

    private ArrayList<ResSeq> theResult;

    public DsearchDC_so(String[] args) {
        theResult = new ArrayList<ResSeq>();

        try {
            iR = new InputReader(args[0]);
        } catch (Throwable e) {
            System.out.println("Exception in DsearchDC_so(): " + e.toString());
        }

        dC = new DivCon_so();
    }

    private void createResultFile() {
        try {
            File PD = new File(System.getProperty("user.dir"));
            File r = new File(PD, "result.txt");
            FileOutputStream fos = new FileOutputStream(r);
            psRes = new PrintStream(fos);
        } catch (Exception e) {
            System.out.println("Exception in createResultFile(): "
                    + e.toString());
        }
    }

    private void doStepByStep(double startTime) {
        generateResultDivCon(startTime);
    }

    private void generateResultDivCon(double startTime) {
        System.out.println();
        System.out.println("query sequences    = "
                + dC.getQuerySeqPointers().size());
        System.out.println("database sequences = "
                + dC.getDatabaseSeqPointers().size());
        System.out.println("threshold          = " + dC.getThreshold());
        System.out.println();

        dC.generateTheResult();
        System.out.println("The result has been generated in "
                + (System.currentTimeMillis() - startTime) / 1000.0 + " sec\n");

        theResult = dC.getTheResult();

        double start1 = System.currentTimeMillis();
        printTheResultInFile();
        double end1 = System.currentTimeMillis() - start1;
        System.out.println("The result has been printed " + end1 / 1000.0
                + " sec");

        double time = (System.currentTimeMillis() - startTime) / 1000.0;
        System.out.println("\nThe program has been finished in " + time
                + " sec");
    }

    private void printTheResultInFile() {
        createResultFile();

        for (int i = 0; i < theResult.size(); i++)
            psRes.println((theResult.get(i)).toString() + "\n\n");
    }

    private void processArgumentsAndSetValues() {
        dC.setAlignmentAlgorithm(iR.getAlignmentAlgorithm());
        dC.setScoresOrAlignments(iR.getScoresOrAlignments());
        dC.setScoringScheme(iR.getScoringScheme());
        dC.setMaximumScore(iR.getMaxScores());
        dC.setThreshold(iR.getValueOfThreshold());
        dC.setSharedObject(iR.getQueryFile(), iR.getDatabaseFile());
    }

    public void start() {
        System.out.println("\n---> START <---");
        double startTime = System.currentTimeMillis();
        processArgumentsAndSetValues();
        doStepByStep(startTime);
        System.out.println("\n---> FINISH <---\n");
    }

    public static void main(String[] args) {
        new DsearchDC_so(args).start();
    }
}
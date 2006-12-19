//package dsearchDC;

import ibis.satin.impl.Satin;

import java.util.*;
import java.io.*;

public class DsearchDC {
    private PrintStream psRes;

    private InputReader iR;

    private DivCon dC;

    private ArrayList<ResSeq> theResult;

    public DsearchDC(String[] args) {
        theResult = new ArrayList<ResSeq>();

        if(args.length != 1) {
            System.err.println("Usage: java DsearchDC <input file>");
            System.exit(1);
        }
        
        try {
            iR = new InputReader(args[0]);
        } catch (Throwable e) {
            System.out.println("An error occurred: " + e.toString());
            System.exit(1);
        }

        dC = new DivCon();
    }

    private void createResultFile() {
        try {
            File PD = new File(System.getProperty("user.dir"));
            File r = new File(PD, "result.txt");
            FileOutputStream fos = new FileOutputStream(r);
            psRes = new PrintStream(new BufferedOutputStream(fos));
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
            + dC.getQuerySequences().size());
        System.out.println("database sequences = "
            + dC.getDatabaseSequences().size());
        System.out.println("threshold          = " + dC.getThreshold());
        System.out.println();

        dC.generateTheResult();
        System.out.println("The result has been generated in "
            + (System.currentTimeMillis() - startTime) / 1000.0 + " sec");

        theResult = dC.getTheResult();

        Satin.pause();
        double start1 = System.currentTimeMillis();
        printTheResultInFile();
        double end1 = System.currentTimeMillis() - start1;
        System.out.println("\nThe result has been printed in " + end1 / 1000.0
            + " sec");
        Satin.resume();

        double time = (System.currentTimeMillis() - startTime) / 1000.0;
        System.out.println("\nThe program has been finished in " + time
            + " sec");
    }

    private void printTheResultInFile() {
        createResultFile();

        for (int i = 0; i < theResult.size(); i++) {
            psRes.println((theResult.get(i)).toString() + "\n\n");
        }
        psRes.close();
    }

    private void processArgumentsAndSetValues() {
        dC.setAlignmentAlgorithm(iR.getAlignmentAlgorithm());
        dC.setScoresOrAlignments(iR.getScoresOrAlignments());
        dC.setScoringScheme(iR.getScoringScheme());
        dC.setMaximumScore(iR.getMaxScores());
        dC.setThreshold(iR.getValueOfThreshold());
        dC.setQueryFile(iR.getQueryFile());
        dC.setDatabaseFile(iR.getDatabaseFile());
    }

    public void start() {
        System.out.println("\n---> START <---");
        double startTime = System.currentTimeMillis();
        processArgumentsAndSetValues();
        doStepByStep(startTime);
        System.out.println("\n---> FINISH <---");
    }

    public static void main(String[] args) {
        new DsearchDC(args).start();
    }
}

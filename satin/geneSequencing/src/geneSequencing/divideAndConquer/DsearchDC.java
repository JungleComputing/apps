package geneSequencing.divideAndConquer;

import geneSequencing.InputReader;
import geneSequencing.ResSeq;
import ibis.satin.impl.Satin;

import java.util.*;
import java.io.*;

public class DsearchDC {
    private PrintStream psRes;

    private InputReader iR;

    private DivCon dC;

    private ArrayList<ResSeq> theResult;

    private boolean dump = false;
    
    String[] args;
    
    public DsearchDC(String[] args) {
        this.args = args;
        theResult = new ArrayList<ResSeq>();

        if(args.length < 1) {
            System.err.println("Usage: java DsearchDC <input file> [-dump]");
            System.exit(1);
        }
        
        if(args.length == 2) {
            if(args[1].equals("-dump")) {
                dump = true;
            }
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
            File r = new File(PD, args[0] + "_result.txt");
            FileOutputStream fos = new FileOutputStream(r);
            psRes = new PrintStream(new BufferedOutputStream(fos));
        } catch (Exception e) {
            System.out.println("Exception in createResultFile(): "
                + e.toString());
        }
    }

    private void generateResultDivCon(double startTime) {
        System.out.println();
        System.out.println("query sequences    = "
            + dC.getQuerySequences().size());
        System.out.println("database sequences = "
            + dC.getDatabaseSequences().size());
        System.out.println("threshold          = " + dC.getThreshold());
        System.out.println();

        Satin.resume();
        
        dC.generateTheResult();
        System.out.println("The result has been generated in "
            + (System.currentTimeMillis() - startTime) / 1000.0 + " sec");

        theResult = dC.getTheResult();

        if(dump) {
        Satin.pause();
            double start1 = System.currentTimeMillis();
            printTheResultInFile();
            double end1 = System.currentTimeMillis() - start1;
            System.out.println("\nThe result has been printed in " + end1 / 1000.0
                + " sec");
            Satin.resume();
        }
        
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
        generateResultDivCon(startTime);
        System.out.println("\n---> FINISH <---");
    }

    public static void main(String[] args) {
        Satin.pause();
        new DsearchDC(args).start();
    }
}

//package dsearchDC;

import java.util.*;
import java.io.*;

public class DsearchDC
{
    private PrintStream psRes;
    private InputReader iR;
    private DivCon dC;
    private Vector theResult;

    public DsearchDC()
    {
        theResult   = new Vector();

        try
        {
            iR = new InputReader();
        }
        catch (Throwable e)
        {
            System.out.println("Exception in  Dsearch(): " + e.toString());
        }

        dC = new DivCon();
    }

    private void createResultFile()
    {
        try
        {
            File PD = new File(System.getProperty("user.dir"));
            File r = new File(PD, "result.txt");
            FileOutputStream fos = new FileOutputStream(r);
            psRes = new PrintStream(fos);
        }
        catch (Exception e)
        {
            System.out.println("Exception in createResultFile(): " + e.toString());
        }
    }

    private void doStepByStep(double startTime)
    {
        generateResultDivCon(startTime);
    }

    private void generateResultDivCon(double startTime)
    {
        System.out.println();
        System.out.println("query sequences    = " + dC.getQuerySequences().size());
        System.out.println("database sequences = " + dC.getDatabaseSequences().size());
        System.out.println("threshold          = " + dC.getThreshold());
        System.out.println();

        dC.generateTheResult();
        System.out.println("The result has been generated in " +
                            (System.currentTimeMillis() - startTime)/1000.0 + " sec");

        theResult = dC.getTheResult();
        
        double start1 = System.currentTimeMillis();
        printTheResultInFile();
        double end1 = System.currentTimeMillis() - start1;
        System.out.println("\nThe result has been printed in " + end1/1000.0 + " sec");


        double time = (System.currentTimeMillis() - startTime) / 1000.0;
        System.out.println("\nThe program has been finished in " + time + " sec");                                      
    }

    private void printTheResultInFile()
    {
        createResultFile();

        for(int i = 0; i < theResult.size(); i++)
            psRes.println((theResult.get(i)).toString()+ "\n\n");
    }

    private void processArgumentsAndSetValues()
    {
        dC.setAlignmentAlgorithm(iR.getAlignmentAlgorithm());
        dC.setScoresOrAlignments(iR.getScoresOrAlignments());
        dC.setScoringScheme(iR.getScoringScheme());
        dC.setMaximumScore(iR.getMaxScores());
        dC.setThreshold(iR.getValueOfThreshold());
        dC.setQueryFile(iR.getQueryFile());
        dC.setDatabaseFile(iR.getDatabaseFile());
    }

    public void start(String[] args)
    {
        System.out.println("\n---> START <---");
        double startTime = System.currentTimeMillis();
        processArgumentsAndSetValues();        
        doStepByStep(startTime);
        System.out.println("\n---> FINISH <---");
    }

    public static void main(String[] args)
    {
        new DsearchDC().start(args);
    }
}
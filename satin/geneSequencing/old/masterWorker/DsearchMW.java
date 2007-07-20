//package dsearchMW;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Vector;

public class DsearchMW
{
    private PrintStream psRes;
    private InputReader iR;
    private MastWork mW;
    private Vector theResult;

    public DsearchMW()
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

        mW = new MastWork();
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
        generateResultMastWork(startTime);
    }

    private void generateResultMastWork(double startTime)
    {
        System.out.println();
        System.out.println("query sequences    = " + mW.getQuerySequences().size());
        System.out.println("database sequences = " + mW.getDatabaseSequences().size());
        System.out.println("threshold          = " + mW.getThreshold());

        mW.generateTheResult();
        System.out.println();
        System.out.println("The result has been generated in " +
                (System.currentTimeMillis() - startTime)/1000.0 + " sec\n");

        theResult = mW.getTheResult();

        double start1 = System.currentTimeMillis();
        printTheResultInFile();
        double end1 = System.currentTimeMillis() - start1;
        System.out.println("The result has been printed " + end1/1000.0 + " sec");

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
        mW.setAlignmentAlgorithm(iR.getAlignmentAlgorithm());
        mW.setScoresOrAlignments(iR.getScoresOrAlignments());
        mW.setScoringScheme(iR.getScoringScheme());
        mW.setMaximumScore((iR.getMaxScores()));
        mW.setThreshold(iR.getValueOfThreshold());
        mW.setQueryFile(iR.getQueryFile());
        mW.setDatabaseFile(iR.getDatabaseFile());
    }


    public void start(String[] args)
    {
        System.out.println("\n---> START <---");
        double startTime = System.currentTimeMillis();
        processArgumentsAndSetValues();
        doStepByStep(startTime);
        System.out.println("\n---> FINISH <---\n");
    }

    public static void main(String[] args)
    {
        new DsearchMW().start(args);
    }
}

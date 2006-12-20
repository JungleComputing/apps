//package dsearchDC;

import neobio.alignment.*;
import java.io.*;
import java.util.*;

public class InputReader implements AlignmentAlgorithms {

    private ScoringScheme scoringScheme; //the scoring scheme for the computation

    private String alignmentAlgorithm; //name of the alignment algorithm being used to align sequences

    private int scoresOrAlignments;

    private int valueOfThreshold;

    private File database; //database file

    private File query; //database file

    private int numScores; //how many top scores per query sequence to record

    public InputReader(String parameterFile) throws Throwable {
        //read and check all parameters in the inputs file
        BufferedReader inputs =
                new BufferedReader(new FileReader(new File(parameterFile)));
        Properties parameters = new Properties();
        String line = inputs.readLine();
        while (line != null) {
            if (line.length() > 0 && line.charAt(0) != '#') {
                StringTokenizer stk = new StringTokenizer(line, "=");
                if (stk.countTokens() == 2) {
                    String lhs = stk.nextToken().trim().toLowerCase();
                    String rhs = stk.nextToken().trim();
                    lhs = lhs.toLowerCase();

                    //file names cannot be changed
                    if (!lhs.endsWith("file")) {
                        rhs = rhs.toLowerCase();
                    }
                    parameters.setProperty(lhs, rhs);
                }
            }
            line = inputs.readLine();
        }
        inputs.close();

        //get the input parameters
        String property = parameters.getProperty("database.file");
        if (property == null) {
            throw new Exception(
                "Could not get name of database.file parameter from parameter file");
        }

        database = new File(property);
        if (!database.isFile()) {
            throw new Exception(
                "Cannot find database file: " + property);
        }

        property = parameters.getProperty("query.file");
        if (property == null) {
            throw new Exception(
                "Could not get name of query.file parameter from parameter file");
        }

        query = new File(property);
        if (!query.isFile()) {
            throw new Exception(
                "Cannot find query file - was it included as problem data? Was the name of the query file entered into the parameter file?");
        }

        property = parameters.getProperty("scores.or.alignments");
        if (property == null)
            throw new Exception(
                "Could not get scoresOrAlignments parameter from parameter file");

        try {
            scoresOrAlignments = Integer.parseInt(property);
        } catch (Exception e) {
            throw new Exception(
                "Check scoresOrAlignments parameter in parameter file");
        }

        property = parameters.getProperty("top.scores");
        if (property == null) {
            throw new Exception(
                "Could not get top.scores parameter from parameter file");
        }

        try {
            numScores = Integer.parseInt(property);
        } catch (Exception e) {
            throw new Exception("Check top.scores parameter in parameter file");
        }

        property = parameters.getProperty("value.threshold");
        if (property == null) {
            throw new Exception(
                "Could not get value.threshold parameter from parameter file");
        }

        try {
            valueOfThreshold = Integer.parseInt(property);
        } catch (Exception e) {
            throw new Exception(
                "Check value.threshold parameter in parameter file");
        }

        property = parameters.getProperty("alignment.algorithm");
        if (property == null) {
            throw new Exception("Could not determine alignment algorithm");
        }

        alignmentAlgorithm = new String(property.toLowerCase());
        if (!alignmentAlgorithm.equals(NEEDLEMAN_WUNSCH)
            && !alignmentAlgorithm.equals(SMITH_WATERMAN)
            && !alignmentAlgorithm.equals(CROCHEMORE_GLOBAL)
            && !alignmentAlgorithm.equals(CROCHEMORE_LOCAL)) {
            throw new Exception("Alignment algorithm must be: "
                + NEEDLEMAN_WUNSCH + " or " + SMITH_WATERMAN + " or "
                + CROCHEMORE_GLOBAL + " or " + CROCHEMORE_LOCAL);
        }

        property = parameters.getProperty("scoring.scheme");
        if (property == null) {
            throw new Exception(
                "Could not get score.scheme parameter from parameter file");
        }

        if (property.equals("ss")) {
            //get the scoring scheme parameters
            property = parameters.getProperty("match");
            int match = 0;
            try {
                match = Integer.parseInt(property);
            } catch (Exception e) {
                throw new Exception("Could not get match value");
            }

            property = parameters.getProperty("mismatch");
            int mismatch = 0;
            try {
                mismatch = Integer.parseInt(property);
            } catch (Exception e) {
                throw new Exception("Could not get mismatch value");
            }

            property = parameters.getProperty("gap.penalty");
            int gapPenalty = 0;
            try {
                gapPenalty = Integer.parseInt(property);
            } catch (Exception e) {
                throw new Exception("Could not get gap.penalty value");
            }

            //create the scoring scheme
            scoringScheme = new BasicScoringScheme(match, mismatch, gapPenalty);
        } else if (property.equals("sm")) {
            property = parameters.getProperty("score.matrix");

            if (property == null) {
                throw new Exception(
                    "Could not get score.matrix parameter from parameter file");
            }
            File scoringMatrix = new File(property);
            if (!scoringMatrix.isFile()) {
                throw new Exception(
                    "Check that the score matrix file was included as problem data");
            }

            //check the score matrix is valid
            try {
                FileReader fr = new FileReader(scoringMatrix);
                scoringScheme = new ScoringMatrix(fr);
                fr.close();
            } catch (Exception e) {
                throw new Exception("Scoring matrix not valid: " + e);
            }
        } else {
            throw new Exception("User did not select valid scoring scheme type");
        }
    }

    public int getMaxScores() {
        return numScores;
    }

    public String getDatabaseFile() {
        return database.toString();
    }

    public String getQueryFile() {
        return query.toString();
    }

    public String getAlignmentAlgorithm() {
        return alignmentAlgorithm;
    }

    public int getScoresOrAlignments() {
        return scoresOrAlignments;
    }

    public ScoringScheme getScoringScheme() {
        return scoringScheme;
    }

    public int getValueOfThreshold() {
        return valueOfThreshold;
    }
}

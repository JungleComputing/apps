package geneSequencing;

import neobio.alignment.*;
import java.io.*;

public class Sequence_Aligner implements AlignmentAlgorithms, Serializable {
    public static String computeAlignment(String alignmentAlgorithm, Reader s1,
            Reader s2, ScoringScheme scoringScheme) throws Exception {
        PairwiseAlignmentAlgorithm algorithm = null;
        if (alignmentAlgorithm.equals(SMITH_WATERMAN)) {
            algorithm = new SmithWaterman();
        } else if (alignmentAlgorithm.equals(NEEDLEMAN_WUNSCH)) {
            algorithm = new NeedlemanWunsch();
        } else if (alignmentAlgorithm.equals(CROCHEMORE_LOCAL)) {
            algorithm = new CrochemoreLandauZivUkelsonLocalAlignment();
        } else if (alignmentAlgorithm.equals(CROCHEMORE_GLOBAL)) {
            algorithm = new CrochemoreLandauZivUkelsonGlobalAlignment();
        } else {
            throw new Exception("Non-existant alignment algorithm selected: "
                + alignmentAlgorithm);
        }

        // set scoring scheme
        algorithm.setScoringScheme(scoringScheme);

        algorithm.loadSequences(s1, s2);

        //return the actual alignment
        PairwiseAlignment alignment = algorithm.getPairwiseAlignment();

        // close files
        s1.close();
        s2.close();

        return alignment.toString();
    }

    public static int computeAlignmentScore(String alignmentAlgorithm,
            Reader s1, Reader s2, ScoringScheme scoringScheme) throws Exception {
        PairwiseAlignmentAlgorithm algorithm = null;
        if (alignmentAlgorithm.equals(SMITH_WATERMAN)) {
            algorithm = new SmithWaterman();
        } else if (alignmentAlgorithm.equals(NEEDLEMAN_WUNSCH)) {
            algorithm = new NeedlemanWunsch();
        } else if (alignmentAlgorithm.equals(CROCHEMORE_LOCAL)) {
            algorithm = new CrochemoreLandauZivUkelsonLocalAlignment();
        } else if (alignmentAlgorithm.equals(CROCHEMORE_GLOBAL)) {
            algorithm = new CrochemoreLandauZivUkelsonGlobalAlignment();
        } else {
            throw new Exception("Non-existant alignment algorithm selected: "
                + alignmentAlgorithm);
        }

        // set scoring scheme
        algorithm.setScoringScheme(scoringScheme);

        algorithm.loadSequences(s1, s2);

        //align the sequences and produce the score
        int score = algorithm.getScore();

        // close files
        s1.close();
        s2.close();

        return score;
    }
}

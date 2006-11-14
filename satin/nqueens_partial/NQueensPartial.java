/* $Id: NQueens.java 4345 2006-09-26 14:47:44Z ceriel $ */

import ibis.satin.SatinObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StreamTokenizer;

final class NQueensPartial extends SatinObject implements
        NQueensInterface, Serializable {

    static final long[] solutions = { 0, 0, 0, 0, 2, 10, 4, 40, 92, 352, 724,
        2680, 14200, 73712, 365596, 2279184L, 14772512L, 95815104L, 666090624L,
        4968057848L, 39029188884L, 314666222712L, 2691008701644L,
        24233937684440L, 227514171973736L };

    public long spawn_QueenNotInCorner(int sizee, int spawnLevel,
            int y, int left, int down, int right, int mask) {
        return QueenNotInCorner(sizee, spawnLevel, y, left, down, right,
            mask);
    }

    public long spawn_QueenInCorner(int y, int spawnLevel, int left, int down,
            int right, int bound1, int mask, int sizee) {
        return QueenInCorner(y, spawnLevel, left, down, right, bound1, mask,
            sizee);
    }

    private static final long seq_QueenInCorner(final int y, final int left,
            final int down, final int right, final int bound1, final int mask,
            final int sizee) {

        int bitmap = mask & ~(left | down | right);

        if (y == sizee) {
            if (bitmap != 0) {
                return 2;
            }
            return 0;
        }

        if (y < bound1) {
            bitmap |= 2;
            bitmap ^= 2;
        }

        long lnsol = 0;

        while (bitmap != 0) {
            int bit = -bitmap & bitmap;
            bitmap ^= bit;
            lnsol += seq_QueenInCorner(y + 1, (left | bit) << 1, down | bit,
                (right | bit) >> 1, bound1, mask, sizee);
        }

        return lnsol;
    }

    private long QueenInCorner(final int y, final int spawnLevel,
            final int left, final int down, final int right, final int bound1,
            final int mask, final int sizee) {

        // Check if we've gone deep enough into the recursion to 
        // have generated a decent number of jobs. If so, stop spawning
        // and switch to a sequential algorithm...
        if (spawnLevel <= 0) {
            return seq_QueenInCorner(y, left, down, right, bound1, mask, sizee);
        }

        int bitmap = mask & ~(left | down | right);

        if (y < bound1) {
            bitmap |= 2;
            bitmap ^= 2;
        }

        long[] lnsols = new long[sizee];
        int it = 0;

        while (bitmap != 0) {
            final int bit = -bitmap & bitmap;
            bitmap ^= bit;
            lnsols[it] = spawn_QueenInCorner(y + 1, spawnLevel - 1,
                (left | bit) << 1, down | bit, (right | bit) >> 1, bound1, mask,
                sizee);
            it++;
        }

        // Wait for all the result to be returned.
        sync();

        // Determine the sum of the solutions.
        long lnsol = 0;

        for (int i = 0; i < it; i++) {
            lnsol += lnsols[i];
        }

        return lnsol;
    }

    private static final long seq_QueenNotInCorner(
            final int y, final int left, final int down,
            final int right, final int mask) {

        int bitmap = mask & ~(left | down | right);

        // Check if we have reached the end of the board. If so, 
        // we check the number of solution this board represents.
        if (y == 0) {
            return bitmap != 0 ? 1 : 0;
        }

        long lnsol = 0;

        // Where not done, so recursively compute the rest of the solutions...
        while (bitmap != 0) {
            final int bit = -bitmap & bitmap;
            bitmap ^= bit;
            lnsol += seq_QueenNotInCorner(y - 1, (left | bit) << 1,
                down | bit, (right | bit) >> 1, mask);
        }

        return lnsol;
    }

    private long QueenNotInCorner(final int sizee,
            final int spawnLevel, final int y, final int left, final int down,
            final int right, final int mask) {

        // Check if we've gone deep enough into the recursion to 
        // have generated a decent number of jobs. If so, stop spawning
        // and switch to a sequential algorithm...
        if (spawnLevel <= 0) {
            return seq_QueenNotInCorner(y, left, down, right, mask);
        }

        int bitmap = mask & ~(left | down | right);

        int it = 0;
        long[] lnsols = new long[sizee];

        while (bitmap != 0) {
            int bit = -bitmap & bitmap;
            bitmap ^= bit;
            lnsols[it] = spawn_QueenNotInCorner(sizee, spawnLevel - 1,
                y - 1, (left | bit) << 1, down | bit, (right | bit) >> 1, mask);
            it++;
        }

        // Wait for all the result to be returned.
        sync();

        // Determine the sum of the solutions
        long lnsol = 0;

        for (int i = 0; i < it; i++) {
            lnsol += lnsols[i];
        }

        return lnsol;
    }

    private final long calculate(final long[] results, final int[] bounds,
            final int size, int spawnLevel) {

        final int SIZEE = size - 1;
        final int MASK = (1 << size) - 1;

        if (spawnLevel > SIZEE-1) {
            System.out.println("Spawnlevel set too high. Setting it to "
                    + (SIZEE-1));
            spawnLevel = SIZEE-1;
        }

        long start = System.currentTimeMillis();

        long[] tempresults = new long[size];

        for (int i = 0; i < bounds.length; i++) {

            final int SELECTED_BOUND = bounds[i];
            results[i] = 0;

            if (SELECTED_BOUND == 0) {
                // Queen in lower-left corner case.
                // Apparently, placing the 2nd queen on the upper border
                // does not give any solutions? Otherwise, why not include
                // SIZEE in this loop?
                for (int BOUND1 = 2; BOUND1 < SIZEE; BOUND1++) {

                    int bit = 1 << BOUND1;
                    tempresults[BOUND1] = spawn_QueenInCorner(2, spawnLevel,
                        (2 | bit) << 1, 1 | bit, bit >> 1, BOUND1, MASK, SIZEE);
                    // The "left" parameter actually is ((1 << 1) | bit) << 1.
                    // Likewise, the "right" parameter is ((1 >> 1) | bit) >> 1.
                }

            } else {
                final int bit = 1 << SELECTED_BOUND;

                results[i] = spawn_QueenNotInCorner(SIZEE,
                    spawnLevel, SIZEE-1, bit << 1, bit, bit >> 1, MASK);
            }
        }

        sync();

        for (int i = 0; i < bounds.length; i++) {
            if (bounds[i] == 0) {
                for (int j = 0; j < size; j++) {
                    results[i] += tempresults[j];
                }
            }
        }


        long end = System.currentTimeMillis();

        //printResults(results, nextResult, size, (end-start) / 1000.0);

        return (end - start);
    }

    private void printResults(long[] results, int[] bounds, int size,
            double time) {

        long nsol = 0;
        int maxbound = size/2 + size%2 - 1;
        boolean[] done = new boolean[maxbound+1];
        boolean size_done = true;

        for (int i = 0; i < results.length; i++) {
            System.out.println("result(" + size + ", " + bounds[i]
                    + ") = " + results[i]);
            nsol += results[i];
            if (bounds[i] != maxbound || (size % 2 == 0)) {
                nsol += results[i];
            }
            done[bounds[i]] = true;
        }

        for (int i = 0; i < done.length; i++) {
            if (! done[i]) {
                size_done = false;
                break;
            }
        }

        if (size_done) {
            System.out.print(" total result nqueens (" + size + ") = " + nsol);

            if (size < solutions.length) {
                if (nsol == solutions[size]) {
                    System.out.println(" application result is OK");
                } else {
                    System.out.println(" application result is WRONG!");
                }
            }
        }

        time = time / 1000.0;
        System.out.println(", time = " + time + " s.");
    }

    private static int readInt(StreamTokenizer d) throws IOException {
        if (d.ttype != StreamTokenizer.TT_NUMBER) {
            throw new IOException ("Format error in input");
        }
        int v = (int) d.nval;
        d.nextToken();
        return v;
    }

    private void doRun(StreamTokenizer d) throws IOException {
        while (d.ttype == StreamTokenizer.TT_EOL) {
            d.nextToken();
        }
        if (d.ttype == StreamTokenizer.TT_EOF) {
            return;
        }
        /* description of initial configuration */
        int repeat = readInt(d);
        int size = readInt(d);
        int spawnLevel = readInt(d);
        int maxbound = size/2 + size%2 - 1;

        int bounds_index = 0;
        int[] tempbounds = new int[size];

        while (d.ttype == StreamTokenizer.TT_NUMBER) {
            tempbounds[bounds_index] = readInt(d);
            if (tempbounds[bounds_index] > maxbound) {
                System.out.println("Illegal bound: " + tempbounds[bounds_index]
                        + ", ignored");
            } else {
                bounds_index++;
            }
        }

        int[] bounds = null;
        if (bounds_index == 0) {
            bounds = new int[maxbound + 1];
            for (int i = 0; i <= maxbound; i++) {
                bounds[i] = i;
            }
        } else {
            bounds = new int[bounds_index];
            for (int i = 0; i < bounds_index; i++) {
                bounds[i] = tempbounds[i];
            }
        }

        System.out.print("NQueens size " + size
                + ", spawnlevel " + spawnLevel
                + ", repeat " + repeat
                + ", bounds:");
        for (int i = 0; i < bounds.length; i++) {
            System.out.print(" " + bounds[i]);
        }
        System.out.println();

        for (int i = 0; i < repeat; i++) {
            final long results[] = new long[bounds.length];
            double time = calculate(results, bounds, size, spawnLevel);
            printResults(results, bounds, size, time);
        }
    }

    private void ReadFile(String file) {
        try {

            InputStream s = this.getClass().getClassLoader()
                    .getResourceAsStream(file);
            StreamTokenizer d = new StreamTokenizer(new InputStreamReader(s));

            d.commentChar('#');
            d.eolIsSignificant(true);
            d.parseNumbers();

            d.nextToken();

            while (d.ttype != StreamTokenizer.TT_EOF) {
                doRun(d);
            }

        } catch (Exception e) {
            System.err.println("ReadFile error: " + e);
            System.exit(1);
        }
    }

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("usage: nqueens <filename>");
            System.exit(1);
        }

        NQueensPartial nq = new NQueensPartial();
        nq.ReadFile(args[0]);
    }

}

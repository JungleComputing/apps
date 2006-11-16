/* $Id: NQueens.java 4345 2006-09-26 14:47:44Z ceriel $ */

import ibis.satin.SatinObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StreamTokenizer;

final class NQueensPartial extends SatinObject implements
        NQueensInterface, Serializable {

    static final int MAXSZ = 30;        // :-) Yeah, right.

    static final long[] solutions = { 0, 1, 0, 0, 2, 10, 4, 40, 92, 352, 724,
        2680, 14200, 73712, 365596, 2279184L, 14772512L, 95815104L, 666090624L,
        4968057848L, 39029188884L, 314666222712L, 2691008701644L,
        24233937684440L, 227514171973736L };

    private static final long seq_QueenCount(
            final int y, final int left, final int down,
            final int right, final int mask) {

        int bitmap = mask & ~(left | down | right);

        if (bitmap != 0) {
            if (y != 0) {
                long lnsol = 0;
                // We're not done, so recursively compute the rest of the
                // solutions...
                do {
                    final int bit = -bitmap & bitmap;
                    bitmap ^= bit;
                    lnsol += seq_QueenCount(y - 1, (left | bit) << 1,
                        down | bit, (right | bit) >> 1, mask);
                } while (bitmap != 0);

                return lnsol;
            }
            return 1;
        }
        return 0;
    }

    public long spawn_QueenCount(
            final int spawnLevel, final int y, final int left, final int down,
            final int right, final int mask) {

        // Check if we've gone deep enough into the recursion to 
        // have generated a decent number of jobs. If so, stop spawning
        // and switch to a sequential algorithm...
        if (spawnLevel <= 0) {
            return seq_QueenCount(y, left, down, right, mask);
        }

        int bitmap = mask & ~(left | down | right);

        int it = 0;
        long[] lnsols = new long[y+1];

        while (bitmap != 0) {
            int bit = -bitmap & bitmap;
            bitmap ^= bit;
            lnsols[it] = spawn_QueenCount(spawnLevel - 1,
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

        if (spawnLevel >= SIZEE-1 && spawnLevel > 0) {
            System.out.println("Spawnlevel set too high. Setting it to "
                    + (SIZEE-2));
            spawnLevel = SIZEE-2;
            if (spawnLevel < 0) {
                spawnLevel = 0;
            }
        }

        long start = System.currentTimeMillis();

        for (int i = 0; i < bounds.length; i++) {

            final int SELECTED_BOUND = bounds[i];
            final int bit = 1 << SELECTED_BOUND;

            if (size == 1) {
                results[SELECTED_BOUND] = 1;
            } else {
                results[SELECTED_BOUND] = spawn_QueenCount(
                    spawnLevel, SIZEE-1, bit << 1, bit, bit >> 1, MASK);
            }
        }

        sync();

        long end = System.currentTimeMillis();

        return (end - start);
    }

    private void printResults(long[] results, int[] bounds, int size,
            double time) {

        int maxbound = size/2 + size%2 - 1;
        boolean size_done = true;

        for (int i = 0; i < bounds.length; i++) {
            System.out.println("result(" + size + ", " + bounds[i]
                    + ") = " + results[bounds[i]]);
        }

        long nsol = 0;

        for (int i = 0; i < results.length; i++) {
            if (results[i] < 0) {
                size_done = false;
                break;
            }
            nsol += results[i];
            if (i != maxbound || (size % 2 == 0)) {
                nsol += results[i];
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

    private void doRun(StreamTokenizer d, long[][] results) throws IOException {
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

        if (results[size] == null) {
            results[size] = new long[maxbound+1];
            for (int i = 0; i <= maxbound; i++) {
                results[size][i] = -1;
            }
        }

        for (int i = 0; i < repeat; i++) {
            double time = calculate(results[size], bounds, size, spawnLevel);
            printResults(results[size], bounds, size, time);
        }
    }

    private void ReadFile(String file) {
        try {
            InputStream s = this.getClass().getClassLoader()
                    .getResourceAsStream(file);
            StreamTokenizer d = new StreamTokenizer(new InputStreamReader(s));
            long[][] results = new long[MAXSZ+1][];

            d.commentChar('#');
            d.eolIsSignificant(true);
            d.parseNumbers();

            d.nextToken();

            while (d.ttype != StreamTokenizer.TT_EOF) {
                doRun(d, results);
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

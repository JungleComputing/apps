/* $Id$ */

import ibis.satin.SatinObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.util.Date;

final class NQueens extends SatinObject implements NQueensInterface,
        Serializable {

    static final int MAXSZ = 30;        // :-) Yeah, right.

    static final long[] solutions = { 0, 1, 0, 0, 2, 10, 4, 40, 92, 352, 724,
        2680, 14200, 73712, 365596, 2279184L, 14772512L, 95815104L, 666090624L,
        4968057848L, 39029188884L, 314666222712L, 2691008701644L,
        24233937684440L, 227514171973736L };

    private static final long seq_QueenInCorner(final int y, final int left,
            final int right, final int bound1, final int mask) {
        if (bound1 < 0) {
            return seq_QueenInCorner1(y, left, right, mask);
        }
        // Don't allow a queen in the second row as long as the column
        // index is smaller than the row index of the queen in the second
        // column. (Symmetry).
        int bitmap = mask & ~(left | right | 2);

        long lnsol = 0;

        while (bitmap != 0) {
            int bit = -bitmap & bitmap;
            bitmap ^= bit;
            lnsol += seq_QueenInCorner(y - 1, (left | bit) << 1,
                (right | bit) >> 1, bound1 - 1, mask ^ bit);
        }

        return lnsol;
    }

    private static final long seq_QueenInCorner1(final int y, final int left,
            final int right, final int mask) {
        // Note: the 'y' counts down here.
        int bitmap = mask & ~(left | right);

        if (y == 0) {
            if (bitmap != 0) {
                return 8;
            }
            return 0;
        }

        long lnsol = 0;

        while (bitmap != 0) {
            int bit = -bitmap & bitmap;
            bitmap ^= bit;
            lnsol += seq_QueenInCorner1(y - 1, (left | bit) << 1,
                (right | bit) >> 1, mask ^ bit);
        }

        return lnsol;
    }

    public long spawn_QueenInCorner(final int y, final int spawnLevel,
            final int left, final int right, final int bound1,
            final int mask) {

        // Check if we've gone deep enough into the recursion to 
        // have generated a decent number of jobs. If so, stop spawning
        // and switch to a sequential algorithm...
        if (spawnLevel <= 0) {
            return seq_QueenInCorner(y, left, right, bound1, mask);
        }

        int bitmap = mask & ~(left | right);

        if (y == 0) {
            if (bitmap != 0) {
                return 8;
            }
            return 0;
        }

        if (bound1 >= 0) {
            bitmap |= 2;
            bitmap ^= 2;
        }

        // If where not deep enough, we keep spawning.
        long[] lnsols = new long[y+1];
        int it = 0;

        while (bitmap != 0) {
            final int bit = -bitmap & bitmap;
            bitmap ^= bit;
            lnsols[it] = spawn_QueenInCorner(y - 1, spawnLevel - 1,
                (left | bit) << 1, (right | bit) >> 1, bound1-1,
                mask ^ bit);
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

    private static final long seq_QueenNotInCorner(final int[] board,
            final int ydiff, final int y, final int left, final int right,
            final int mask, final int lastmask, final int sidemask,
            final int bound1) {
        int bitmap = mask & ~(left | right);

        // Check if we have reached the end of the board. If so, 
        // we check the number of solution this board represents.
        if (ydiff == 0) {
            if (bitmap != 0) {
                // There is still a position left
                if ((bitmap & lastmask) == 0) {
                    // ... and it is not dealt with earlier
                    board[y] = bitmap;
                    return Check(board, y, bound1);
                }
            }
            return 0;
        }

        if (y < bound1) {
            // Don't put a queen on the lower or upper row,
            // this case is already dealt with earlier (when bound1 was lower).
            bitmap |= sidemask;
            bitmap ^= sidemask;
        } else if (ydiff == bound1) {
            // This is the last opportunity to place the queen on the lower
            // or upper row. If we do this later, it will not count, because
            // this case has been dealt with earlier, when bound1 was lower.
            if ((sidemask & ~mask) == 0) {
                // No queens on the lower or upper row yet.
                // There should be at least one by now, because this is the
                // last opportunity to place the second one.
                return 0;
            }
            if ((sidemask & ~mask) != sidemask) {
                // If there is only one queen placed on the lower
                // or upper row yet, place the other one now.
                // Otherwise a dead end results (?)
                bitmap &= sidemask;
            }
        }

        if (bitmap == 0) {
            return 0;
        }

        // Check if there still is room for the last queen. After all, there
        // was already limited room for it, due to lastmask. If this check
        // fails, we cannot complete the board, even if we can still find
        // a position for the current column.
        if (((lastmask | (left << ydiff) | (right >> ydiff)) & mask) == mask) {
            // System.out.println("" + y);
            return 0;
        }

        long lnsol = 0;

        // Where not done, so recursively compute the rest of the solutions...
        do {
            final int bit = -bitmap & bitmap;
            board[y] = bit;
            bitmap ^= bit;
            lnsol += seq_QueenNotInCorner(board, ydiff-1, y+1, (left|bit) << 1,
                (right|bit) >> 1, mask ^ bit, lastmask, sidemask, bound1);
        } while (bitmap != 0);

        return lnsol;
    }

    public long spawn_QueenNotInCorner(final int[] board, final int sizee,
            final int spawnLevel, final int y, final int left, final int right,
            final int mask, final int lastmask, final int sidemask,
            final int bound1) {

        int bitmap = mask & ~(left | right);

        if (y < bound1) {
            bitmap |= sidemask;
            bitmap ^= sidemask;
        } else if (y == sizee-bound1) {
            if ((sidemask & ~mask) == 0) {
                return 0;
            }
            if ((sidemask & ~mask) != sidemask) {
                bitmap &= sidemask;
            }
        }

        // Check if we've gone deep enough into the recursion to 
        // have generated a decent number of jobs. If so, stop spawining
        // and switch to a sequential algorithm...
        if (spawnLevel <= 0) {
            return seq_QueenNotInCorner(board, sizee-y, y, left, right,
                    mask, lastmask, sidemask, bound1);
        }

        // If where not deep enough, we keep spawning.
        int it = 0;
        long[] lnsols = new long[sizee];

        while (bitmap != 0) {
            int[] boardClone = (int[]) board.clone();
            int bit = -bitmap & bitmap;
            boardClone[y] = bit;
            bitmap ^= bit;
            lnsols[it] = spawn_QueenNotInCorner(boardClone, sizee, spawnLevel-1,
                y + 1, (left | bit) << 1, (right | bit) >> 1, mask ^ bit,
                lastmask, sidemask, bound1);
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

    private static final long Check(final int[] board, final int sizee,
            final int bound1) {

        // There is a queen on position (0,bound1).
        // Check for rotations.
        if (board[sizee-bound1] == 1) {
            // Here, there is a queen on position (sizee-bound1,0).
            // This means that the position found now could be a 90-degree
            // rotation (anti-clockwise) of one that we found earlier.
            int own = 1;

            for (int ptn = 2; own <= sizee; own++, ptn <<= 1) {
                // Queen positions from 1 .. size-1. "own" is the bit number.
                // "ptn" the bit mask.
                int bit = 1;
                // Here, we are looking for a queen in row "own" as well
                // as a queen in column "row".
                for (int you = sizee; board[you] != ptn && board[own] >= bit; you--, bit <<= 1) {
                }

                if (board[own] > bit) {
                    // In this case, we found the queen in row "own" first
                    // (from right to left), which means that the row-index
                    // of the queen in column "own" is higher than the
                    // column-index of the queen in row "own".
                    // This means that this is a double of a 90-degree
                    // rotation of an earlier found solution, which means
                    // that it is counted already.
                    return 0;
                }
                if (board[own] < bit) {
                    // In this case, we found the queen in column "own" first
                    // (from right to left). This is a new one, and not a
                    // 90-degree rotation of itself.
                    break;
                }
                // Here, row "own" and column "own" don't tell us anything,
                // the queens on them are in positions (own, X) and
                // (sizee-X, own), which are 90-degree rotation positions.
                // Continue with the next row/column.
            }
            if (own > sizee) {
                // In this case, the board is the same as its 90-degree
                // rotation. We can still flip the board over the horizontal
                // axis, so it counts for 2.
                return 2;
            }
        }

        final int topbit = 1 << sizee;

        if (board[sizee] == (1 << (sizee-bound1))) {
            // Here, there is a queen on position (sizee,sizee-bound1).
            // This means that the position found now could be a 180-degree
            // rotation of one that we found earlier.
            
            int own = 1;
            int you;
            for (you = sizee - 1; own < you; own++, you--) {
                // stop condition was: own <= sizee
                int bit = 1;
                // Now, we are looking at queens in columns 1 and sizee-2,
                // 2 and sizee-3, et cetera, to find out if they too are at
                // 180-degree rotation positions of each other.
                for (int ptn = topbit; ptn != board[you] && board[own] >= bit; ptn >>= 1, bit <<= 1) {
                }

                if (board[own] > bit) {
                    // We found the one in row sizee-own first, which means
                    // that this position has been counted before (when it
                    // was rotated 180 degrees).
                    return 0;
                }
                if (board[own] < bit) {
                    // We found the one in column own first. This is a new
                    // one, and not a 180-degree rotation of itself (and also
                    // not a 90-degree rotation).
                    break;
                }
                // Here, the queens found are in 180-degree rotation
                // positions. Continue with the next two rows.
            }
            // if (own > sizee) {
            if (own >= you) {
                // 180-degree rotation of itself, but not 90-degree rotation.
                // So, it counts for 2*2 (flipping over the horizontal axis
                // too).
                return 4;
            }
        }

        if (board[bound1] == topbit) {
            // Here, there is a queen in position (bound1, sizee), which means
            // that this solution could be a 270-degree rotation (or a
            // 90-degree clockwise rotation). We only need to check here
            // for positions that we have counted before. After all, we cannot
            // really find a 270-degree rotation anymore, as this would
            // be a 90-degree rotation as well.
            for (int ptn = topbit >> 1, own = 1; own <= sizee; own++, ptn >>= 1) {
                int bit = 1;

                for (int you = 0; board[you] != ptn && board[own] >= bit; you++, bit <<= 1) {
                }

                if (board[own] > bit) {
                    // We found a queen on row "sizee-row" first, which means
                    // that the queen on column "row" is higher up, which means
                    // that we have counted this position before.
                    return 0;
                }
                if (board[own] < bit) {
                    // Definitely a new position.
                    break;
                }
            }
        }

        return 8;
    }

    private static final long seq_QueenCountPartial(
            final int y, final int left,
            final int right, final int mask) {

        int bitmap = mask & ~(left | right);

        if (bitmap != 0) {
            if (y != 0) {
                long lnsol = 0;
                // We're not done, so recursively compute the rest of the
                // solutions...
                do {
                    final int bit = -bitmap & bitmap;
                    bitmap ^= bit;
                    lnsol += seq_QueenCountPartial(y - 1, (left | bit) << 1,
                        (right | bit) >> 1, mask ^ bit);
                } while (bitmap != 0);

                return lnsol;
            }
            return 1;
        }
        return 0;
    }

    public long spawn_QueenCountPartial(
            final int spawnLevel, final int y, final int left,
            final int right, final int mask) {

        // Check if we've gone deep enough into the recursion to 
        // have generated a decent number of jobs. If so, stop spawning
        // and switch to a sequential algorithm...
        if (spawnLevel <= 0) {
            return seq_QueenCountPartial(y, left, right, mask);
        }

        int bitmap = mask & ~(left | right);

        int it = 0;
        long[] lnsols = new long[y+1];

        while (bitmap != 0) {
            int bit = -bitmap & bitmap;
            bitmap ^= bit;
            lnsols[it] = spawn_QueenCountPartial(spawnLevel - 1,
                y - 1, (left | bit) << 1, (right | bit) >> 1, mask ^ bit);
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

    private final long calculatePartial(final long[] results,
            int pos, final int size, int spawnLevel) {

        final int SIZEE = size - 1;
        final int MASK = (1 << size) - 1;

        pos--; // 1..half --> 0..half-1: array index and shift count.

        if (spawnLevel >= SIZEE-1 && spawnLevel > 0) {
            System.out.println("Spawnlevel set too high. Setting it to "
                    + (SIZEE-2));
            spawnLevel = SIZEE-2;
            if (spawnLevel < 0) {
                spawnLevel = 0;
            }
        }

        long start = System.currentTimeMillis();

        final int bit = 1 << pos;

        if (size == 1) {
            results[pos] = 1;
        } else {
            if (pos == 0) {
                // Queen in lower-left corner case.
                // Apparently, placing the 2nd queen on the upper border
                // does not give any solutions? Otherwise, why not include
                // SIZEE in this loop?
                long[] tempresults = new long[SIZEE];
                for (int BOUND1 = 2; BOUND1 < SIZEE; BOUND1++) {
                    int bit2 = 1 << BOUND1;
                    tempresults[BOUND1] = spawn_QueenInCorner(SIZEE-2,
                            spawnLevel-1, (2 | bit2) << 1, bit2 >> 1,
                            BOUND1-2, MASK ^ (1 | bit2));
                }
                sync();
                results[0] = 0;
                for (int i = 0; i < SIZEE; i++) {
                    results[0] += tempresults[i];
                }
                results[0] /= 4;
            } else if ((size % 2 != 0) && pos == (size / 2)) {
                // Queen in middle case.
                long[] tempresults = new long[SIZEE];
                int bit1 = 1 << pos;
                for (int BOUND1 = 0; BOUND1 < pos-1; BOUND1++) {
                    int bit2 = 1 << BOUND1;
                    tempresults[BOUND1] = spawn_QueenCountPartial(spawnLevel-1,
                            SIZEE-2, ((bit1 << 1) | bit2) << 1,
                            ((bit1 >> 1) | bit2) >> 1,
                            MASK ^ (bit1 | bit2));
                }
                sync();
                results[pos] = 0;
                for (int i = 0; i < SIZEE; i++) {
                    results[pos] += tempresults[i];
                }
                results[pos] *= 2;
            } else {
                results[pos] = spawn_QueenCountPartial(
                    spawnLevel, SIZEE-1, bit << 1, bit >> 1, MASK ^ bit);
                sync();
            }
        }


        long end = System.currentTimeMillis();

        return (end - start);
    }

    private final long calculate(final long[] results, final int size,
            final int spawnLevel) {

        int maxbound = size/2 - 1;
        final int SIZEE = size - 1;
        final int MASK = (1 << size) - 1;

        long start = System.currentTimeMillis();

        long[] tempresults = new long[size];

        // Queen in lower-left corner case.
        // Apparently, placing the 2nd queen on the upper border
        // does not give any solutions? Otherwise, why not include
        // SIZEE in this loop?
        for (int BOUND1 = 2; BOUND1 < SIZEE; BOUND1++) {

            int bit = 1 << BOUND1;
            tempresults[BOUND1] = spawn_QueenInCorner(SIZEE-2,
                    spawnLevel-1, (2 | bit) << 1, bit >> 1,
                    BOUND1-2, MASK ^ (1 | bit));
            // The "left" parameter actually is ((1 << 1) | bit) << 1.
            // Likewise, the "right" parameter is ((1 >> 1) | bit) >> 1.
        }

        for (int i = 1; i <= maxbound; i++) {
            final int BOUND1 = i;

            final int bit = 1 << BOUND1;

            int LASTMASK = (1 << SIZEE) | 1;
            final int SIDEMASK = LASTMASK;

            final int[] board = new int[size];
            board[0] = bit;

            for (int b1 = 1; b1 < BOUND1; b1++) {
                LASTMASK |= LASTMASK >> 1 | LASTMASK << 1;
            }

            results[i] = spawn_QueenNotInCorner(board, SIZEE, spawnLevel, 1,
                    bit << 1, bit >> 1, MASK ^ bit, LASTMASK, SIDEMASK,
                    BOUND1);
        }

        sync();

        if (size == 1) {
            results[0] = 1;
        } else {
            for (int j = 0; j < size; j++) {
                results[0] += tempresults[j];
            }
        }


        long end = System.currentTimeMillis();

        //printResults(results, nextResult, size, (end-start) / 1000.0);

        return (end - start);
    }

    private void printResults(long[] results, int size,
            double time) {
        // Note that for odd sizes, we don't have to try and place the
        // first queen on the middle row: these solutions are already
        // accounted for in the other searches.
        long nsol = 0;
        int maxbound = size/2 - 1;

        for (int i = 0; i <= maxbound; i++) {
            nsol += results[i];
        }

        time = time / 1000.0;

        System.out.println((new Date().toString()) + ": NQueens(" + size
                + ") = " + nsol + ", time = " + time + " s.");

        if (size < solutions.length && nsol != solutions[size]) {
            System.out.println("Application result is WRONG!");
        }
    }

    private static int readInt(StreamTokenizer d) throws IOException {
        if (d.ttype != StreamTokenizer.TT_NUMBER) {
            throw new IOException ("Format error in input");
        }
        int v = (int) d.nval;
        d.nextToken();
        return v;
    }

    private void printResultsPartial(long[] results, int pos, int size,
            double time, double totalTime) {

        int maxbound = size/2 + size%2 - 1;
        boolean size_done = true;

        time = time / 1000.0;

        System.out.println((new Date()).toString()
                + ": NQueens(" + size + ", " + pos + ") = "
                + results[pos-1] + ", time = " + time + " s.");

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
            System.out.println((new Date()).toString()
                    + ": sum NQueens(" + size + ") = " + nsol
                    + ", sum time = " + (totalTime / 1000.0) + " s.");

            if (size < solutions.length && nsol != solutions[size]) {
                System.out.println(" application result is WRONG!");
            }
        }
    }

    private void doRunPartial(StreamTokenizer d, long[][] results, int size,
            int spawnLevel, double[] times) throws IOException {

        int maxbound = size/2 + size%2;

        int pos = readInt(d);
        if (pos > maxbound) {
            System.out.println("Illegal bound: " + pos + ", ignored");
            return;
        }

        System.out.println((new Date()).toString() + ": Started NQueens("
                + size + ", " + pos + "), spawnlevel " + spawnLevel);

        if (results[size] == null) {
            results[size] = new long[maxbound];
            for (int i = 0; i < maxbound; i++) {
                results[size][i] = -1;
            }
        }

        double time = calculatePartial(results[size], pos, size, spawnLevel);

        times[size] += time;

        printResultsPartial(results[size], pos, size, time, times[size]);
    }

    private void doRun(StreamTokenizer d, long[][] partials, double[] times)
            throws IOException {
        while (d.ttype == StreamTokenizer.TT_EOL) {
            d.nextToken();
        }
        if (d.ttype == StreamTokenizer.TT_EOF) {
            return;
        }
        /* description of initial configuration */
        int size = readInt(d);
        int spawnLevel = readInt(d);

        if (d.ttype == StreamTokenizer.TT_NUMBER) {
            doRunPartial(d, partials, size, spawnLevel, times);
            return;
        }
        int maxbound = size/2 - 1;

        if (maxbound < 0) maxbound = 0;

        System.out.println((new Date()).toString() + ": Started NQueens("
                + size + "), spawnlevel " + spawnLevel);

        final long results[] = new long[maxbound+1];
        double time = calculate(results, size, spawnLevel);
        printResults(results, size, time);
    }

    private void readInput(String[] args) {
        InputStream in = System.in;
        long[][] partialResults = new long[MAXSZ+1][];
        double[] partialTimes = new double[MAXSZ+1];

        try {
            if (args.length > 0) {
                in = this.getClass().getClassLoader()
                        .getResourceAsStream(args[0]);
                if (in == null) {
                    throw new IOException("Could not open " + args[0]);
                }
            }
            StreamTokenizer d = new StreamTokenizer(new InputStreamReader(in));

            d.commentChar('#');
            d.eolIsSignificant(true);
            d.parseNumbers();

            d.nextToken();

            while (d.ttype != StreamTokenizer.TT_EOF) {
                doRun(d, partialResults, partialTimes);
            }

        } catch (Exception e) {
            System.err.println("readInput error: " + e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        if (args.length > 1) {
            System.err.println("usage: nqueens [ <filename> ]");
        } else {
            NQueens nq = new NQueens();
            nq.readInput(args);
        }
    }
}

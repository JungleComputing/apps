/* $Id$ */

import ibis.satin.Spawnable;

interface NQueensInterface extends Spawnable {

    public long spawn_QueenNotInCorner(int[] board, int N, int spawnLevel,
            int y, int left, int right, int mask, int lastmask,
            int sidemask, int bound1);
    public long spawn_QueenInCorner(int y, int spawnLevel, int left, int right,
            int bound1, int mask);
    public long spawn_QueenCountPartial(int spawnLevel, int y, int left,
            int right, int mask);
}

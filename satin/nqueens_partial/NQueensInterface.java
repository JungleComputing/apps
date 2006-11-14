/* $Id: NQueensInterface.java 3249 2005-10-19 10:09:11Z rob $ */

import ibis.satin.Spawnable;

interface NQueensInterface extends Spawnable {

    public long spawn_QueenNotInCorner(int N, int spawnLevel, int y, int left, int down, int right, int mask);
    public long spawn_QueenInCorner(int y, int spawnLevel, int left, int down, int right, int bound1, int mask, int sizee);
}

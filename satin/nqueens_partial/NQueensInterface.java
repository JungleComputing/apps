/* $Id: NQueensInterface.java 3249 2005-10-19 10:09:11Z rob $ */

import ibis.satin.Spawnable;

interface NQueensInterface extends Spawnable {

    public long spawn_QueenCount(int spawnLevel, int y, int left, int down, int right, int mask);
}

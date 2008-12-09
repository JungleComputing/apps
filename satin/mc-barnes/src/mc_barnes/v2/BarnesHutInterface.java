package mc_barnes.v2;
/* $Id: */

import ibis.satin.*;

public interface BarnesHutInterface extends Spawnable {
    
    public int BarnesSO(int numBodies, int nodeId, int iteration);
}

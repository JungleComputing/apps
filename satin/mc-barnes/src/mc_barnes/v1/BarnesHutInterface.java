package mc_barnes.v1;
/* $Id: */

import ibis.satin.*;

public interface BarnesHutInterface extends Spawnable {
    
    public BodyUpdates BarnesSO(int numBodies, int nodeId, int iteration);
}

/* $Id: */

import ibis.satin.*;

public interface BarnesHutInterface extends Spawnable {
    
    public BodyUpdates BarnesSO(int nodeId, int iteration, BodiesSO bodies);
    public BodyUpdates barnesNTC(BodyTreeNode me, BodyTreeNode tree,
            RunParameters params);
}

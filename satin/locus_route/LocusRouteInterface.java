/* $Id$ */

import ibis.satin.Spawnable;
import java.util.LinkedList;

public interface LocusRouteInterface extends Spawnable {

    public LinkedList<Wire> computeWires(LinkedList<Wire> wires, CostArray costArray);
        
}
    

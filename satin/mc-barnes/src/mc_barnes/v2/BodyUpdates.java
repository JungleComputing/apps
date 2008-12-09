package mc_barnes.v2;
import java.io.IOException;

import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

/* $Id: BodyUpdates.java 6128 2007-08-22 09:19:51Z rob $ */

/**
 * Container for collecting accellerations. This container is used for
 * collecting body updates, and for multicasting them.
 */
public abstract class BodyUpdates {
    /** Body number corresponding to the index. */
    protected int[] bodyNumbers;

    /** Current fill index. */
    protected int index;
  
    int iteration;

    /**
     * Constructor.
     * @param sz the initial size of the accelleration arrays.
     */
    public BodyUpdates(int sz) {
        bodyNumbers = new int[sz];
        index = 0;
    }

    /**
     * Adds the specified accelerations for the specified body number.
     * @param bodyno the body number.
     * @param x the acceleration in the X direction.
     * @param y the acceleration in the Y direction.
     * @param z the acceleration in the Z direction.
     */
    public abstract void addAccels(int bodyno, double x, double y, double z);

    /**
     * Applies the updates to the bodies in the specified array.
     * @param bodyArray the bodies
     * @param iteration the current iteration number
     * @param params the run parameters.
     */
    public abstract void updateBodies(Body[] bodyArray, RunParameters params);
    
    public abstract void readUpdates(ReadMessage m) throws IOException;
    
    public abstract void writeUpdates(WriteMessage m) throws IOException;
}

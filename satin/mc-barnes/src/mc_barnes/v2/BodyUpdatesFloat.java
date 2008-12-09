package mc_barnes.v2;
import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

import java.io.IOException;

/* $Id: BodyUpdatesFloat.java 6128 2007-08-22 09:19:51Z rob $ */

/**
 * Container for collecting accellerations. This container is used for
 * job results, as well as for sending updates in the SO version.
 */
public final class BodyUpdatesFloat extends BodyUpdates {

    private static final long serialVersionUID = -5278667869725719221L;

    /** Acceleration in X direction. */
    private float[] acc_x;

    /** Acceleration in Y direction. */
    private float[] acc_y;

    /** Acceleration in Z direction. */
    private float[] acc_z;

    /**
     * Constructor.
     * @param sz the initial size of the accelleration arrays.
     */
    public BodyUpdatesFloat(int sz) {
        super(sz);
        acc_x = new float[sz];
        acc_y = new float[sz];
        acc_z = new float[sz];
    }

    /**
     * Grow (or shrink) to the specified size.
     * @param newsz the size to grow or shrink to.
     */
    private final void grow(int newsz) {
        if (newsz != bodyNumbers.length) {
            int[] newnums = new int[newsz];
            System.arraycopy(bodyNumbers, 0, newnums, 0, index);
            bodyNumbers = newnums;
            float[] newacc = new float[newsz];
            System.arraycopy(acc_x, 0, newacc, 0, index);
            acc_x = newacc;
            newacc = new float[newsz];
            System.arraycopy(acc_y, 0, newacc, 0, index);
            acc_y = newacc;
            newacc = new float[newsz];
            System.arraycopy(acc_z, 0, newacc, 0, index);
            acc_z = newacc;
        }
    }

    public final void addAccels(int bodyno, double x, double y, double z) {
        if (index >= bodyNumbers.length) {
            grow(2 * index + 1);
        }
        bodyNumbers[index] = bodyno;
        acc_x[index] = (float) x;
        acc_y[index] = (float) y;
        acc_z[index] = (float) z;
        index++;
    }

    public final void updateBodies(Body[] bodyArray, RunParameters params) {
        for (int i = 0; i < index; i++) {            
            bodyArray[bodyNumbers[i]].computeNewPosition(iteration != 0, (double) acc_x[i],
                    (double) acc_y[i], (double) acc_z[i], params);
        }
    }

    public void readUpdates(ReadMessage m) throws IOException {
        iteration = m.readInt();
        m.readArray(bodyNumbers, 0, index);
        m.readArray(acc_x, 0, index);
        m.readArray(acc_y, 0, index);
        m.readArray(acc_z, 0, index);
    }
    
    public void writeUpdates(WriteMessage m) throws IOException {
        m.writeInt(iteration);
        m.writeArray(bodyNumbers, 0, index);
        m.writeArray(acc_x, 0, index);
        m.writeArray(acc_y, 0, index);
        m.writeArray(acc_z, 0, index);
    }   
}

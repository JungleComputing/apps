package mc_barnes.v2;
import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

import java.io.IOException;

/* $Id: BodyUpdatesDouble.java 6128 2007-08-22 09:19:51Z rob $ */

/**
 * Container for collecting accellerations. This container is used for
 * job results, as well as for sending updates in the SO version.
 */
public final class BodyUpdatesDouble extends BodyUpdates {

    // Do not cache arrays. It saves three new operations, but
    // they waste a large amount of memory,
    // putting more pressure on the garbage collector.

    private static final long serialVersionUID = 4084153704155381935L;

    /** Acceleration in X direction. */
    private double[] acc_x;

    /** Acceleration in Y direction. */
    private double[] acc_y;

    /** Acceleration in Z direction. */
    private double[] acc_z;

    /**
     * Constructor.
     * @param sz the initial size of the accelleration arrays.
     */
    public BodyUpdatesDouble(int sz) {
        super(sz);
        acc_x = new double[sz];
        acc_y = new double[sz];
        acc_z = new double[sz];
    }

    private final void grow(int newsz) {
        if (newsz != bodyNumbers.length) {
            int[] newnums = new int[newsz];
            System.arraycopy(bodyNumbers, 0, newnums, 0, index);
            bodyNumbers = newnums;
            double[] newacc = new double[newsz];
            System.arraycopy(acc_x, 0, newacc, 0, index);
            acc_x = newacc;
            newacc = new double[newsz];
            System.arraycopy(acc_y, 0, newacc, 0, index);
            acc_y = newacc;
            newacc = new double[newsz];
            System.arraycopy(acc_z, 0, newacc, 0, index);
            acc_z = newacc;
        }
    }

    public final void addAccels(int bodyno, double x, double y, double z) {
        if (index >= bodyNumbers.length) {
            grow(2 * index + 1);
        }
        bodyNumbers[index] = bodyno;
        acc_x[index] = x;
        acc_y[index] = y;
        acc_z[index] = z;
        index++;
    }

    public final void updateBodies(Body[] bodyArray, RunParameters params) {
        for (int i = 0; i < index; i++) {
            int j = bodyNumbers[i];
            bodyArray[j].computeNewPosition(iteration != 0, acc_x[i], acc_y[i],
                    acc_z[i], params);
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

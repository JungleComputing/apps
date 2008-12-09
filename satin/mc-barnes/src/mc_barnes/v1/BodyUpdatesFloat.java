package mc_barnes.v1;
import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

import java.io.IOException;

import mcast.ht.storage.Piece;
import mcast.ht.storage.PieceFactory;

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
    protected final void grow(int newsz) {
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
            System.out.println("Should not happen 1");
            grow(2 * index + 1);
        }
        bodyNumbers[index] = bodyno;
        acc_x[index] = (float) x;
        acc_y[index] = (float) y;
        acc_z[index] = (float) z;
        index++;
    }

    /**
     * Adds the specified updates, preparing for an update round.
     * @param r the specified updates.
     */
    private void addUpdates(BodyUpdatesFloat r) {
        for (int i = 0; i < r.index; i++) {
            int ix = r.bodyNumbers[i];
            acc_x[ix] = r.acc_x[i];
            acc_y[ix] = r.acc_y[i];
            acc_z[ix] = r.acc_z[i];
        }
        if (r.more != null) {
            for (int i = 0; i < r.more.length; i++) {
                addUpdates((BodyUpdatesFloat) r.more[i]);
            }
            r.more = null;
        }
    }

    public final void prepareForUpdate() {
        int sz = computeSize();
        float[] acc_x_tmp = new float[sz];
        float[] acc_y_tmp = new float[sz];
        float[] acc_z_tmp = new float[sz];

        for (int i = 0; i < index; i++) {
            int ix = bodyNumbers[i];
            acc_x_tmp[ix] = acc_x[i];
            acc_y_tmp[ix] = acc_y[i];
            acc_z_tmp[ix] = acc_z[i];
        }
        bodyNumbers = null;
        acc_x = acc_x_tmp;
        acc_y = acc_y_tmp;
        acc_z = acc_z_tmp;
        if (more != null) {
            for (int i = 0; i < more.length; i++) {
                addUpdates((BodyUpdatesFloat) more[i]);
            }
            more = null;
        }
    }

    public final void updateBodies(Body[] bodyArray, RunParameters params) {
        for (int i = 0; i < bodyArray.length; i++) {
            bodyArray[i].computeNewPosition(iteration != 0, (double) acc_x[i],
                    (double) acc_y[i], (double) acc_z[i], params);
        }
    }

    public void close() throws IOException {
        // nothing       
    }

    public Piece createPiece(int index) throws IOException {
        return PieceFactory.createPiece(index);
    }

    public int getPieceCount() {
        return BarnesHut.bodies.getPieceCount();
    }

    public Piece readPiece(ReadMessage m) throws IOException {
        int index = m.readInt();
        // System.out.println("Reading piece " + index);
        int begin = index * BodiesSO.CHUNKSIZE;
        int end = begin + BodiesSO.CHUNKSIZE;
        if (end > acc_x.length) {
            end = acc_x.length;
        }
        if (index == 0) {
            iteration = m.readInt();
            BarnesHut.bodies.cleanup();
        }
        m.readArray(acc_x, begin, end - begin);
        m.readArray(acc_y, begin, end - begin);
        m.readArray(acc_z, begin, end - begin);
        return PieceFactory.createPiece(index);
    }

    public void writePiece(Piece piece, WriteMessage m) throws IOException {
        int index = piece.getIndex();
        // System.out.println("Writing piece " + index);
        m.writeInt(index);
        int begin = index * BodiesSO.CHUNKSIZE;
        int end = begin + BodiesSO.CHUNKSIZE;
        if (end > acc_x.length) {
            end = acc_x.length;
        }
        if (index == 0) {
            m.writeInt(iteration);
        }
        m.writeArray(acc_x, begin, end - begin);
        m.writeArray(acc_y, begin, end - begin);
        m.writeArray(acc_z, begin, end - begin);   
    }
}

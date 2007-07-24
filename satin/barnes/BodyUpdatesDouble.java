/* $Id$ */

/**
 * Container for collecting accellerations. This container is used for
 * job results, as well as for sending updates in the SO version.
 */
public final class BodyUpdatesDouble extends BodyUpdates {

    // Do not cache arrays. It saves three new operations, but
    // they waste a large amount of memory,
    // putting more pressure on the garbage collector.
    
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

    protected final void grow(int newsz) {
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
            System.out.println("Should not happen 1");
            grow(2 * index + 1);
        }
        bodyNumbers[index] = bodyno;
        acc_x[index] = x;
        acc_y[index] = y;
        acc_z[index] = z;
        index++;
    }

    protected final void optimize(BodyUpdates bb) {
        BodyUpdatesDouble b = (BodyUpdatesDouble) bb;
        System.arraycopy(b.bodyNumbers, 0, bodyNumbers, index, b.index);
        System.arraycopy(b.acc_x, 0, acc_x, index, b.index);
        System.arraycopy(b.acc_y, 0, acc_y, index, b.index);
        System.arraycopy(b.acc_z, 0, acc_z, index, b.index);
        index += b.index;
        if (b.more != null) {
            for (int i = 0; i < b.more.length; i++) {
                optimize(b.more[i]);
            }
        }
    }

    /**
     * Adds the specified updates, preparing for an update round.
     * @param r the specified updates.
     */
    private void addUpdates(BodyUpdatesDouble r) {
        for (int i = 0; i < r.index; i++) {
            int ix = r.bodyNumbers[i];
            acc_x[ix] = r.acc_x[i];
            acc_y[ix] = r.acc_y[i];
            acc_z[ix] = r.acc_z[i];
        }
        if (r.more != null) {
            for (int i = 0; i < r.more.length; i++) {
                addUpdates((BodyUpdatesDouble) r.more[i]);
            }
            r.more = null;
        }
    }

    public final void prepareForUpdate() {
        int sz = computeSz();
        double[] acc_x_tmp = new double[sz];
        double[] acc_y_tmp = new double[sz];
        double[] acc_z_tmp = new double[sz];

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
                addUpdates((BodyUpdatesDouble) more[i]);
            }
            more = null;
        }
    }

    public final void updateBodies(Body[] bodyArray, int iteration,
            RunParameters params) {
        for (int i = 0; i < bodyArray.length; i++) {
            bodyArray[i].computeNewPosition(iteration != 0, acc_x[i], acc_y[i],
                    acc_z[i], params);
        }
    }
}

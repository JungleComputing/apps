package mc_barnes.v1;
import mcast.ht.storage.Storage;

/* $Id: BodyUpdates.java 6128 2007-08-22 09:19:51Z rob $ */

/**
 * Container for collecting accellerations. This container is used for
 * job results, as well as for sending updates in the SO version.
 */
public abstract class BodyUpdates implements java.io.Serializable, Storage {
    /** Body number corresponding to the index. */
    protected int[] bodyNumbers;

    /** Current fill index. */
    protected int index;

    /** For combining BodyUpdate containers. */
    protected BodyUpdates[] more;
    
    transient int iteration;

    /**
     * Constructor.
     * @param sz the initial size of the accelleration arrays.
     */
    public BodyUpdates(int sz) {
        bodyNumbers = new int[sz];
        index = 0;
    }

    /**
     * Grow (or shrink) to the specified size.
     * @param newsz the size to grow or shrink to.
     */
    protected abstract void grow(int newsz);

    /**
     * Adds the specified accelerations for the specified body number.
     * @param bodyno the body number.
     * @param x the acceleration in the X direction.
     * @param y the acceleration in the Y direction.
     * @param z the acceleration in the Z direction.
     */
    public abstract void addAccels(int bodyno, double x, double y, double z);

    /**
     * Computes the number of bodies for which this BodyUpdates structure
     * (and its nested structures) has updates.
     * @return the number of updates.
     */
    protected final int computeSize() {
        int size = index;
        if (more != null) {
            for (int i = 0; i < more.length; i++) {
                size += more[i].computeSize();
            }
        }
        return size;
    }

    /**
     * Combines the specified updates into this one and returns the result.
     * Assumes that the current BodyUpdates has no <code>more</code> array
     * yet.
     * @param v the updates to combine into this one.
     * @return the result.
     */
    public BodyUpdates combineResults(BodyUpdates[] v) {
        if (more != null) {
            throw new Error("Oops: something wrong here.");
        }
        more = v;
        return this;
    }

    /**
     * Prepares for an update round. It changes the order in the acceleration
     * arrays to the body order, and removes the bodyNumbers array, as it is
     * no longer needed, and this saves on serialization and sending time
     * when the BodyUpdate gets broadcasted.
     */
    public abstract void prepareForUpdate();

    /**
     * Applies the updates to the bodies in the specified array.
     * @param bodyArray the bodies
     * @param iteration the current iteration number
     * @param params the run parameters.
     */
    public abstract void updateBodies(Body[] bodyArray, RunParameters params);
}

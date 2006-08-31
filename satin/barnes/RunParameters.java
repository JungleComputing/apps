/* $Id$ */
/**
 * Container class for some parameters that influence the run.
 */
public final class RunParameters implements java.io.Serializable {
    /** Cell subdivision tolerance. */
    final double THETA;

    /** Integration time-step. */
    final double DT;

    /** Half of the integration time-step. */
    final double DT_HALF;

    /** Potential softening value. */
    final double SOFT;

    /** Potential softening value squared. */
    final double SOFT_SQ;

    /** Maximum bumber of bodies per leaf. */
    final int MAX_BODIES_PER_LEAF;

    /** Spawn threshold. */
    final int SPAWN_MIN_THRESHOLD;

    /** Whether to use double or float for accelerations. */
    final boolean USE_DOUBLE_UPDATES;

    /**
     * @param theta
     * @param dt
     * @param soft
     * @param maxBodiesPerLeaf
     * @param threshold
     * @param useDoubleUpdates
     */
    public RunParameters(double theta, double dt, double soft, int maxBodiesPerLeaf, int threshold, boolean useDoubleUpdates) {
        THETA = theta;
        DT = dt;
        DT_HALF = dt / 2.0;
        SOFT = soft;
        SOFT_SQ = SOFT * SOFT;
        MAX_BODIES_PER_LEAF = maxBodiesPerLeaf;
        SPAWN_MIN_THRESHOLD = threshold;
        USE_DOUBLE_UPDATES = useDoubleUpdates;
    }
}

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
    final int THRESHOLD;

    /** Wether to use double or float for accelerations. */
    final boolean USE_DOUBLE_UPDATES;

    /** the start time of the simulation */
    final double START_TIME;

    /** the end time of the simulation */
    final double END_TIME;

    /** the number of iterations of the simulation */
    final int ITERATIONS;


    // don't allow uninitialized construction, not used
    private RunParameters() {
        throw new Error("internal error");
    }

    public RunParameters(double theta, double dt, double soft,
        int max_bodies_per_leaf, int threshold, boolean useDoubleUpdates,
        double start_time, double end_time, int iterations) {
        THETA = theta;
        DT = dt;
        DT_HALF = DT / 2.0;
        SOFT = soft;
        SOFT_SQ = SOFT * SOFT;
        MAX_BODIES_PER_LEAF = max_bodies_per_leaf;
        THRESHOLD = threshold;
        USE_DOUBLE_UPDATES = useDoubleUpdates;
        START_TIME = start_time;
        END_TIME = end_time;
        
        if (iterations == -1) {
            ITERATIONS = (int) ((END_TIME + 0.1 * DT - START_TIME) / DT);
        } else {
            ITERATIONS = iterations;
        }
    }
}

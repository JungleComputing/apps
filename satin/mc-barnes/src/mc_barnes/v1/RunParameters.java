package mc_barnes.v1;
import java.io.IOException;

import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

/* $Id: RunParameters.java 6217 2007-09-03 12:49:44Z rob $ */
/**
 * Container class for some parameters that influence the run.
 */
public final class RunParameters {
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
    @SuppressWarnings("unused")
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
    
    public void write(WriteMessage m) throws IOException {
        m.writeDouble(THETA);
        m.writeDouble(DT);
        m.writeDouble(DT_HALF);
        m.writeDouble(SOFT);
        m.writeDouble(SOFT_SQ);
        m.writeInt(MAX_BODIES_PER_LEAF);
        m.writeInt(THRESHOLD);
        m.writeBoolean(USE_DOUBLE_UPDATES);
        m.writeDouble(START_TIME);
        m.writeDouble(END_TIME);
        m.writeInt(ITERATIONS);
    }
    
    public RunParameters(ReadMessage m) throws IOException {
        THETA = m.readDouble();
        DT = m.readDouble();
        DT_HALF = m.readDouble();
        SOFT = m.readDouble();
        SOFT_SQ = m.readDouble();
        MAX_BODIES_PER_LEAF = m.readInt();
        THRESHOLD = m.readInt();
        USE_DOUBLE_UPDATES = m.readBoolean();
        START_TIME = m.readDouble();
        END_TIME = m.readDouble();
        ITERATIONS = m.readInt();
    }
}

/* $Id$ */

import java.io.*;

strictfp public final class Body implements Cloneable, Comparable, Serializable {

    public int number;

    public double pos_x, pos_y, pos_z;

    public double mass;

    // These are only used by calculateNewPosition. They are transient
    // because the NTC version only calculates the new positions on the
    // master node. The SO version needs to send them separately.
    transient public double vel_x, vel_y, vel_z;

    transient public double oldAcc_x, oldAcc_y, oldAcc_z;

    // transient public boolean updated = false; //used for debugging

    void initialize() {
        mass = 1.0;
        number = 0;
    }

    Body() {
        initialize();
    }

    //used for sorting bodies, they're sorted using the 'number' field
    public int compareTo(Object o) {
        Body other = (Body) o;

        return this.number - other.number;
    }

    //copied from the rmi implementation
    public void computeNewPosition(boolean useOldAcc,
            double acc_x, double acc_y, double acc_z, RunParameters params) {
        if (useOldAcc) { // always true, except for first iteration
            vel_x += (acc_x - oldAcc_x) * params.DT_HALF;
            vel_y += (acc_y - oldAcc_y) * params.DT_HALF;
            vel_z += (acc_z - oldAcc_z) * params.DT_HALF;
        }

        pos_x += (acc_x * params.DT_HALF + vel_x) * params.DT;
        pos_y += (acc_y * params.DT_HALF + vel_y) * params.DT;
        pos_z += (acc_z * params.DT_HALF + vel_z) * params.DT;

        vel_x += acc_x * params.DT;
        vel_y += acc_y * params.DT;
        vel_z += acc_z * params.DT;

        //prepare for next call of BodyTreeNode.barnes()
        oldAcc_x = acc_x;
        oldAcc_y = acc_y;
        oldAcc_z = acc_z;
    }

    public String toString() {
        return "pos: (" + pos_x + ", " + pos_y + ", " + pos_z + "), vel: ("
            + vel_x + ", " + vel_y + ", " + vel_z + ")";
    }
}

/* $Id: */

import ibis.satin.SharedObject;

final public class BodiesSO extends SharedObject implements BodiesInterface, BodiesSOInterface, java.io.Serializable {

    Body[] bodyArray;

    transient BodyTreeNode bodyTreeRoot;

    RunParameters params;

    int iteration = -1;

    public BodiesSO(Body[] bodyArray, RunParameters params) {
        this.bodyArray = bodyArray;
        this.params = params;
        bodyTreeRoot = new BodyTreeNode(bodyArray, params);
        bodyTreeRoot.computeCentersOfMass();
    }

    // write method 
    public void updateBodies(BodyUpdates b, int iteration) {
        // when a node joins while node 0 broadcasts the update,
        // and the node obtains an already updated object while it has
        // not received the update yet, when it receives the update, it will
        // update again, which is wrong.
        if (iteration == this.iteration+1) {
            updateBodiesLocally(b, iteration);
        }
    }

    public void updateBodiesLocally(BodyUpdates b, int iteration) {
        b.updateBodies(bodyArray, iteration, params);

	bodyTreeRoot = new BodyTreeNode(bodyArray, params);
	bodyTreeRoot.computeCentersOfMass();
        this.iteration = iteration;
        // System.out.println("Body 0 updated after iteration " + iteration
        //         + ": " + bodyArray[0]);
    }    

    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        
        in.defaultReadObject();

        // Also read velocities and old accellerations when needed.
        // They are transient in Body.java, because the NTC version does
        // not need them on the other nodes. The SO version does, however,
        // because it updates by shipping accellerations.
        for (int i = 0; i < bodyArray.length; i++) {
            bodyArray[i].vel_x = in.readDouble();
            bodyArray[i].vel_y = in.readDouble();
            bodyArray[i].vel_z = in.readDouble();
        }
        // System.out.println("Read body 0 after iteration " + iteration
        //         + ": " + bodyArray[0]);
        if (iteration >= 0) {
            // System.out.println("Reading accellerations");
            for (int i = 0; i < bodyArray.length; i++) {
                bodyArray[i].oldAcc_x = in.readDouble();
                bodyArray[i].oldAcc_y = in.readDouble();
                bodyArray[i].oldAcc_z = in.readDouble();
            }
        }
        
        bodyTreeRoot = new BodyTreeNode(bodyArray, params);
        bodyTreeRoot.computeCentersOfMass();
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws java.io.IOException {
        
        out.defaultWriteObject();
        // System.out.println("Wrote body 0 after iteration " + iteration
        //         + ": " + bodyArray[0]);
        // Also write velocities and old accellerations when needed.
        // They are transient in Body.java, because the NTC version does
        // not need them on the other nodes. The SO version does, however,
        // because it updates by shipping accellerations.
        for (int i = 0; i < bodyArray.length; i++) {
            out.writeDouble(bodyArray[i].vel_x);
            out.writeDouble(bodyArray[i].vel_y);
            out.writeDouble(bodyArray[i].vel_z);
        }
        if (iteration >= 0) {
            // System.out.println("Writing accellerations");
            for (int i = 0; i < bodyArray.length; i++) {
                out.writeDouble(bodyArray[i].oldAcc_x);
                out.writeDouble(bodyArray[i].oldAcc_y);
                out.writeDouble(bodyArray[i].oldAcc_z);
            }
        }
    }

    public BodyTreeNode getRoot() {
        return bodyTreeRoot;
    }
    
    public void cleanup() {
        bodyTreeRoot.cleanup(); // clean the static cache of node IDs
        bodyTreeRoot = null; // allow the gc to throw away the entire tree
    }
}

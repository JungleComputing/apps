package mc_barnes.v2;
import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

import java.io.IOException;

import mcast.ht.storage.Piece;
import mcast.ht.storage.PieceFactory;
import mcast.ht.storage.Storage;

final public class BodiesSO implements Storage {

    Body[] bodyArray;

    private BodyTreeNode bodyTreeRoot;

    private RunParameters params;

    int iteration = -1;
    
    static final int CHUNKSIZE = 1024;
    
    int len = -1;

    public void init(Body[] bodyArray, RunParameters params) {
        this.bodyArray = bodyArray;
        this.params = params;
        computeRoot(-1);
    }
    
    public synchronized void waitForRoot(int iter) {
        while (iteration < iter || bodyTreeRoot == null) {
            try {
                wait();
            } catch(Throwable e) {
                // ignored
            }
        }
    }
   
    public synchronized void computeRoot(int iter) {
        bodyTreeRoot = new BodyTreeNode(bodyArray, params);
        bodyTreeRoot.computeCentersOfMass();
        iteration = iter;
//         System.out.println("Computed root for iteration " + iter);
        notifyAll();
    }
    
    public BodiesSO() {        
    }

    public BodyTreeNode getRoot(int iter) {
        waitForRoot(iter);
        return bodyTreeRoot;
    }
    
    public synchronized void cleanup() {
        BodyTreeNode.cleanup(); // clean the static cache of node IDs
        bodyTreeRoot = null; // allow the gc to throw away the entire tree
    }

    /**
     * @return the params
     */
    public RunParameters getParams() {
        return params;
    }

    public void close() throws IOException {
        // nothing        
    }

    public Piece createPiece(int index) throws IOException {
        return PieceFactory.createPiece(index);
    }

    public int getPieceCount() {
        int retval;
        if (len != 0 && len % CHUNKSIZE == 0) {
            retval = len / CHUNKSIZE;
        } else {
            retval = len / CHUNKSIZE + 1;
        }
        // System.out.println("getPieceCount: " + retval);
        return retval;
    }

    public Piece readPiece(ReadMessage m) throws IOException {
        int index = m.readInt();
        // System.out.println("Reading piece " + index);
        if (index == 0) {
            params = new RunParameters(m);
        }
        synchronized(this) {
            if (bodyArray == null) {
                bodyArray = new Body[len];
            }
        }
        int begin = index * CHUNKSIZE;
        int end = begin + CHUNKSIZE;
        if (end > bodyArray.length) {
            end = bodyArray.length;
        }
        
        for (int i = begin; i < end; i++) {
            Body b = new Body();
            bodyArray[i] = b;
            b.number = m.readInt();
            b.pos_x = m.readDouble();
            b.pos_y = m.readDouble();
            b.pos_z = m.readDouble();
            b.mass = m.readDouble();
            b.vel_x = m.readDouble();
            b.vel_y = m.readDouble();
            b.vel_z = m.readDouble();           
        }

        return PieceFactory.createPiece(index);
    }

    public void writePiece(Piece piece, WriteMessage m) throws IOException {
        int index = piece.getIndex();
        // System.out.println("Writing piece " + index);
        m.writeInt(index);
        if (index == 0) {
            params.write(m);
        }
        int begin = index * CHUNKSIZE;
        int end = begin + CHUNKSIZE;
        if (end > bodyArray.length) {
            end = bodyArray.length;
        }
        for (int i = begin; i < end; i++) {
            Body b = bodyArray[i];
            m.writeInt(b.number);
            m.writeDouble(b.pos_x);
            m.writeDouble(b.pos_y);
            m.writeDouble(b.pos_z);
            m.writeDouble(b.mass);
            m.writeDouble(b.vel_x);
            m.writeDouble(b.vel_y);
            m.writeDouble(b.vel_z);
        }       
    }
}

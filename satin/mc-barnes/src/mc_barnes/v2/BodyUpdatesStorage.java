package mc_barnes.v2;

import ibis.ipl.ReadMessage;
import ibis.ipl.WriteMessage;

import java.io.IOException;

import mcast.ht.storage.Piece;
import mcast.ht.storage.PieceFactory;
import mcast.ht.storage.Storage;

public class BodyUpdatesStorage implements Storage {
    
    private BodyUpdates[] bodyUpdates;
    private final ClusterInfo clusterInfo;
    private final int clusterNo;
    private final Updater updater;
    
    public BodyUpdatesStorage(ClusterInfo info, int clusterNo, Updater updater) {
        clusterInfo = info;
        this.clusterNo = clusterNo;
        this.updater = updater;
        init();
    }
    
    public void init() {
        bodyUpdates = new BodyUpdates[clusterInfo.clusterSize(clusterNo)];
    }
    
    public void setMyUpdate(BodyUpdates u) {
        bodyUpdates[clusterInfo.myIndexInCluster()] = u;
        updater.addUpdate(u);
    }

    public void close() throws IOException {
        // nothing
    }

    public Piece createPiece(int index) throws IOException {
        return PieceFactory.createPiece(index);
    }

    public int getPieceCount() {
        return clusterInfo.clusterSize(clusterNo);
    }

    public Piece readPiece(ReadMessage m) throws IOException {
        int index = m.readInt();
        int sz = m.readInt();
        BodyUpdates u = BarnesHut.getBodyUpdates(sz, BarnesHut.bodies.getParams());
        u.index = sz;
        u.readUpdates(m);
        bodyUpdates[index] = u;
        updater.addUpdate(u);
        return createPiece(index);
    }

    public void writePiece(Piece piece, WriteMessage m) throws IOException {
        int index = piece.getIndex();
        BodyUpdates u = bodyUpdates[index];
        m.writeInt(index);
        m.writeInt(u.index);
        u.writeUpdates(m);
    }

}

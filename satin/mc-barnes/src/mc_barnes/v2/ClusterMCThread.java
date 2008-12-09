package mc_barnes.v2;

import java.io.IOException;

import mcast.ht.MulticastChannel;
import mcast.ht.admin.PieceIndexSet;
import mcast.ht.admin.PieceIndexSetFactory;
import mcast.ht.robber.RobberMulticastChannel;
import ibis.ipl.Ibis;
import ibis.ipl.IbisIdentifier;

public class ClusterMCThread extends Thread {
    
    private final Ibis ibis;
    private MulticastChannel ch;
    private final int clusterNo;
    private final ClusterInfo info;
    private final BodyUpdatesStorage storage;
    private int iteration = 0;
    
    public ClusterMCThread(ClusterInfo info, Ibis ibis, int clusterNo,
            IbisIdentifier[] list, Updater updater) {
        this.ibis = ibis;
        storage = new BodyUpdatesStorage(info, clusterNo, updater);
        try {
            this.ch = new RobberMulticastChannel(this.ibis, list, "RobberChannel_" + clusterNo);
        } catch (IOException e) {
            System.out.println("Could not create multicast channel for cluster " + clusterNo);
            e.printStackTrace();
            System.exit(1);
        };
        this.info = info;
        this.clusterNo = clusterNo;
        setDaemon(true);
        start();
    }
    
    public void close() {
        try {
            ch.close();
        } catch (IOException e) {
            // ignored
        }
    }

    public void run() {
        for (;;) {
            synchronized(BarnesHut.bodies) {
                while (BarnesHut.bodies.iteration < iteration) {
                    try {
                        BarnesHut.bodies.wait();
                    } catch(Throwable e) {
                        // ignored
                    }
                }
            }
            broadcast();
            iteration++;
            if (iteration == BarnesHut.bodies.getParams().ITERATIONS) {
                close();
                return;
            }
        }       
    }
    
    public void broadcast() {
        PieceIndexSet pieces = PieceIndexSetFactory.createEmptyPieceIndexSet();
        if (info.myClusterNo() == clusterNo) {
            pieces.add(info.myIndexInCluster());
            BarnesHut.result.iteration = iteration;
            storage.setMyUpdate(BarnesHut.result);
            BarnesHut.result = BarnesHut.getBodyUpdates(1024, BarnesHut.bodies.getParams());
        }
        try {
            ch.multicastStorage(storage, null, pieces);
        } catch (IOException e) {
            System.out.println("broadcast failed");
            e.printStackTrace();
            System.exit(1);
        }
        try {
            ch.flush();
        } catch (IOException e) {
            System.out.println("flush failed");
            e.printStackTrace();
            System.exit(1);
        }
        storage.init();
    }
}

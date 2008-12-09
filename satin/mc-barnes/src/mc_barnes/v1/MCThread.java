package mc_barnes.v1;
import java.io.IOException;
import java.util.HashSet;

import mcast.ht.MulticastChannel;
import mcast.ht.storage.IntegerStorage;

import ibis.ipl.Ibis;
import ibis.ipl.IbisIdentifier;

public class MCThread extends Thread {
    
    private final Ibis ibis;
    private IbisIdentifier master;
    private boolean isMaster;
    private final MulticastChannel ch;
    
    public MCThread(Ibis ibis, MulticastChannel ch) {
        this.ibis = ibis;
        this.ch = ch;
        setDaemon(true);
        start();
    }

    public void run() {
        try {
            master = ibis.registry().getElectionResult("master");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Could not obtain master");
            System.exit(1);
        }
        isMaster = master.equals(ibis.identifier());
        if (isMaster) {
            return;
        }

        try {
            multicastTree();
        } catch (IOException e) {
            System.out.println("Got exception in multicastTree!");
            e.printStackTrace();
            System.exit(1);
        }
        
        for (;;) {
            try {
                multicastUpdates(BarnesHut.updates);
            } catch (IOException e) {
                System.out.println("Got exception in multicastUpdates!");
                e.printStackTrace();
                System.exit(1);
            }

            if (BarnesHut.updates.iteration == BarnesHut.bodies.getParams().ITERATIONS - 2) {
                close();
                return;
            }
        }
    }
    
    public void close() {
        try {
            ch.close();
        } catch (IOException e) {
            // ignored
        }
        try {
            ibis.end();
        } catch (IOException e) {
            // ignored
        }
    }
    
    public void multicastTree() throws IOException {
        HashSet<IbisIdentifier> root = new HashSet<IbisIdentifier>();
        root.add(master);
        if (! isMaster) {
            BarnesHut.bodies = new BodiesSO();
        }
        IntegerStorage is = new IntegerStorage(isMaster ? BarnesHut.bodyArray.length : 0);
        ch.multicastStorage(is, root);
        BarnesHut.bodies.len = is.getValue();
        ch.multicastStorage(BarnesHut.bodies, root);
        if (! isMaster) {
            BarnesHut.updates = BarnesHut.getBodyUpdates(BarnesHut.bodies.len,
                    BarnesHut.bodies.getParams());
            BarnesHut.bodyArray = BarnesHut.bodies.bodyArray;
            BarnesHut.params = BarnesHut.bodies.getParams();
            BarnesHut.bodies.computeRoot(-1);
        }
        ch.flush();     // make sure that we can write the data        
    }
    
    public void multicastUpdates(BodyUpdates updates) throws IOException {
        HashSet<IbisIdentifier> root = new HashSet<IbisIdentifier>();
        root.add(master);
        ch.multicastStorage(updates, root);
        updates.updateBodies(BarnesHut.bodyArray, BarnesHut.bodies.getParams());
        BarnesHut.bodies.computeRoot(updates.iteration);
        ch.flush();
    }

}

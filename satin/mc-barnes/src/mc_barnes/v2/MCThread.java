package mc_barnes.v2;
import java.io.IOException;
import java.util.HashSet;

import mcast.ht.MulticastChannel;
import mcast.ht.robber.RobberMulticastChannel;
import mcast.ht.storage.IntegerStorage;

import ibis.ipl.Ibis;
import ibis.ipl.IbisIdentifier;

// Object used to handle the tree broadcast in slaves, and to handle the iteration broadcasts
// in slaves.
public class MCThread extends Thread {
    
    private final Ibis ibis;
    private IbisIdentifier master;
    private boolean isMaster;
    private MulticastChannel ch;
    
    public MCThread(Ibis ibis, IbisIdentifier[] list) {
        this.ibis = ibis;
        try {
            ch = new RobberMulticastChannel(ibis, list, "RobberChannel");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
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
                multicastIteration(0);
            } catch (IOException e) {
                System.out.println("Got exception in multicastUpdates!");
                e.printStackTrace();
                System.exit(1);
            }
            if (BarnesHut.bodies.iteration == BarnesHut.bodies.getParams().ITERATIONS-1) {
                close();
                return;
            }
        }
    }
    
    public void close() {
        BarnesHut.bodies.waitForRoot(BarnesHut.bodies.getParams().ITERATIONS-1);
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
        IntegerStorage is = new IntegerStorage(isMaster ? BarnesHut.bodyArray.length : 0);
        ch.multicastStorage(is, root);
        BarnesHut.bodies.len = is.getValue();
        ch.multicastStorage(BarnesHut.bodies, root);
        BarnesHut.result = BarnesHut.getBodyUpdates(1024, BarnesHut.bodies.getParams());
        if (! isMaster) {
            BarnesHut.bodyArray = BarnesHut.bodies.bodyArray;
            BarnesHut.params = BarnesHut.bodies.getParams();
            BarnesHut.bodies.computeRoot(-1);
        }
        ch.flush();     // make sure that we can write the data        
    }
    
    public void multicastIteration(int iteration) throws IOException {
        IntegerStorage is = new IntegerStorage(iteration);
        HashSet<IbisIdentifier> root = new HashSet<IbisIdentifier>();
        root.add(master);        
        ch.multicastStorage(is, root);
        ch.flush();
        BarnesHut.bodies.cleanup();
        synchronized(BarnesHut.bodies) {
            BarnesHut.bodies.iteration = is.getValue();
            BarnesHut.bodies.notifyAll();
        }
    }
}

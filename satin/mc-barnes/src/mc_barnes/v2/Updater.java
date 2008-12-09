package mc_barnes.v2;

import java.util.ArrayList;

public class Updater extends Thread {
    
    private ArrayList<BodyUpdates> todo = new ArrayList<BodyUpdates>();
    private int nHosts;
    
    public Updater(int nHosts) {
        setDaemon(true);
        this.nHosts = nHosts;
        start();
    }
    
    public void run() {
        int count = 0;
        int iter = -1;
        int bodyCount = 0;
        for (;;) {
            synchronized(this) {
                while (todo.size() == 0) {
                    try {
                        wait();
                    } catch(Throwable e) {
                        // ignored
                    }
                }
                BodyUpdates update = todo.remove(0);
//                 System.out.println("Got update, iter = " + update.iteration);
                if (iter == -1) {
                    iter = update.iteration;
                } else if (iter != update.iteration) {
                    System.out.println("Oops!");
                }
                bodyCount += update.index;
                update.updateBodies(BarnesHut.bodyArray, BarnesHut.bodies.getParams());
            }
            count++;
            if (count == nHosts) {
//                 System.out.println("Updated " + bodyCount + " bodies, computing root, iteration = " + iter);
                BarnesHut.bodies.computeRoot(iter);
                count = 0;
                bodyCount = 0;
                iter = -1;
            }
        }
    }
    
    public synchronized void addUpdate(BodyUpdates b) {
        todo.add(b);
        notifyAll();
    }
}

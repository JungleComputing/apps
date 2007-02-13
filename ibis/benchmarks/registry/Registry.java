import ibis.util.ThreadPool;
import ibis.ipl.IbisFactory;
import ibis.ipl.Ibis;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.ResizeHandler;
import ibis.ipl.StaticProperties;

public final class Registry implements Runnable {
    
    final class AppEmulator implements Runnable, ResizeHandler {
        private final long start;

        private final Ibis ibis;

        private final IbisIdentifier identifier;
        
        private final int poolSize;

        private boolean done = false;
        
        private ArrayList<Double, Double> stats;
        
        private int seen;
        
        private int step;

        AppEmulator(int estimatedTotal) throws Exception {
            start = System.currentTimeMillis();
            seen = 0;
            
            step = estimatedTotal / TOTAL_POINTS;

            StaticProperties s = new StaticProperties();
            s.add("Serialization", "ibis");

            s.add("Communication", "OneToOne, Reliable, ExplicitReceipt");
            s.add("worldmodel", "open");

            ibis = IbisFactory.createIbis(s, this);

            identifier = ibis.identifier();
            ibis.enableResizeUpcalls();
        }

        public synchronized void joined(IbisIdentifier ident) {
            seen++;
            
        }

        public void left(IbisIdentifier ident) {
            // IGNORE
        }

        public void died(IbisIdentifier corpse) {
            // IGNORE
        }

        public void mustLeave(IbisIdentifier[] ibisses) {
            // IGNORE
        }

        public synchronized void run() {
            
            while (!done) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    // IGNORE
                }
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                // IGNORE
            }
            try {
            ibis.end();
            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
        }
    }

    Registry(int threads) {
        for (int i = 0; i < threads - 1; i++) {
            ThreadPool.createNew(this, "registry stress test");
        }
        run();
    }
    
    synchronized void addTime(long time) {
        totalTime += time;
        totalTries++;
        
        System.err.println((int) (totalTime / totalTries));
    }

    public static void main(String[] args) {
        new Registry(new Integer(args[0]));
    }

    public void run() {
        while (true) {
            try {
            AppEmulator emulator = new AppEmulator();

            emulator.run();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }

        }
    }

}

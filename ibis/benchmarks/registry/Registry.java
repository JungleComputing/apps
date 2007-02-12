import ibis.util.ThreadPool;
import ibis.ipl.IbisFactory;
import ibis.ipl.Ibis;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.ResizeHandler;
import ibis.ipl.StaticProperties;

public final class Registry implements Runnable {
    
    double totalTime = 0.0;
    int totalTries = 0;

    final class AppEmulator implements Runnable, ResizeHandler {
        private final long start;

        private final Ibis ibis;

        private final IbisIdentifier identifier;

        private boolean done = false;

        AppEmulator() throws Exception {
            start = System.currentTimeMillis();

            StaticProperties s = new StaticProperties();
            s.add("Serialization", "ibis");

            s.add("Communication", "OneToOne, Reliable, ExplicitReceipt");
            s.add("worldmodel", "open");

            ibis = IbisFactory.createIbis(s, this);

            identifier = ibis.identifier();
            ibis.enableResizeUpcalls();
        }

        public void joined(IbisIdentifier ident) {
            if (ident.equals(identifier)) {
                System.err.println("got join for self after "
                        + (System.currentTimeMillis() - start)
                        + " milliseconds");
                addTime(System.currentTimeMillis() - start);
                synchronized (this) {
                    done = true;
                    notifyAll();
                }
            }
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

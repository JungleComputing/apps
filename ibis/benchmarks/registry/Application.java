import org.apache.log4j.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import ibis.ipl.Capabilities;
import ibis.ipl.Ibis;
import ibis.ipl.IbisFactory;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.ResizeHandler;
import ibis.ipl.TypedProperties;
import ibis.util.ThreadPool;

final class Application implements Runnable, ResizeHandler {

    private static final Logger logger = Logger.getLogger(Application.class);

    private final long start;

    private final int step;

    private int seen;

    private final boolean printStats;

    private Ibis ibis;

    Application(boolean printStats, int step) {
        this.printStats = printStats;
        this.step = step;

        start = System.currentTimeMillis();
        seen = 0;
        
        // register shutdown hook
        try {
            Runtime.getRuntime().addShutdownHook(new Shutdown(this));
        } catch (Exception e) {
            // IGNORE
        }
        
        try {
            Signal.handle(new Signal("USR2"), new Terminator(this));
        } catch (Exception e) {
            logger.warn("could not install handler for USR2 signal");
        }

    }

    void start() {
        ThreadPool.createNew(this, "application");
    }

    public void run() {
        try {
            
            String[] strings = {"serialization.object","communication.reliable","receive.explicit","worldmodel.Open"}; 
            
            Capabilities capabilities = new Capabilities(strings);
            TypedProperties attributes = new TypedProperties();

            logger.debug("creating ibis");
            Ibis ibis = IbisFactory.createIbis(capabilities, null, attributes, this);
            logger.debug("ibis created, enabling upcalls");

            ibis.enableResizeUpcalls();
            logger.debug("upcalls enabled");

            synchronized (this) {
                this.ibis = ibis;
            }
        } catch (Exception e) {
            logger.error("could not initialize application", e);
        }
    }

    synchronized void end() {
        if (ibis != null) {
            try {
                ibis.end();
                logger.debug("ended ibis");
            } catch (Exception e) {
                logger.error("could not end ibis", e);
            }
        }
    }

    public synchronized void joined(IbisIdentifier ident) {
        seen++;
        if (printStats && seen % step == 0) {
            double time = (System.currentTimeMillis() - start) / 1000.0;
            logger.info(time + ": seen " + seen + " joins so far");
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
    
    private static class Shutdown extends Thread {
        private final Application app;

        Shutdown(Application app) {
            this.app = app;
        }

        public void run() {
            System.err.println("shutdown hook triggered");

            app.end();
        }
    }
    
    private static class Terminator implements SignalHandler {
        private final Application app;

        Terminator(Application app) {
            this.app = app;
        }

        public void handle(Signal signal) {
            logger.debug("SIGUSR2 catched, shutting down");

            app.end();
            System.exit(0);
        }
    }
}
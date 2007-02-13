import org.apache.log4j.Logger;

import ibis.ipl.Ibis;
import ibis.ipl.IbisFactory;
import ibis.ipl.IbisIdentifier;
import ibis.ipl.ResizeHandler;
import ibis.ipl.StaticProperties;
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
    }

    void start() {
        ThreadPool.createNew(this, "application");
    }

    public void run() {
        try {
            StaticProperties s = new StaticProperties();
            s.add("Serialization", "ibis");

            s.add("Communication", "OneToOne, Reliable, ExplicitReceipt");
            s.add("worldmodel", "open");

            logger.debug("creating ibis");
            Ibis ibis = IbisFactory.createIbis(s, this);
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
}
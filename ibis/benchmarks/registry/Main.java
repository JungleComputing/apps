
import org.apache.log4j.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;


public final class Main {
 
    private static final Logger logger = Logger.getLogger(Main.class);
    
    private final Application[] apps;
    
    Main(int threads, boolean sync, int step) {
        
        apps = new Application[threads];
        for (int i = 0; i < threads ; i++) {
            logger.debug("starting thread " + i + " of " + threads);
            try {
                apps[i] = new Application(i == (threads - 1), step);
                if (sync) {
                    //wait for the ibis to be initialized
                    apps[i].run();
                } else {
                    //fork of a thread for the application to run in
                    apps[i].start();
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
    
    private void end() {
        for (int i = 0 ; i < apps.length;i++) {
            apps[i].end();
        }
    }
    

    public static void main(String[] args) {
        int threads = 1;
        int step = 1;
        boolean sync = false;
        
        for (int i = 0; i < args.length;i++) {
            if (args[i].equalsIgnoreCase("--threads")) {
                i++;
                threads = new Integer(args[i]);
            } else if (args[i].equalsIgnoreCase("--sync")) {
                sync = true;
            } else if (args[i].equalsIgnoreCase("--step")) {
                i++;
                step = new Integer(args[i]);
            } else {
                System.err.println("unknown option: " + args[i]);
                System.exit(1);
            }
        }
            
        Main main = new Main(threads, sync, step);

        // register shutdown hook
        try {
            Runtime.getRuntime().addShutdownHook(new Shutdown(main));
        } catch (Exception e) {
            // IGNORE
        }
        
        try {
            Signal.handle(new Signal("USR2"), new Terminator(main));
        } catch (Exception e) {
            logger.warn("could not install handler for USR2 signal");
        }

        
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //IGNORE
            }
        }
    }
    
    private static class Shutdown extends Thread {
        private final Main main;

        Shutdown(Main main) {
            this.main = main;
        }

        public void run() {
            System.err.println("shutdown hook triggered");

            main.end();
        }
    }
    
    private static class Terminator implements SignalHandler {
        private final Main main;

        Terminator(Main main) {
            this.main = main;
        }

        public void handle(Signal signal) {
            logger.debug("SIGUSR2 catched, shutting down");

            main.end();
            System.exit(0);
        }
    }


}

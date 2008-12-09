package mc_barnes.v1;
/* $Id: BarnesHut.java 6781 2007-11-07 12:19:39Z rob $ */

import ibis.ipl.Ibis;
import ibis.ipl.IbisCapabilities;
import ibis.ipl.IbisCreationFailedException;
import ibis.ipl.IbisFactory;
import ibis.ipl.PortType;
import ibis.satin.SatinObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import mcast.ht.MulticastChannel;
import mcast.ht.robber.RobberMulticastChannel;

/* strictfp */final class BarnesHut extends SatinObject implements
        BarnesHutInterface {

    private static final long serialVersionUID = 1L;

    static boolean remoteViz = false;

    static String dumpViz = null;

    static boolean viz = false;

    static boolean debug = false; //use -(no)debug to modify

    static boolean verbose = false; //use -v to turn on

    static final boolean ASSERTS = false; //also used in other barnes classes

    // number of bodies at which the ntc impl work sequentially
    private static int spawn_min = 500; //use -min <threshold> to modify

    private static long totalTime = 0, updateTime = 0, forceCalcTime = 0,
            vizTime = 0;

    static Body[] bodyArray;

    static transient RunParameters params;

    private static String dump_file = null;

    private static int dump_iters = 40;
    
    private static final IbisCapabilities REQ_CAPABILITIES = 
        new IbisCapabilities(IbisCapabilities.CLOSED_WORLD,
                IbisCapabilities.MEMBERSHIP_TOTALLY_ORDERED,
                IbisCapabilities.ELECTIONS_STRICT);
    private static final List<PortType> portTypes = RobberMulticastChannel.getPortTypes();
    
    static Ibis mcibis;
    static MulticastChannel ch;
    static MCThread mc;
    
    static BodiesSO bodies;
    static BodyUpdates updates; // only used on slaves.
    
    static {
        Properties props = new Properties();
        props.setProperty("ibis.pool.name", "MC_" + System.getProperty("ibis.pool.name"));
        try {
            mcibis = IbisFactory.createIbis(REQ_CAPABILITIES, props, true, null, 
                    portTypes.toArray(new PortType[portTypes.size()]));
        } catch (IbisCreationFailedException e) {
            e.printStackTrace();
            System.exit(1);
        }
        mcibis.registry().waitUntilPoolClosed();
        try {
            ch = new RobberMulticastChannel(mcibis, mcibis.registry().joinedIbises(), "RobberChannel");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        mc = new MCThread(mcibis, ch);
    }
    
    BarnesHut(int n) {
        bodyArray = new Plummer().generate(n);
    }

    BarnesHut(Reader r) throws IOException {
        BufferedReader br = new BufferedReader(r);
        StreamTokenizer tokenizer = new StreamTokenizer(br);

        tokenizer.resetSyntax();
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('.', '.');
        tokenizer.wordChars('+', '+');
        tokenizer.wordChars('E', 'E');
        tokenizer.wordChars('-', '-');
        tokenizer.eolIsSignificant(false);
        tokenizer.whitespaceChars('\t', ' ');

        int nBodies = (int) readDouble(tokenizer,
            "number expected for number of bodies");

        // Ignore number of dimensions.
        readDouble(tokenizer, "number expected for dimensions");

        double startTtime = readDouble(tokenizer,
            "number expected for start time");

        params = new RunParameters(params.THETA, params.DT, params.SOFT,
            params.MAX_BODIES_PER_LEAF, params.THRESHOLD,
            params.USE_DOUBLE_UPDATES, startTtime, params.END_TIME,
            params.ITERATIONS);

        // Allocate bodies and read mass.
        bodyArray = new Body[nBodies];
        for (int i = 0; i < nBodies; i++) {
            Body body = new Body();
            bodyArray[i] = body;
            body.number = i;
            body.mass = readDouble(tokenizer,
                "number expected for mass of body");
        }

        // Read positions
        for (int i = 0; i < nBodies; i++) {
            Body body = bodyArray[i];
            body.pos_x = readDouble(tokenizer, "x coordinate expected for body");
            body.pos_y = readDouble(tokenizer, "y coordinate expected for body");
            body.pos_z = readDouble(tokenizer, "z coordinate expected for body");
        }

        // Read velocities
        for (int i = 0; i < nBodies; i++) {
            Body body = bodyArray[i];
            body.vel_x = readDouble(tokenizer, "x velocity expected for body");
            body.vel_y = readDouble(tokenizer, "y velocity expected for body");
            body.vel_z = readDouble(tokenizer, "z velocity expected for body");
        }
    }

    private double readDouble(StreamTokenizer tk, String err)
        throws IOException {
        int tok = tk.nextToken();
        if (tok != StreamTokenizer.TT_WORD) {
            throw new IOException(err);
        }
        return Double.parseDouble(tk.sval);
    }

    private void dump(int iteration) {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(dump_file));
            w.write("" + bodyArray.length + "\n");
            w.write("3\n");
            w.write("" + (params.START_TIME + iteration * params.DT) + "\n");
            for (int i = 0; i < bodyArray.length; i++) {
                w.write("" + bodyArray[i].mass + "\n");
            }
            for (int i = 0; i < bodyArray.length; i++) {
                w.write("" + bodyArray[i].pos_x + " " + bodyArray[i].pos_y
                    + " " + bodyArray[i].pos_z + "\n");
            }
            for (int i = 0; i < bodyArray.length; i++) {
                w.write("" + bodyArray[i].vel_x + " " + bodyArray[i].vel_y
                    + " " + bodyArray[i].vel_z + "\n");
            }
            w.close();
        } catch (Exception e) {
            throw new Error(e.toString(), e);
        }
    }

    public static BodyUpdates getBodyUpdates(int n, RunParameters params) {
        if (params.USE_DOUBLE_UPDATES) {
            return new BodyUpdatesDouble(n);
        }
        return new BodyUpdatesFloat(n);
    }

    /* spawnable */
    public BodyUpdates BarnesSO(int numBodies, int nodeId, int iteration) {
        return doBarnesSO(numBodies, nodeId, iteration);
    }

    public BodyUpdates doBarnesSO(int numBodies, int nodeId, int iteration) {
        BodyTreeNode root = bodies.getRoot(iteration - 1);
        BodyTreeNode me = BodyTreeNode.getTreeNode(nodeId);

        if (me.bodyCount != numBodies) {
            System.out.println("Expected numBodies = " + numBodies
                    + ", actual = " + me.bodyCount);
            root.dump();
            for (Body body : bodyArray) {
                System.out.println(body.toString());
            }
        }

        if (me.children == null || me.bodyCount < params.THRESHOLD) {
            /* it is a leaf node, do sequential computation */
            // System.out.println("LEAF: bodycount = " + me.bodyCount);
            BodyUpdates res = getBodyUpdates(me.bodyCount, params);
            me.barnesSequential(root, res, params);
            return res;
        }

        int childcount = 0;
        for (int i = 0; i < 8; i++) {
            if (me.children[i] != null) {
                childcount++;
            }
        }
        BodyUpdates[] res = new BodyUpdates[childcount];
        childcount = 0;
        BodyUpdates result = getBodyUpdates(0, params);

        for (int i = 0; i < 8; i++) {
            BodyTreeNode ch = me.children[i];
            if (ch != null) {
                // System.out.println("SPAWN ... bodycount = " + ch.bodyCount);
                /* spawn child jobs */
                res[childcount] = /* spawn */BarnesSO(ch.bodyCount, ch.getId(), iteration);
                childcount++;
            }
        }

        sync();
        result = result.combineResults(res);
        int sz = result.computeSize();
        if (sz != me.bodyCount) {
            System.out.println("Expected result size = " + sz
                    + ", actual = " + me.bodyCount);
            root.dump();
            for (Body body : bodyArray) {
                System.out.println(body.toString());
            }
            System.exit(1);
        }

        return result;
    }

    void runSim() {
        long start = 0, end, phaseStart = 0;

        BodyUpdates result = null;

        BodyCanvas bc = null;

        RemoteVisualization rv = null;

        System.out.println("BarnesHut: simulating " + bodyArray.length
            + " bodies, " + params.MAX_BODIES_PER_LEAF + " bodies/leaf node, "
            + "theta = " + params.THETA + ", spawn-min-threshold = "
            + spawn_min);

        // print the starting problem
        if (verbose) {
            printBodies(bodyArray);
        }

        if (viz) {
            bc = BodyCanvas.visualize(bodyArray);
        }

        if (remoteViz) {
            rv = new RemoteVisualization();
        } else if (dumpViz != null) {
            try {
                rv = new RemoteVisualization(dumpViz);
            } catch (IOException e) {
                System.out.println("Warning: could not open " + dumpViz
                    + ", got " + e);
            }
        }

        // turn of Satin during sequential pars.
        ibis.satin.SatinObject.pause();

        printMemStats("pre bodies");

        bodies = new BodiesSO(bodyArray, params);
        try {
            mc.multicastTree();
        } catch (IOException e) {
            System.out.println("Oops: got exception in multicastTree");
            e.printStackTrace();
            System.exit(1);
        }

        printMemStats("post bodies");

        start = System.currentTimeMillis();

        for (int iteration = 0; iteration < params.ITERATIONS; iteration++) {
            printMemStats("begin iter " + iteration);
            
            long updateTimeTmp = 0, forceCalcTimeTmp = 0, vizTimeTmp = 0;

            // System.out.println("Starting iteration " + iteration);

            phaseStart = System.currentTimeMillis();

            //force calculation

            ibis.satin.SatinObject.resume(); //turn ON divide-and-conquer stuff

            result = doBarnesSO(bodyArray.length, 0, iteration);
            sync();
            
            ibis.satin.SatinObject.pause(); // pause divide-and-conquer stuff

            bodies.cleanup(); // throw away the tree, we only need the body array now
            // we do this to avoid out of memory problems

            printMemStats("post force " + iteration);

            forceCalcTimeTmp = System.currentTimeMillis() - phaseStart;
            forceCalcTime += forceCalcTimeTmp;

            phaseStart = System.currentTimeMillis();

            result.prepareForUpdate();
            result.iteration = iteration;

            printMemStats("post prepare for update " + iteration);

            //            System.err.println("update: " + result);

            if (iteration < params.ITERATIONS - 1) {
                try {
                    mc.multicastUpdates(result);
                } catch (IOException e) {
                    System.out.println("Oops: got exception in multicastUpdates");
                    e.printStackTrace();
                    System.exit(1);
                }
            } else {
                mc.close();
                bodies.updateBodiesLocally(result, iteration);
            }

            updateTimeTmp = System.currentTimeMillis() - phaseStart;
            updateTime += updateTimeTmp;

            printMemStats("post update " + iteration);

            phaseStart = System.currentTimeMillis();

            if (dump_file != null && iteration != 0
                && (iteration % dump_iters) == 0) {
                dump(iteration);
            }

            if (viz) {
                bc.repaint();
            }

            if (rv != null) {
                rv.showBodies(bodyArray, iteration, updateTimeTmp
                    + forceCalcTimeTmp);
            }

            vizTimeTmp = System.currentTimeMillis() - phaseStart;
            vizTime += vizTimeTmp;

            long total = updateTimeTmp + forceCalcTimeTmp + vizTimeTmp;

            System.out.println("Iteration " + iteration + " done"
                + ", update = " + updateTimeTmp + ", force = "
                + forceCalcTimeTmp + ", viz = " + vizTimeTmp + ", total = "
                + total);
        }

        end = System.currentTimeMillis();
        totalTime = end - start;
    }

    void printBodies(Body[] bodyArray) {
        Body b;
        int i;

        Body[] sorted = new Body[bodyArray.length];
        System.arraycopy(bodyArray, 0, sorted, 0, bodyArray.length);

        Arrays.sort(sorted); // sort the copied bodyArray (by bodyNumber)

        for (i = 0; i < bodyArray.length; i++) {
            b = sorted[i];
            System.out.println("0: Body " + i + ": [ " + b.pos_x + ", "
                + b.pos_y + ", " + b.pos_z + " ]");
            System.out.println("0:      " + i + ": [ " + b.vel_x + ", "
                + b.vel_y + ", " + b.vel_z + " ]");
            System.out.println("0:      " + i + ": [ " + b.oldAcc_x + ", "
                + b.oldAcc_y + ", " + b.oldAcc_z + " ]");
            System.out.println("0:      " + i + ": [ " + b.mass + " ]");
            // System.out.println("0:      " + i + ": " + b.number);
        }
    }

    void run() {
        System.out.println("Iterations: " + params.ITERATIONS + " (timings DO "
            + "include the first iteration!)");

        runSim();

        System.out.println("update took             " + updateTime / 1000.0
            + " s");
        System.out.println("Force calculation took  " + forceCalcTime / 1000.0
            + " s");
        System.out
            .println("visualization took      " + vizTime / 1000.0 + " s");
        System.out.println("application barnes took "
            + (double) (totalTime / 1000.0) + " s");

        printMemStats("done");

        if (verbose) {
            System.out.println();
            printBodies(bodyArray);
        }
    }

    public static void printMemStats(String prefix) {
        if(false) {
        Runtime r = Runtime.getRuntime();

        System.gc();
        long free = r.freeMemory() / (1024*1024);
        long max = r.maxMemory() / (1024*1024);
        long total = r.totalMemory() / (1024*1024);
        System.err.println(prefix + ": free = " + free + " max = " + max
            + " total = " + total);
        }
    }

    public static void main(String argv[]) {
        int nBodies = 0;
        boolean nBodiesSeen = false;
        boolean maxBodiesPerLeafSeen = false;
        FileReader rdr = null;

        int maxBodiesPerLeaf = 100;
        double theta = 2.0;
        double dt = 0.025;
        double soft = 0.0000025; // this value is copied from Suel, splash-2 uses 0.05
        boolean useDoubleUpdates = true;
        double startTime = 0.0;
        double endTime = 0.175;
        int iterations = -1;

        printMemStats("start");
        try {
            // We know the result, I am the master.
            // But now the other mcibisses know it as well.
            mcibis.registry().elect("master");
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(1);
        }
        
        //parse arguments
        for (int i = 0; i < argv.length; i++) {
            //options
            if (argv[i].equals("-debug")) {
                debug = true;
            } else if (argv[i].equals("-no-debug")) {
                debug = false;
            } else if (argv[i].equals("-v")) {
                verbose = true;
            } else if (argv[i].equals("-no-v")) {
                verbose = false;
            } else if (argv[i].equals("-viz")) {
                viz = true;
            } else if (argv[i].equals("-remote-viz")) {
                remoteViz = true;
            } else if (argv[i].equals("-dump-viz")) {
                dumpViz = argv[++i];
            } else if (argv[i].equals("-float")) {
                useDoubleUpdates = false;
            } else if (argv[i].equals("-double")) {
                useDoubleUpdates = true;
            } else if (argv[i].equals("-no-viz")) {
                viz = false;
            } else if (argv[i].equals("-it")) {
                iterations = Integer.parseInt(argv[++i]);
                if (iterations < 0) {
                    throw new IllegalArgumentException(
                        "Illegal argument to -it: number of iterations must be >= 0 !");
                }
            } else if (argv[i].equals("-dump-iter")) {
                dump_iters = Integer.parseInt(argv[++i]);
                if (dump_iters <= 0) {
                    throw new IllegalArgumentException(
                        "Illegal argument to -dump-iter: number of iterations must be > 0 !");
                }
            } else if (argv[i].equals("-theta")) {
                theta = Double.parseDouble(argv[++i]);
            } else if (argv[i].equals("-starttime")) {
                startTime = Double.parseDouble(argv[++i]);
            } else if (argv[i].equals("-endtime")) {
                endTime = Double.parseDouble(argv[++i]);
            } else if (argv[i].equals("-dt")) {
                dt = Double.parseDouble(argv[++i]);
            } else if (argv[i].equals("-eps")) {
                soft = Double.parseDouble(argv[++i]);
            } else if (argv[i].equals("-input")) {
                try {
                    rdr = new FileReader(argv[++i]);
                } catch (IOException e) {
                    throw new IllegalArgumentException(
                        "Could not open input file " + argv[i]);
                }
            } else if (argv[i].equals("-dump")) {
                dump_file = argv[++i];
            } else if (argv[i].equals("-min")) {
                spawn_min = Integer.parseInt(argv[++i]);
                if (spawn_min < 0) {
                    throw new IllegalArgumentException(
                        "Illegal argument to -min: Spawn min threshold must be >= 0 !");
                }
            } else if (!nBodiesSeen) {
                try {
                    nBodies = Integer.parseInt(argv[i]); //nr of bodies to
                    // simulate
                    nBodiesSeen = true;
                } catch (NumberFormatException e) {
                    System.err.println("Illegal argument: " + argv[i]);
                    System.exit(1);
                }
            } else if (!maxBodiesPerLeafSeen) {
                try {
                    maxBodiesPerLeaf = Integer.parseInt(argv[i]); //max bodies per leaf node
                    maxBodiesPerLeafSeen = true;
                } catch (NumberFormatException e) {
                    System.err.println("Illegal argument: " + argv[i]);
                    System.exit(1);
                }
            } else {
                System.err.println("Illegal argument: " + argv[i]);
                System.exit(1);
            }
        }

        if (nBodies < 1 && rdr == null) {
            System.err.println("Invalid body count, generating 3000 bodies...");
            nBodies = 3000;
        }

        params = new RunParameters(theta, dt, soft,
            maxBodiesPerLeaf, spawn_min, useDoubleUpdates, startTime, endTime,
            iterations);

        if (rdr != null) {
            if (nBodiesSeen) {
                System.out
                    .println("Warning: nBodies as seen in argument list ignored!");
            }
            try {
                new BarnesHut(rdr).run();
            } catch (IOException e) {
                throw new NumberFormatException(e.getMessage());
            }
        } else {
            new BarnesHut(nBodies).run();
        }

        ibis.satin.SatinObject.resume(); // allow satin to exit cleanly
    }
}

/* $Id$ */

import ibis.satin.SatinObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.BufferedWriter;
import java.io.FileWriter;

import java.util.Arrays;

/* strictfp */final class BarnesHut extends SatinObject implements
        BarnesHutInterface {

    static boolean remoteViz = false;

    static String dumpViz = null;

    static boolean viz = false;

    static boolean debug = false; //use -(no)debug to modify

    static boolean verbose = false; //use -v to turn on

    static final boolean ASSERTS = false; //also used in other barnes classes

    static final int IMPL_NTC = 1; // -ntc option

    static final int IMPL_SO = 2; // -so option

    static final int IMPL_SEQ = 3; // -seq option

    static final int IMPL_FULLTREE = 4; // -fulltree option

    // number of bodies at which the ntc impl work sequentially
    private static int spawn_min = 500; //use -min <threshold> to modify

    private static long totalTime = 0, updateTime = 0, forceCalcTime = 0,
            vizTime = 0;

    static Body[] bodyArray;

    private transient RunParameters params;

    private static String dump_file = null;

    private static int dump_iters = 100;

    BarnesHut(int n, RunParameters params) {
        this.params = params;
        bodyArray = new Plummer().generate(n);
    }

    BarnesHut(Reader r, RunParameters params) throws IOException {
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

        this.params = new RunParameters(params.THETA, params.DT, params.SOFT,
            params.MAX_BODIES_PER_LEAF, params.THRESHOLD,
            params.USE_DOUBLE_UPDATES, startTtime, params.END_TIME,
            params.ITERATIONS, params.IMPLEMENTATION);

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

    public BodyUpdates getBodyUpdates(int n, RunParameters params) {
        if (params.USE_DOUBLE_UPDATES) {
            return new BodyUpdatesDouble(n);
        }
        return new BodyUpdatesFloat(n);
    }

    /* guard method for the spawn below */
    public boolean guard_BarnesSO(int nodeId, int iteration, BodiesSO bodies) {
        // System.out.println("guard: iteration = " + iteration
        //         + ", bodies.iteration = " + bodies.iteration);
        return bodies.getIteration() + 1 == iteration;
    }

    /* spawnable */
    public BodyUpdates BarnesSO(int nodeId, int iteration, BodiesSO bodies) {
        return doBarnesSO(nodeId, iteration, bodies);
    }

    public BodyUpdates doBarnesSO(int nodeId, int iteration, BodiesSO bodies) {
        RunParameters params = bodies.getParams();
        BodyTreeNode me = BodyTreeNode.getTreeNode(nodeId);

        if (me.children == null || me.bodyCount < params.THRESHOLD) {
            /* it is a leaf node, do sequential computation */
            // System.out.println("LEAF: bodycount = " + me.bodyCount);
            BodyUpdates res = getBodyUpdates(me.bodyCount, params);
            // Experiment:
            // BodyTreeNode necessaryTree = new BodyTreeNode(bodies.bodyTreeRoot, me);
            // me.barnesSequential(necessaryTree, res, params);
            me.barnesSequential(bodies.getRoot(), res, params);
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
                res[childcount] = /* spawn */BarnesSO(ch.getId(), iteration,
                    bodies);
                childcount++;
            }
        }

        sync();
        return result.combineResults(res);
    }

    public BodyUpdates doBarnesNTC(BodyTreeNode me, BodyTreeNode tree,
        RunParameters params) {
        if (me.children == null || me.bodyCount < params.THRESHOLD) {
            // leaf node, let barnesSequential handle this
            BodyUpdates res = getBodyUpdates(me.bodyCount, params);
            me.barnesSequential(tree, res, params);
            return res;
        }

        int childcount = 0;
        for (int i = 0; i < 8; i++) {
            if (me.children[i] != null) {
                childcount++;
            }
        }
        BodyUpdates[] res = new BodyUpdates[childcount];
        BodyUpdates result = getBodyUpdates(0, params);
        childcount = 0;

        for (int i = 0; i < 8; i++) {
            BodyTreeNode ch = me.children[i];
            if (ch != null) {
                //necessaryTree creation
                BodyTreeNode necessaryTree =
                        (params.IMPLEMENTATION == IMPL_FULLTREE 
                            || ch == tree) ? tree : new BodyTreeNode(tree, ch);
                res[childcount] = barnesNTC(ch, necessaryTree, params); // spawn
                childcount++;
            }
        }

        sync();
        return result.combineResults(res);
    }

    /**
     * Does the same as barnesSequential, but spawnes itself until a threshold
     * is reached. Before a subjob is spawned, the necessary tree for that
     * subjob is created to be passed to the subjob.
     */
    public BodyUpdates barnesNTC(BodyTreeNode me, BodyTreeNode interactTree,
        RunParameters params) {
        return doBarnesNTC(me, interactTree, params);
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

        BodiesInterface bodies;

        printMemStats("pre bodies");
        
        if (params.IMPLEMENTATION == IMPL_SO) {
            bodies = new BodiesSO(bodyArray, params);
            ((BodiesSO) bodies).exportObject();
        } else {
            bodies = new Bodies(bodyArray, params);
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

            switch (params.IMPLEMENTATION) {
            case IMPL_NTC:
            case IMPL_FULLTREE:
                result = doBarnesNTC(bodies.getRoot(), bodies.getRoot(), params);
                break;
            case IMPL_SO:
                result = doBarnesSO(0, iteration, (BodiesSO) bodies);
                sync();
                break;
            case IMPL_SEQ:
                result = getBodyUpdates(bodies.getRoot().bodyCount, params);
                bodies.getRoot().barnesSequential(bodies.getRoot(), result,
                    params);
                break;
            }
            
            ibis.satin.SatinObject.pause(); // pause divide-and-conquer stuff

            bodies.cleanup(0); // throw away the tree, we only need the body array now
            // we do this to avoid out of memory problems

            printMemStats("post force " + iteration);

            forceCalcTimeTmp = System.currentTimeMillis() - phaseStart;
            forceCalcTime += forceCalcTimeTmp;

            phaseStart = System.currentTimeMillis();

            result.prepareForUpdate();

            printMemStats("post prepare for update " + iteration);

            //            System.err.println("update: " + result);

            if (iteration < params.ITERATIONS - 1) {
                bodies.updateBodies(result, iteration);
            } else {
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

        switch (params.IMPLEMENTATION) {
        case IMPL_NTC:
            System.out.println("Using necessary tree impl");
            break;
        case IMPL_SEQ:
            System.out.println("Using hierarchical sequential impl");
            break;
        case IMPL_SO:
            System.out.println("Using shared object impl");
            break;
        case IMPL_FULLTREE:
            System.out.println("Using full tree impl");
        default:
            System.out.println("EEK! Using unknown implementation #" + params.IMPLEMENTATION);
            System.exit(1);
            break; //blah
        }

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
        int impl = IMPL_NTC;


        printMemStats("start");
        
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
            } else if (argv[i].equals("-ntc")) {
                impl = IMPL_NTC;
            } else if (argv[i].equals("-fulltree")) {
                impl = IMPL_FULLTREE;
            } else if (argv[i].equals("-so")) {
                impl = IMPL_SO;
            } else if (argv[i].equals("-seq")) {
                impl = IMPL_SEQ;
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
            System.err.println("Invalid body count, generating 300 bodies...");
            nBodies = 3000;
        }

        RunParameters params = new RunParameters(theta, dt, soft,
            maxBodiesPerLeaf, spawn_min, useDoubleUpdates, startTime, endTime,
            iterations, impl);

        if (rdr != null) {
            if (nBodiesSeen) {
                System.out
                    .println("Warning: nBodies as seen in argument list ignored!");
            }
            try {
                new BarnesHut(rdr, params).run();
            } catch (IOException e) {
                throw new NumberFormatException(e.getMessage());
            }
        } else {
            new BarnesHut(nBodies, params).run();
        }

        ibis.satin.SatinObject.resume(); // allow satin to exit cleanly
    }
}

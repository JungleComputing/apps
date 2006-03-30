/* $Id$ */

import ibis.satin.SatinObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/* strictfp */final class BarnesHut extends SatinObject implements BarnesHutInterface {

    static boolean remoteViz = false;

    static boolean viz = false;

    static boolean debug = false; //use -(no)debug to modify

    static boolean verbose = false; //use -v to turn on

    static final boolean ASSERTS = false; //also used in other barnes classes

    static final int IMPL_NTC = 1; // -ntc option

    static final int IMPL_SO = 2; // -so option

    static final int IMPL_SEQ = 3; // -seq option

    static int impl = IMPL_NTC;

    //number of bodies at which the ntc impl work sequentially
    private static int spawn_min = 500; //use -min <threshold> to modify

    private static long totalTime = 0,
            updateTime = 0, forceCalcTime = 0, vizTime = 0;

    //Parameters for the BarnesHut algorithm / simulation
    private static final double THETA = 2.0; // cell subdivision tolerance

    private static final double DT = 0.025; // default integration time-step
    private static final double DT_HALF = DT / 2.0; // default integration time-step

    //we do 7 iterations (first one isn't measured)
    private static double START_TIME = 0.0;

    private static double END_TIME = 0.175;

    static int iterations = -1;

    static Body[] bodyArray;

    //Indicates if we are the root divide-and-conquer node
    
    static boolean I_AM_ROOT = false;

    private transient RunParameters params;

    static {
        System.setProperty("satin.so", "true"); // So that we don't forget.
    }

    BarnesHut(int n, RunParameters params) {
        I_AM_ROOT = true; //constructor is only called at the root node

        this.params = params;

        initialize(n);

        /*
         * The RMI version contained magic code equivalent to this: (the
         * DEFAULT_* variables were different)
         * 
         * double scale = Math.pow( nBodies / 16384.0, -0.25 ); DT = DEFAULT_DT *
         * scale; END_TIME = DEFAULT_END_TIME * scale; THETA = DEFAULT_THETA /
         * scale;
         * 
         * Since Rutger didn't know where it came from, and barnes from splash2
         * also doesn't include this code, I will omit it. - Maik.
         */

        if (iterations == -1) {
            iterations = (int) ((END_TIME + 0.1 * params.DT - START_TIME) / params.DT);
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

    BarnesHut(Reader r, RunParameters params) throws IOException {
        BufferedReader br = new BufferedReader(r);
        StreamTokenizer tokenizer = new StreamTokenizer(br);

        this.params = params;
        I_AM_ROOT = true; //constructor is only called at the root node

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

        START_TIME = readDouble(tokenizer, "number expected for start time");

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

        if (iterations == -1) {
            iterations = (int) ((END_TIME + 0.1 * params.DT - START_TIME) / params.DT);
        }
    }

    static void initialize(int nBodies) {
        bodyArray = new Plummer().generate(nBodies);

        //Plummer should make sure that a body with number x also has index x
        for (int i = 0; i < nBodies; i++) {
            if (ASSERTS && bodyArray[i].number != i) {
                System.err.println("EEK! Plummer generated an "
                    + "inconsistent body number");
                System.exit(1);
            }
        }
    }

    public boolean guard_doBarnesSO(byte[] nodeId, int iteration,
				       int threshold, BodiesSO bodies) {
        // System.out.println("guard: iteration = " + iteration
        //         + ", bodies.iteration = " + bodies.iteration);
        return bodies.iteration+1 == iteration;
    }
    

    /*spawnable*/
    public ArrayList doBarnesSO(byte[] nodeId, int iteration,
				    int threshold, BodiesSO bodies) {
	int lastValidChild = -1;
	ArrayList result, result1;
	BodyTreeNode treeNode; 
	boolean spawned = false;

        treeNode = bodies.findTreeNode(nodeId);

	if (treeNode.children == null || treeNode.bodyCount < threshold) {
	    /*it is a leaf node, do sequential computation*/
            ArrayList res = new ArrayList();
	    treeNode.barnesSequential(bodies.bodyTreeRoot, res, bodies.params);
            return res;
	} 

	ArrayList res[] = new ArrayList[8];
	for (int i = 0; i < 8; i++) {
	    BodyTreeNode ch = treeNode.children[i];
	    if (ch != null) {
		if (ch.children == null) {
                    res[i] = new ArrayList();
		    ch.barnesSequential(bodies.bodyTreeRoot, res[i], bodies.params);
		} else {
		    /*spawn child jobs*/
		    byte[] newNodeId = new byte[nodeId.length + 1];
		    System.arraycopy(nodeId, 0, newNodeId, 0, nodeId.length);
		    newNodeId[nodeId.length] = (byte) i;
		    res[i] = /*spawn*/ doBarnesSO(newNodeId, iteration,
						     threshold, bodies);
		    spawned = true;
		}
		lastValidChild = i;
	    }
	}
	if (spawned) {
	    /*if we spawned, we have to sync*/
            sync();
	    return BodyTreeNode.combineResults(res, lastValidChild);
	}
        return BodyTreeNode.optimizeList(BodyTreeNode.combineResults(res, lastValidChild));
    }	

    public ArrayList doBarnesNTC(BodyTreeNode me, BodyTreeNode tree,
	    int threshold, RunParameters params) {
        if (me.children == null || me.bodyCount < threshold) {
            // leaf node, let barnesSequential handle this
            // (using optimizeList isn't useful for leaf nodes)
            ArrayList res = new ArrayList();
            me.barnesSequential(tree, res, params);
            return res;
        }

        //cell node -> call children[].barnes()
        ArrayList res[] = new ArrayList[8];
        int lastValidChild = -1;
        boolean spawned = false;

        for (int i = 0; i < 8; i++) {
            BodyTreeNode ch = me.children[i];
            if (ch != null) {
                if (ch.children == null) {
                    res[i] = new ArrayList();
                    ch.barnesSequential(tree, res[i], params);
                } else {
                    spawned = true;
                    //necessaryTree creation
                    BodyTreeNode necessaryTree = ch == tree
                        ? tree : new BodyTreeNode(tree, ch);
                    res[i] = barnesNTC(ch, necessaryTree, threshold, params);
                    //alternative: copy whole tree
                    //res[i] = barnesNTC(ch, tree, threshold, params);
                }
                lastValidChild = i;
            }
        }
        if (spawned) {
            sync();
            return BodyTreeNode.combineResults(res, lastValidChild);
        }
        //this was a sequential job, optimize!
        return BodyTreeNode.optimizeList(BodyTreeNode.combineResults(res, lastValidChild));
    }

    /**
     * Does the same as barnesSequential, but spawnes itself until a threshold
     * is reached. Before a subjob is spawned, the necessary tree for that
     * subjob is created to be passed to the subjob.
     * 
     * @param threshold
     *            the recursion depth at which work shouldn't be spawned anymore
     */
    public ArrayList barnesNTC(BodyTreeNode me, BodyTreeNode interactTree,
	    int threshold, RunParameters params) {
        return doBarnesNTC(me, interactTree, threshold, params);
    }

    void runSim() {
        long start = 0, end, phaseStart = 0;

        ArrayList result = null;
        double[] accs_x = new double[bodyArray.length];
        double[] accs_y = new double[bodyArray.length];
        double[] accs_z = new double[bodyArray.length];

        BodyCanvas bc = null;

        RemoteVisualization rv = null;

        System.out.println("BarnesHut: simulating "
                + bodyArray.length + " bodies, "
                + params.MAX_BODIES_PER_LEAF + " bodies/leaf node, "
                + "theta = " + params.THETA
                + ", spawn-min-threshold = " + spawn_min);

        // print the starting problem
        if (verbose) {
            printBodies(bodyArray);
        }

        if (viz) {
            bc = BodyCanvas.visualize(bodyArray);
        }

        if (remoteViz) {
            rv = new RemoteVisualization();
        }

        // turn of Satin during sequential pars.
        ibis.satin.SatinObject.pause();

        BodiesInterface bodies;

        if (impl == IMPL_SO) {
            bodies = new BodiesSO(bodyArray, params);
            ((BodiesSO)bodies).exportObject();
        } else {
            bodies = new Bodies(bodyArray, params);
        }

        start = System.currentTimeMillis();

        for (int iteration = 0; iteration < iterations; iteration++) {
            long updateTimeTmp = 0, forceCalcTimeTmp = 0, vizTimeTmp = 0;

            // System.out.println("Starting iteration " + iteration);

            phaseStart = System.currentTimeMillis();

            //force calculation

            ibis.satin.SatinObject.resume(); //turn ON divide-and-conquer stuff

            switch (impl) {
            case IMPL_NTC:
                result = doBarnesNTC(bodies.getRoot(), bodies.getRoot(),
                        spawn_min, params);
                break;
            case IMPL_SO:
                result = doBarnesSO(new byte[0], iteration, spawn_min, (BodiesSO) bodies);
                sync();
                break;
            case IMPL_SEQ:
                result = new ArrayList();
                bodies.getRoot().barnesSequential(bodies.getRoot(), result,
                        params);
                break;
            }

            ibis.satin.SatinObject.pause(); //killall divide-and-conquer stuff

            processLinkedListResult(result, accs_x, accs_y, accs_z);

            forceCalcTimeTmp = System.currentTimeMillis() - phaseStart;
            forceCalcTime += forceCalcTimeTmp;

            phaseStart = System.currentTimeMillis();

            if (iteration < iterations-1) {
                bodies.updateBodies(accs_x, accs_y, accs_z, iteration);
            } else {
                bodies.updateBodiesLocally(accs_x, accs_y, accs_z, iteration);
            }

            updateTimeTmp = System.currentTimeMillis() - phaseStart;
            updateTime += updateTimeTmp;

            phaseStart = System.currentTimeMillis();

            if (viz) {
                bc.repaint();
            }

            if (remoteViz) {
                rv.showBodies(bodyArray, iteration, updateTimeTmp + forceCalcTimeTmp);
            }

            vizTimeTmp = System.currentTimeMillis() - phaseStart;
            vizTime += vizTimeTmp;

            long total = updateTimeTmp + forceCalcTimeTmp + vizTimeTmp;

            System.err.println("Iteration " + iteration
                + " done"
                + ", update = "
                + updateTimeTmp + ", force = " + forceCalcTimeTmp + ", viz = "
                + vizTimeTmp + ", total = " + total);
        }

        end = System.currentTimeMillis();
        totalTime = end - start;
    }

    void processLinkedListResult(List result, double[] all_x, double[] all_y,
        double[] all_z) {
        Iterator it = result.iterator();
        int[] bodyNumbers;
        double[] tmp;
        int i;

        /*
         * I tried putting bodies computed by the same leaf job together in the
         * array of bodies, by creating a new bodyArray every time (to find the
         * current position of a body an extra int[] lookup table was used,
         * which of course had to be updated every iteration)
         * 
         * I thought this would improve locality during the next tree building
         * phase, and indeed, the tree building phase was shorter with a
         * sequential run with ibm 1.4.1 with jitc (with 4000 bodies/10
         * maxleafbodies: 0.377 s vs 0.476 s) (the CoM and update phases were
         * also slightly shorter)
         * 
         * but the force calc phase was longer, in the end the total run time
         * was longer ( 18.24 s vs 17.66 s )
         */

        while (it.hasNext()) {
            bodyNumbers = (int[]) it.next();
            tmp = (double[]) it.next();

            for (i = 0; i < bodyNumbers.length; i++) {
                all_x[bodyNumbers[i]] = tmp[3*i];
                all_y[bodyNumbers[i]] = tmp[3*i+1];
                all_z[bodyNumbers[i]] = tmp[3*i+2];
                if (ASSERTS) bodyArray[bodyNumbers[i]].updated = true;
            }
        }
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
        System.out.println("Iterations: " + iterations + " (timings DO "
            + "include the first iteration!)");

        switch (impl) {
        case IMPL_NTC:
            System.out.println("Using necessary tree impl");
            runSim();
            break;
        case IMPL_SEQ:
            System.out.println("Using hierarchical sequential impl");
            runSim();
            break;
        case IMPL_SO:
            System.out.println("Using shared object impl");
            runSim();
            break;
        default:
            System.out.println("EEK! Using unknown implementation #" + impl);
            System.exit(1);
            break; //blah
        }

        System.out.println("update took             " + updateTime / 1000.0
            + " s");
        System.out.println("Force calculation took  " + forceCalcTime / 1000.0
            + " s");
        System.out.println("visualization took      " + vizTime / 1000.0
            + " s");
        System.out.println("application barnes took "
            + (double) (totalTime / 1000.0) + " s");

        if (verbose) {
            System.out.println();
            printBodies(bodyArray);
        }
    }

    public static void main(String argv[]) {
        int nBodies = 0, mlb = 3;
        boolean nBodiesSeen = false;
        boolean mlbSeen = false;
        FileReader rdr = null;
        int i;

        RunParameters params = new RunParameters();
        params.THETA = THETA;
        params.DT = DT;
        params.DT_HALF = DT_HALF;
        params.SOFT = BodyTreeNode.SOFT;
        params.SOFT_SQ = BodyTreeNode.SOFT_SQ;

        //parse arguments
        for (i = 0; i < argv.length; i++) {
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
            } else if (argv[i].equals("-no-viz")) {
                viz = false;
            } else if (argv[i].equals("-ntc")) {
                impl = IMPL_NTC;
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
            } else if (argv[i].equals("-theta")) {
                params.THETA = Double.parseDouble(argv[++i]);
            } else if (argv[i].equals("-starttime")) {
                START_TIME = Double.parseDouble(argv[++i]);
            } else if (argv[i].equals("-endtime")) {
                END_TIME = Double.parseDouble(argv[++i]);
            } else if (argv[i].equals("-dt")) {
                params.DT = Double.parseDouble(argv[++i]);
                params.DT_HALF = params.DT / 2.0;
            } else if (argv[i].equals("-eps")) {
                params.SOFT = Double.parseDouble(argv[++i]);
                params.SOFT_SQ = params.SOFT * params.SOFT;
            } else if (argv[i].equals("-input")) {
                try {
                    rdr = new FileReader(argv[++i]);
                } catch (IOException e) {
                    throw new IllegalArgumentException(
                        "Could not open input file " + argv[i]);
                }
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
            } else if (!mlbSeen) {
                try {
                    mlb = Integer.parseInt(argv[i]); //max bodies per leaf node
                    mlbSeen = true;
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
            nBodies = 300;
        }

        params.MAX_BODIES_PER_LEAF = mlb;

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

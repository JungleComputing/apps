class SpawnTest {

    public static void main(String args[]) {

 	try {
	    int branch = Integer.parseInt(args[0]);
            int depth =  Integer.parseInt(args[1]);
            int load =  Integer.parseInt(args[2]);
            int workers = Integer.parseInt(args[3]);

            long count = 0;

            for (int i=0;i<=depth;i++) { 
                count += Math.pow(branch, i);
            }

            double time = (load * Math.pow(branch, depth)) / (1000*workers); 

            System.out.println("Running D&C with branch factor " + branch + 
                        " and depth " + depth + " load " + load + 
                        " (expected jobs: " + count + ", expected time: " + 
                        time + " sec.)");

            Test t = new Test();

            long start = System.currentTimeMillis();

	    long result = t.start(branch, depth, load);

            long end = System.currentTimeMillis();

            double msPerJob = ((double)(end-start)) / count;

            System.out.println("D&C(" + branch + ", " + depth + ") " 
                        + " wall clock time = " + (end-start) 
                        + " processing time = " + result  
                        + " avg job time = " + msPerJob + " msec/job");

        } catch (Exception e) {
            System.err.println("Oops: " + e);
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}

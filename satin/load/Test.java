class Test extends ibis.satin.SatinObject implements TestInterface {

    public Test() {
    }

    public long start(int branch, int depth, int load) {

      	long result = spawn_test(branch, depth, load);

	sync();

	return result;
    }
  
    public long spawn_test(int branch, int depth, int load) {

	if (depth == 0) {            

            if (load > 0) {
                
                long start = System.currentTimeMillis();
                long time = 0;
                
                while (time < load) { 
                    time = System.currentTimeMillis() - start;
                } 

                return time;
            }

            return 0;

	} else { 
	    
            long [] tmp = new long[branch];

            for (int i = 0; i < branch; i++) {
                tmp[i] = spawn_test(branch, depth-1, load);
            }

	    sync();

            long result = 0;
 
            for (int i = 0; i < branch; i++) {
		result += tmp[i];
            }

	    return result;
	}
    }
}

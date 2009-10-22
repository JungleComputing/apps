class Test extends ibis.satin.SatinObject implements TestInterface {

    public Test() {
    }

    public int start(int branch, int depth, int load) {

      	int result = spawn_test(branch, depth, load);

	sync();

	return result;
    }
  
    public int spawn_test(int branch, int depth, int load) {

	if (depth == 0) {            

            if (load > 0) {
                
                long start = System.currentTimeMillis();
                long time = 0;
                
                while (time < load) { 
                    time = System.currentTimeMillis() - start;
                } 
            }

            return 1;

	} else { 
	    
            int [] tmp = new int[branch];

            for (int i = 0; i < branch; i++) {
                tmp[i] = spawn_test(branch, depth-1, load);
            }

	    sync();

            int result = 1;
 
            for (int i = 0; i < branch; i++) {
		result += tmp[i];
            }

	    return result;
	}
    }
}

/* $Id$ */


import ibis.repmi.*;
import ibis.server.poolInfo.PoolInfo;


class Main {

    public static void main(String[] args) {

        try {
            PoolInfo info = new PoolInfo(null, true);

            if (info.rank() == 0) {
                myRep r = (myRep) RTS.createReplica("Test");
                r.foo();
                r.bar();
            } else {
                RTS.init();
                // do nothing !
            }
        } catch (Exception e) {
            System.err.println("oops: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }
}
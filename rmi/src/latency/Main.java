/* $Id$ */

package latency;


import ibis.server.poolInfo.PoolInfo;

class Main {

    public static void main(String[] args) {

        try {
            PoolInfo info = new PoolInfo(null, true);

            System.out.println("Starting process " + info.rank() + " on "
                    + info.getInetAddress().getHostAddress());

            String regHost = info.getInetAddress(1).getHostAddress();
            if (info.rank() == 0) {
                Client.doClient(regHost, 0, "bla");
            } else {
                Server.doServer(regHost, 0, "bla");
            }
        } catch (Exception e) {
            System.out.println("OOPS");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

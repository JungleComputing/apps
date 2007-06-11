/* $Id$ */


import ibis.server.poolInfo.PoolInfo;

class Main {

    public static void main(String[] args) {

        try {
            PoolInfo info = new PoolInfo(null, true);

            System.out.println("Starting process " + info.rank() + " on "
                    + info.getInetAddress().getHostAddress());

            if (info.rank() == 0) {
                Client.doClient("bimbambom", 0, "bla");
            } else {
                Server.doServer("bimbambom", 0, "bla");
            }
        } catch (Exception e) {
            System.out.println("OOPS");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

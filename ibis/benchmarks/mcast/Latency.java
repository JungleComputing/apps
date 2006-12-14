/* $Id$ */


import ibis.ipl.*;
import ibis.util.PoolInfo;

import java.util.Properties;
import java.io.IOException;

class Latency {

    static Ibis ibis;

    static Registry registry;

    public static void main(String[] args) {
        /* Parse commandline. */

        boolean upcall = false;

        if (args.length > 1) {
            upcall = args[1].equals("-u");
        }

        try {
            PoolInfo info = PoolInfo.createPoolInfo();

            int rank = info.rank();
            int size = info.size();
            int remoteRank = (rank == 0 ? 1 : 0);

            StaticProperties sp = new StaticProperties();
            sp.add("serialization", "object");
            sp
                    .add("communication",
                            "OneToOne, ManyToOne, OneToMany, Reliable, ExplicitReceipt, AutoUpcalls");
            sp.add("worldmodel", "closed");

            ibis = IbisFactory.createIbis(sp, null);
            registry = ibis.registry();

            PortType t = ibis.createPortType(sp);

            ReceivePort rport = t.createReceivePort("receive port");
            SendPort sport = t.createSendPort("send port");

            rport.enableConnections();

            Latency lat = null;

            if (rank == 0) {
                registry.elect("0");
                sport.connect(rport.identifier());

                System.err.println(rank + "*******  connect to myself");

                for (int i = 1; i < size; i++) {

                    System.err.println(rank + "******* receive");

                    ReadMessage r = rport.receive();
                    ReceivePortIdentifier id = (ReceivePortIdentifier) r
                            .readObject();
                    r.finish();

                    System.err.println(rank + "*******  connect to " + id);

                    sport.connect(id);
                }

                System.err.println(rank + "*******  connect done ");

                WriteMessage w = sport.newMessage();
                w.writeInt(42);
                w.finish();

                sport.close();

            } else {
                IbisIdentifier id = registry.getElectionResult("0");
                System.err.println(rank + "*******  connect to 0");
                sport.connect(id, "receive port");
                System.err.println(rank + "*******  connect done");

                WriteMessage w = sport.newMessage();
                w.writeObject(rport.identifier());
                w.finish();

                sport.close();
            }

            ReadMessage r = rport.receive();
            int result = r.readInt();
            r.finish();

            System.out.println(rank + " got " + result);

            rport.close();
            ibis.end();

        } catch (Exception e) {
            System.out.println("Got exception " + e);
            System.out.println("StackTrace:");
            e.printStackTrace();
        }
    }
}

/* $Id$ */


import ibis.ipl.*;

import java.util.Properties;
import java.util.Random;

import java.io.IOException;
class TreePingPong {

    static final int TREE_NODES=1023;

static class Sender {
    SendPort sport;
    ReceivePort rport;

    Sender(ReceivePort rport, SendPort sport) {
        this.rport = rport;
        this.sport = sport;
    }

    void send(int count, int repeat) throws Exception {
	Tree t = new Tree(TREE_NODES);

        for (int r = 0; r < repeat; r++) {

            long time = System.currentTimeMillis();

            for (int i = 0; i < count; i++) {
                WriteMessage writeMessage = sport.newMessage();
		writeMessage.writeObject(t);
                writeMessage.finish();

                ReadMessage readMessage = rport.receive();
		t = (Tree) readMessage.readObject();
                readMessage.finish();
            }

            time = System.currentTimeMillis() - time;

            double speed = (time * 1000.0) / (double) count;
	    double tp = ((count * TREE_NODES * Tree.PAYLOAD) / (1024*1024)) / (time / 1000.0);

            System.err.println(count + " calls took "
                    + (time / 1000.0) + " s, time/call = " + speed
                    + " us, throughput = " + tp + " MB/s, msg size = " + (TREE_NODES * Tree.PAYLOAD));
        }
    }
}

static class ExplicitReceiver {

    SendPort sport;

    ReceivePort rport;

    ExplicitReceiver(ReceivePort rport, SendPort sport) {
        this.rport = rport;
        this.sport = sport;
    }

    void receive(int count, int repeat) throws Exception {
	Tree t = null;//new Tree(TREE_NODES);

        for (int r = 0; r < repeat; r++) {
            for (int i = 0; i < count; i++) {

                ReadMessage readMessage = rport.receive();
		t = (Tree) readMessage.readObject();
                readMessage.finish();

                WriteMessage writeMessage = sport.newMessage();
		writeMessage.writeObject(t);
                writeMessage.finish();
            }
        }
    }
}


    static Ibis ibis;

    static Registry registry;

    public static void main(String[] args) {
        int count = 1000;
        int repeat = 50;
        int rank = 0, remoteRank = 1;

        try {
            StaticProperties s = new StaticProperties();
                s.add("Serialization", "ibis");

            s.add("Communication",
                    "OneToOne, Reliable, ExplicitReceipt");
            s.add("worldmodel", "closed");
            ibis = IbisFactory.createIbis(s, null);

            registry = ibis.registry();

            PortType t = ibis.createPortType(s);

            SendPort sport = t.createSendPort("send port");
            ReceivePort rport;
            IbisIdentifier master = registry.elect("latency");
            IbisIdentifier remote;

            if (master.equals(ibis.identifier())) {
                rank = 0;
                remote = registry.getElectionResult("client");
            } else {
                registry.elect("client");
                rank = 1;
                remote = master;
            }

	    Sender sender = null;
	    ExplicitReceiver receiver = null;
            if (rank == 0) {
                rport = t.createReceivePort("test port");
                rport.enableConnections();
                sport.connect(remote, "test port");
                sender = new Sender(rport, sport);
                sender.send(count, repeat);
            } else {
                sport.connect(remote, "test port");
                rport = t.createReceivePort("test port");
                rport.enableConnections();
                receiver = new ExplicitReceiver(rport, sport);
                receiver.receive(count, repeat);
            }

            /* free the send ports first */
            sport.close();
            rport.close();
            ibis.end();
        } catch (Exception e) {
            System.err.println("Got exception " + e);
            System.err.println("StackTrace:");
            e.printStackTrace();
        }
    }
}

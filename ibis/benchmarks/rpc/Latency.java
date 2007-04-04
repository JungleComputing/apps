/* $Id$ */


import ibis.ipl.*;

import java.util.Properties;
import ibis.util.Ticket;

import java.io.IOException;

class Sender implements MessageUpcall {

    Ticket t;

    SendPort sport;

    Ibis ibis;

    Sender(SendPort sport, Ibis ibis) {
        this.sport = sport;
        this.ibis = ibis;
        t = new Ticket();
    }

    void send(int count) throws IOException {
        // warmup
        for (int i = 0; i < count; i++) {
            int ticket = t.get();

            WriteMessage writeMessage = sport.newMessage();
            writeMessage.writeInt(ticket);
            writeMessage.writeByte((byte) 0);
            writeMessage.writeByte((byte) 1);
            writeMessage.writeInt(0);
            writeMessage.writeInt(0);
            writeMessage.writeInt(0);
            writeMessage.writeInt(0);
            writeMessage.finish();
            /*
             ibis.poll();
             ibis.poll();
             ibis.poll();
             ibis.poll();
             ibis.poll();
             ibis.poll();
             ibis.poll();
             ibis.poll();
             ibis.poll();
             ibis.poll();
             */

            ReadMessage readMessage = (ReadMessage) t.collect(ticket);
        }

        // test
        long time = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            int ticket = t.get();

            WriteMessage writeMessage = sport.newMessage();
            writeMessage.writeInt(ticket);
            writeMessage.writeByte((byte) 0);
            writeMessage.writeByte((byte) 1);
            writeMessage.writeInt(0);
            writeMessage.writeInt(0);
            writeMessage.writeInt(0);
            writeMessage.writeInt(0);
            writeMessage.finish();
            /*
             ibis.poll();
             ibis.poll();
             ibis.poll();
             ibis.poll();
             ibis.poll();
             ibis.poll();
             ibis.poll();
             ibis.poll();
             ibis.poll();
             ibis.poll();
             */

            ReadMessage readMessage = (ReadMessage) t.collect(ticket);
        }

        time = System.currentTimeMillis() - time;

        double speed = (time * 1000.0) / (double) count;
        System.err.println("Latency: " + count + " calls took "
                + (time / 1000.0) + " seconds, time/call = " + speed
                + " micros");
    }

    public void upcall(ReadMessage readMessage) {

        try {
            int ticket = readMessage.readInt();
            readMessage.readByte();
            readMessage.readByte();
            readMessage.finish();
            t.put(ticket, readMessage);
        } catch (Exception e) {
            System.out.println("EEEEEK " + e);
            e.printStackTrace();
        }
    }
}

class UpcallReceiver implements MessageUpcall {

    SendPort sport;

    int count = 0;

    int max;

    Ibis ibis;

    UpcallReceiver(SendPort sport, int max, Ibis ibis) {
        this.sport = sport;
        this.max = max;
        this.ibis = ibis;
    }

    public void upcall(ReadMessage readMessage) {

        try {
            int ticket = readMessage.readInt();
            readMessage.readByte();
            readMessage.readByte();
            readMessage.readInt();
            readMessage.readInt();
            readMessage.readInt();
            readMessage.readInt();
            readMessage.finish();

            WriteMessage writeMessage = sport.newMessage();
            writeMessage.writeInt(ticket);
            writeMessage.writeByte((byte) 0);
            writeMessage.writeByte((byte) 1);
            writeMessage.finish();

            count++;

            if (count == max) {
                synchronized (this) {
                    notifyAll();
                }
            }

        } catch (Exception e) {
            System.out.println("EEEEEK " + e);
            e.printStackTrace();
        }
    }

    synchronized void finish() {
        while (count < max) {
            try {
                wait();
            } catch (Exception e) {
            }
        }

        System.err.println("Finished");

    }
}

class Latency implements PredefinedCapabilities {
    static Ibis ibis;

    static Registry registry;

    public static void main(String[] args) {
        /* Parse commandline. */

        if (args.length != 1) {
            System.err.println("Usage: Latency <count>");
            System.exit(33);
        }

        int count = Integer.parseInt(args[0]);
        int rank = 0;

        try {
            CapabilitySet s = new CapabilitySet(WORLDMODEL_CLOSED,
                    CONNECTION_ONE_TO_ONE,
                    COMMUNICATION_RELIABLE, SERIALIZATION_OBJECT,
                    RECEIVE_AUTO_UPCALLS, RECEIVE_POLL);

            ibis = IbisFactory.createIbis(s, null, null, null);
            registry = ibis.registry();

            CapabilitySet t = s;

            SendPort sport = ibis.createSendPort(t);
            ReceivePort rport;
            Latency lat = null;

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

            if (rank == 0) {
                System.out.println("Creating sender");
                Sender sender = new Sender(sport, ibis);
                rport = ibis.createReceivePort(t, "test port", sender);
                rport.enableConnections();
                rport.enableMessageUpcalls();
                sport.connect(remote, "test port");
                System.out.println("Created sender");
                sender.send(count);

            } else {
                System.out.println("Creating receiver");
                sport.connect(remote, "test port");

                UpcallReceiver receiver = new UpcallReceiver(sport, 2 * count,
                        ibis);
                rport = ibis.createReceivePort(t, "test port", receiver);
                rport.enableConnections();
                rport.enableMessageUpcalls();
                System.out.println("Created receiver");
                receiver.finish();
            }

            /* free the send ports first */
            sport.close();
            rport.close();
            ibis.end();

            System.exit(0);

        } catch (Exception e) {
            System.out.println("Got exception " + e);
            System.out.println("StackTrace:");
            e.printStackTrace();
        }
    }
}

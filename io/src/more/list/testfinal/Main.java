package more.list.testfinal;

/* $Id: Main.java 5349 2007-04-07 13:59:32Z ceriel $ */

import ibis.server.poolInfo.PoolInfo;
import ibis.util.io.BufferedArrayInputStream;
import ibis.util.io.BufferedArrayOutputStream;
import ibis.util.io.DataInputStream;
import ibis.util.io.DataOutputStream;
import ibis.util.io.IbisSerializationInputStream;
import ibis.util.io.IbisSerializationOutputStream;

import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

public class Main {

    public static final int LEN = 100;

    public static final int COUNT = 10000;

    static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String args[]) {

        try {
            Data temp = null;
            long start, end;

            int count = COUNT;
            int len = LEN;

            PoolInfo info = new PoolInfo(null, true);

            if (info.rank() == 0) {

                System.err.println("Main starting");

                ServerSocket server = new ServerSocket(1234);
                Socket s = server.accept();

                server.close();

                s.setTcpNoDelay(true);

                DataInputStream in = new BufferedArrayInputStream(s
                        .getInputStream(), 4096);
                DataOutputStream out = new BufferedArrayOutputStream(s
                        .getOutputStream(), 4096);

                IbisSerializationInputStream min = new IbisSerializationInputStream(
                        in);
                IbisSerializationOutputStream mout = new IbisSerializationOutputStream(
                        out);

                // Create list
                for (int i = 0; i < len; i++) {
                    temp = new Data(i, temp);
                }

                System.err.println("Writing list of " + len + " Data objects");

                // Warmup
                for (int i = 0; i < count; i++) {
                    mout.writeObject(temp);
                    mout.flush();
                    mout.reset();
                    logger.debug("Warmup " + i);
                }

                min.readByte();

                // Real test.
                start = System.currentTimeMillis();

                for (int i = 0; i < count; i++) {
                    mout.writeObject(temp);
                    mout.flush();
                    mout.reset();
                    logger.debug("Test " + i);
                }

                min.readByte();

                end = System.currentTimeMillis();

                System.err.println("Write took " + (end - start) + " ms.  => "
                        + ((1000.0 * (end - start)) / count) + " us/call => "
                        + ((1000.0 * (end - start)) / (count * len))
                        + " us/object");

                System.err
                        .println("Bytes written "
                                + (count * len * Data.OBJECT_SIZE)
                                + " throughput = "
                                + (((1000.0 * (count * len * Data.OBJECT_SIZE)) / (1024 * 1024)) / (end - start))
                                + " MBytes/s");

                s.close();

            } else {

                Socket s = null;

                while (s == null) {
                    try {
                        s = new Socket(info.hostName(0), 1234);
                    } catch (Exception e) {
                        Thread.sleep(1000);
                        // ignore
                    }
                }

                s.setTcpNoDelay(true);

                DataInputStream in = new BufferedArrayInputStream(s
                        .getInputStream(), 4096);
                DataOutputStream out = new BufferedArrayOutputStream(s
                        .getOutputStream(), 4096);

                IbisSerializationInputStream min = new IbisSerializationInputStream(
                        in);
                IbisSerializationOutputStream mout = new IbisSerializationOutputStream(
                        out);

                for (int i = 0; i < count; i++) {
                    temp = (Data) min.readObject();
                    logger.debug("Warmup " + i);
                }

                mout.writeByte((byte) 1);
                mout.flush();

                for (int i = 0; i < count; i++) {
                    temp = (Data) min.readObject();
                    logger.debug("Test " + i);
                }

                mout.writeByte((byte) 1);
                mout.flush();

                s.close();
            }

        } catch (Exception e) {
            System.err.println("Main got exception " + e);
            e.printStackTrace();
        }
    }
}

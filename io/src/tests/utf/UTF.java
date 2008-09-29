package tests.testfinal;

/* $Id: UTF.java 2844 2004-11-24 10:52:27Z ceriel $ */

import ibis.io.IbisSerializationInputStream;
import ibis.io.IbisSerializationOutputStream;

/**
 * readUTF/writeUTF was not tested properly ...
 */
public class UTF implements java.io.Serializable {

    String s;

    public UTF() {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < 40000; i += 100) {
            b.append((char) i);
        }
        s = b.toString();
    }

    public static void main(String args[]) {
	UTF e = new UTF();
	StoreBuffer buf = new StoreBuffer();
	StoreArrayInputStream store_in = new StoreArrayInputStream(buf);
	StoreArrayOutputStream store_out = new StoreArrayOutputStream(buf);
	try {
	    IbisSerializationOutputStream mout = new IbisSerializationOutputStream(store_out);
	    IbisSerializationInputStream min = new IbisSerializationInputStream(store_in);
	    mout.writeObject(e);
	    mout.flush();
	    mout.reset();

	    Object o = min.readObject();
	    System.out.println("Object read");
	    if (! (o instanceof UTF)) {
		System.out.println("Error 1");
		System.exit(1);
	    }
	    System.out.println("Test1 succeeds");
	    UTF e2 = (UTF) o;
	    if (! e2.s.equals(e.s)) {
		System.out.println("Error 2");
		System.exit(1);
	    }
	    System.out.println("Test2 succeeds");
	} catch(Exception ex) {
	    System.out.println("Got exception: " + ex);
	    ex.printStackTrace();
	}
    }
}

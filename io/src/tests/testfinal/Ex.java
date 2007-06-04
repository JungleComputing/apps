package tests.testfinal;

/* $Id: Ex.java 2844 2004-11-24 10:52:27Z ceriel $ */

import ibis.io.IbisSerializationInputStream;
import ibis.io.IbisSerializationOutputStream;

/**
 * A difficult case for Ibis serialization. Testing, testing ...
 */
public class Ex extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -2336476944348958468L;
    /**
     * Some final fields. The problem here is that IOGenerator cannot
     * build a constructor when Exception is not rewritten, and this
     * causes difficulties when deserializing the final fields.
     */
    final int ival = 10;
    final String sval = "ten";

    public static void main(String args[]) {
	Ex e = new Ex();
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
	    if (! (o instanceof Ex)) {
		System.out.println("Error 1");
		System.exit(1);
	    }
	    System.out.println("Test1 succeeds");
	    Ex e2 = (Ex) o;
	    if (e2.ival != e.ival) {
		System.out.println("Error 2");
		System.exit(1);
	    }
	    System.out.println("Test2 succeeds");
	    if (! e2.sval.equals(e.sval)) {
		System.out.println("Error 3");
		System.exit(1);
	    }
	    System.out.println("Test3 succeeds");
	} catch(Exception ex) {
	    System.out.println("Got exception: " + ex);
	    ex.printStackTrace();
	}
    }
}

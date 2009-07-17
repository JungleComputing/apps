package tests.testfinal;

/* $Id: Ex.java 2844 2004-11-24 10:52:27Z ceriel $ */

import ibis.io.IbisSerializationInputStream;
import ibis.io.IbisSerializationOutputStream;

/**
 * A difficult case for Ibis serialization. Testing, testing ...
 */
public class Ex1 extends java.util.BitSet {

    private static final long serialVersionUID = 1L;

    public static void main(String args[]) {
	Ex1 e = new Ex1();
	e.set(10);
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
	    if (! (o instanceof Ex1)) {
		System.out.println("Error 1");
		System.exit(1);
	    }
	    System.out.println("Test1 succeeds");
	    e = (Ex1) o;
	    if (! e.get(10)) {
	        System.out.println("Test2 fails");
	    } else {
	        System.out.println("Test2 succeeds");
	    }
	    if (e.get(1)) {
            System.out.println("Test3 fails");
        } else {
            System.out.println("Test3 succeeds");
        }
	    
	} catch(Exception ex) {
	    System.out.println("Got exception: " + ex);
	    ex.printStackTrace();
	}
    }
}

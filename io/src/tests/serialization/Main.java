package tests.serialization;

import java.io.IOException;
import java.util.ArrayList;

import ibis.io.IbisSerializationInputStream;
import ibis.io.IbisSerializationOutputStream;

public class Main {

    /**
     * @param args
     */
    public static void main(String[] args) {
        ArrayList<TestObject> objectsToTest = new ArrayList<TestObject>();
        
        objectsToTest.add(new Fields());
        objectsToTest.add(new FinalsWithConstructor());
        objectsToTest.add(new FinalsWithoutConstructor());
        objectsToTest.add(new SerialPersistent());
        objectsToTest.add(new Transient());
        objectsToTest.add(new BitsetTest());
        objectsToTest.add(new CircularObject());
        objectsToTest.add(new ReadWriteObject());
        objectsToTest.add(new ExternalizableTest());
        objectsToTest.add(new SuperClass());
        
        StoreBuffer buf = new StoreBuffer();
        StoreArrayInputStream store_in = new StoreArrayInputStream(buf);
        StoreArrayOutputStream store_out = new StoreArrayOutputStream(buf);
        try {
            IbisSerializationOutputStream mout = new IbisSerializationOutputStream(store_out);
            IbisSerializationInputStream min = new IbisSerializationInputStream(store_in);
            for (TestObject o : objectsToTest) {
                if (! testObject(o, mout, min)) {
                    System.err.println("Test " + o.getClass().getName() + " failed");
                } else {
                    System.out.println("Test " + o.getClass().getName() + " succeeded");
                }
            }
        } catch(Throwable ex) {
            System.out.println("Got exception: " + ex);
            ex.printStackTrace();
            while (ex.getCause() != null) {
                ex = ex.getCause();
                System.out.println("Nested exception: " + ex);
                ex.printStackTrace();
            }
        }
    }
    
    private static boolean testObject(TestObject e,
            IbisSerializationOutputStream mout,
            IbisSerializationInputStream min) throws IOException, ClassNotFoundException {
        e.init();
        mout.writeObject(e);
        mout.flush();
        mout.reset();
        Object o = min.readObject();
        return e.testResult(o);
    }
}


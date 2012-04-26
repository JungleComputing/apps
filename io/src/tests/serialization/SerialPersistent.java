package tests.serialization;

import java.io.ObjectStreamField;

public class SerialPersistent implements TestObject {

    public int ival;
    String sval;
    private int[] a;
    
    public void init() {
        ival = 10;
        sval = "ten";
        a = new int[] { 0, 1, 2};
    }
 
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("ival", Integer.TYPE),
        new ObjectStreamField("a", (new int[1]).getClass())
    };
    
    public boolean testResult(Object o) {
        if (! (o instanceof SerialPersistent)) {
            return false;
        }
        SerialPersistent f = (SerialPersistent) o;
        if (a.length != f.a.length) {
            System.err.println("SerialPersistent: Wrong array length");
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != f.a[i]) {
                System.err.println("SerialPersistent: Wrong array value");
                return false;
            }
        }
        if (f.sval != null) {
            // sval is not mentioned in serialPersistentFields.
            System.err.println("SerialPersistent: Wrong string value");
            return false;
        }
        if (ival != f.ival) {
            System.err.println("SerialPersistent: Wrong integer value");
            return false;
        }
        return true;
    }
}

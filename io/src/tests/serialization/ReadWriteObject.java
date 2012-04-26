package tests.serialization;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ReadWriteObject implements TestObject {

    public int ival;
    String sval;
    private int[] a;
    
    public void init() {
        ival = 10;
        sval = "ten";
        a = new int[] { 0, 1, 2};
    }
    
    public boolean testResult(Object o) {
        if (! (o instanceof ReadWriteObject)) {
            return false;
        }
        ReadWriteObject f = (ReadWriteObject) o;
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
    
    private void writeObject(ObjectOutputStream oos)
            throws IOException {
        oos.writeInt(ival);
        oos.writeObject(a);
    }

    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException {
        ival = ois.readInt();
        a = (int[]) ois.readObject();
        sval = null;
    }
}

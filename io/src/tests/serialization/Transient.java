package tests.serialization;

public class Transient implements TestObject {

    public int ival;
    transient String sval;
    private int[] a;
    
    public void init() {
        ival = 10;
        sval = "ten";
        a = new int[] { 0, 1, 2};
    }
    
    public boolean testResult(Object o) {
        if (! (o instanceof Transient)) {
            return false;
        }
        Transient f = (Transient) o;
        if (a.length != f.a.length) {
            System.err.println("Transient: Wrong array length");
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != f.a[i]) {
                System.err.println("Transient: Wrong array value");
                return false;
            }
        }
        if (f.sval != null) {
            // sval is transient.
            System.err.println("Transient: Wrong string value");
            return false;
        }
        if (ival != f.ival) {
            System.err.println("Transient: Wrong integer value");
            return false;
        }
        return true;
    }
}

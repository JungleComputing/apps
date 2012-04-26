package tests.serialization;

/**
 * Just some fields.
 */
public class Fields implements TestObject {

    public int ival;
    public float fval;
    public double dval;
    public char cval;
    public byte bval;
    public boolean jval;
    public long lval;
    public short sval;
    String strval;
    private int[] a;
    Class<?> cl;
    
    public void init() {
        ival = 10;
        fval = 20.0f;
        dval = 40.0;
        cval = 'X';
        bval = 037;
        jval = true;
        lval = 65536L * 65536 + 5;
        sval = -5;
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < 40000; i += 100) {
            b.append((char) i);
        }
        strval = b.toString();
        a = new int[] { 0, 1, 2};
        cl = StringBuffer.class;
    }
    
    public boolean testResult(Object o) {
        if (! (o instanceof Fields)) {
            return false;
        }
        Fields f = (Fields) o;
        if (a.length != f.a.length) {
            System.err.println("Fields: Wrong array length");
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != f.a[i]) {
                System.err.println("Fields: Wrong array value");
                return false;
            }
        }
        if (! strval.equals(f.strval)) {
            System.err.println("Fields: Wrong string value");
            return false;
        }
        if (ival != f.ival) {
            System.err.println("Fields: Wrong integer value");
            return false;
        }
        if (fval != f.fval) {
            System.err.println("Fields: Wrong float value");
            return false;
        }
        if (dval != f.dval) {
            System.err.println("Fields: Wrong double value");
            return false;
        }
        if (cval != f.cval) {
            System.err.println("Fields: Wrong char value");
            return false;
        }
        if (bval != f.bval) {
            System.err.println("Fields: Wrong byte value");
            return false;
        }
        if (jval != f.jval) {
            System.err.println("Fields: Wrong boolean value");
            return false;
        }
        if (sval != f.sval) {
            System.err.println("Fields: Wrong short value");
            return false;
        }
        if (lval != f.lval) {
            System.err.println("Fields: Wrong long value");
            return false;
        }
        if (cl != StringBuffer.class) {
            System.err.println("Fields: Wrong class value");
            return false;
        }
        return true;
    }
}

package tests.serialization;

/**
 * Final fields.
 */
public class FinalsWithConstructor implements TestObject {

    public final int ival = 10;
    final String strval = "ten";
    private final int[] a = { 0, 1, 2};
    public final float fval = 20.0f;
    public final double dval = 40.0;
    public final char cval = 'X';
    public final byte bval = 037;
    public final boolean jval = true;
    public final long lval = 65536L * 65536 + 4;
    public final short sval = -17;
    public final Fields fields = new Fields();
    
    public void init() {
        fields.init();
    }
    
    public boolean testResult(Object o) {
        if (! (o instanceof FinalsWithConstructor)) {
            return false;
        }
        FinalsWithConstructor f = (FinalsWithConstructor) o;
        if (a.length != f.a.length) {
            System.err.println("FinalsWithConstructor: Wrong array length");
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != f.a[i]) {
                System.err.println("FinalsWithConstructor: Wrong array value");
                return false;
            }
        }
        if (! strval.equals(f.strval)) {
            System.err.println("FinalsWithConstructor: Wrong string value");
            return false;
        }
        if (ival != f.ival) {
            System.err.println("FinalsWithConstructor: Wrong integer value");
            return false;
        }
        if (! strval.equals(f.strval)) {
            System.err.println("FinalsWithConstructor: Wrong string value");
            return false;
        }
        if (ival != f.ival) {
            System.err.println("FinalsWithConstructor: Wrong integer value");
            return false;
        }
        if (fval != f.fval) {
            System.err.println("FinalsWithConstructor: Wrong float value");
            return false;
        }
        if (dval != f.dval) {
            System.err.println("FinalsWithConstructor: Wrong double value");
            return false;
        }
        if (cval != f.cval) {
            System.err.println("FinalsWithConstructor: Wrong char value");
            return false;
        }
        if (bval != f.bval) {
            System.err.println("FinalsWithConstructor: Wrong byte value");
            return false;
        }
        if (jval != f.jval) {
            System.err.println("FinalsWithConstructor: Wrong boolean value");
            return false;
        }
        if (sval != f.sval) {
            System.err.println("FinalsWithConstructor: Wrong short value");
            return false;
        }
        if (lval != f.lval) {
            System.err.println("FinalsWithConstructor: Wrong long value");
            return false;
        }
        if (! fields.testResult(f.fields)) {
            System.err.println("FinalsWithConstructor: Wrong fields value");
        }
        return true;
    }
}

package tests.serialization;

/**
 * A difficult for Ibis serialization: the IOGenerator cannot create a constructor, because
 * Exception is not rewritten. This makes deserialization of final fields difficult.
 */
public class FinalsWithoutConstructor extends Exception implements TestObject {

    public final int ival = 10;
    final String sval = "ten";
    private final int[] a = { 0, 1, 2};
    private final Fields fields = new Fields();
    
    public void init() {
        fields.init();
    }
    
    public boolean testResult(Object o) {
        if (! (o instanceof FinalsWithoutConstructor)) {
            return false;
        }
        FinalsWithoutConstructor f = (FinalsWithoutConstructor) o;
        if (a.length != f.a.length) {
            System.err.println("FinalsWithoutConstructor: Wrong array length");
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != f.a[i]) {
                System.err.println("FinalsWithoutConstructor: Wrong array value");
                return false;
            }
        }
        if (! sval.equals(f.sval)) {
            System.err.println("FinalsWithoutConstructor: Wrong string value");
            return false;
        }
        if (ival != f.ival) {
            System.err.println("FinalsWithoutConstructor: Wrong integer value");
            return false;
        }
        if (! fields.testResult(f.fields)) {
            System.err.println("FinalsWithoutConstructor: Wrong fields value");
            return false;
        }
        
        return true;
    }
}

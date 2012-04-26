package tests.serialization;

public class SuperClass extends Fields {

    int dummy = 20;

    public boolean testResult(Object o) {
        if (! (o instanceof SuperClass)) {
            System.err.println("SuperClass: wrong type");
            return false;
        }
        SuperClass t = (SuperClass) o;
        if (t.dummy != 20) {
            System.err.println("SuperClass: wrong value");
            return false;
        }
        if (! super.testResult(o)) {
            // The superclass result failed.
            System.err.println("SuperClass: testResult of super failed");
            return false;
        }
        return true;
    }
}

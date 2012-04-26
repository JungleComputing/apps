package tests.serialization;

public class BitsetTest extends java.util.BitSet implements TestObject {

    @Override
    public boolean testResult(Object o) {
        if (! (o instanceof BitsetTest)) {
            System.err.println("BitsetTest: wrong result type");
            return false;
        }
        if (! equals(o)) {
            System.err.println("BitsetTest: not equal");
        }
        return true;
    }

    @Override
    public void init() {
        set(10);
    }

}

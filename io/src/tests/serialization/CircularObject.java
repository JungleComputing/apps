package tests.serialization;

public class CircularObject implements TestObject {

    private static final int LEN = 5;
    
    CircularObject next;
    int num;
    
    @Override
    public boolean testResult(Object o) {
        if (! (o instanceof CircularObject)) {
            System.err.println("CircularObject: wrong type");
            return false;            
        }
        CircularObject c = (CircularObject) o;
        for (int i = 0; i <= LEN; i++) {
            if (c.num != i) {
                System.err.println("CircularObject: wrong value");
                return false;
            }
            c = c.next;
        }
        if (c != (CircularObject) o) {
            System.err.println("CircularObject: not circular");
            return false;
        }
        return true;
    }

    @Override
    public void init() {
        CircularObject o = this;
        o.num = 0;
        for (int i = 1; i <= LEN; i++) {
            o.next = new CircularObject();
            o.next.num = i;
            o = o.next;
        }
        o.next = this;
    }

}

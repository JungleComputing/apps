/* $Id$ */

final class AbortTest extends ibis.satin.SatinObject implements
        AbortTestInterface, java.io.Serializable {
    public int foo() {
        System.out.println("running foo!");
        return 0;
    }

    public static void main(String[] args) {
        AbortTest t = new AbortTest();
        int n = 0;

        for (int i = 0; i < 3; i++) {
            int f = t.foo();
        }
        t.abort();
        t.sync();
    }
}

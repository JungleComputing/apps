package tests.serialization;

import java.io.Serializable;

public interface TestObject extends Serializable {
    public boolean testResult(Object o);
    public void init();
}

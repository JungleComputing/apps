package tests.serialization;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ExternalizableTest extends Fields implements Externalizable {

    int dummy = 20;
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(dummy);

    }

    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        dummy = in.readInt();
    }
    
    public boolean testResult(Object o) {
        if (! (o instanceof ExternalizableTest)) {
            System.err.println("ExternalizableTest: wrong type");
            return false;
        }
        ExternalizableTest t = (ExternalizableTest) o;
        if (t.dummy != 20) {
            System.err.println("ExternalizableTest: wrong value");
            return false;
        }
        if (t.strval != null) {
            // The superclass should not be serialized/deserialized.
            System.err.println("ExternalizableTest: strval != null");
            return false;
        }
        return true;
    }
}

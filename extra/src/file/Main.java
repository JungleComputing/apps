package file;

/* $Id$ */


import ibis.util.io.BufferedArrayInputStream;
import ibis.util.io.BufferedArrayOutputStream;
import ibis.util.io.IbisSerializationInputStream;
import ibis.util.io.IbisSerializationOutputStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;

class Main {

    public static void main(String[] args) {

        try {

            FileOutputStream f = new FileOutputStream("aap");
            BufferedArrayOutputStream b = new BufferedArrayOutputStream(f, 4096);
            IbisSerializationOutputStream m = new IbisSerializationOutputStream(
                    b);

            Tree t = new Tree(16 * 1024);

            m.writeObject(t);
            m.flush();

            FileInputStream fi = new FileInputStream("aap");
            BufferedArrayInputStream bi = new BufferedArrayInputStream(fi, 4096);
            IbisSerializationInputStream mi = new IbisSerializationInputStream(
                    bi);

            mi.readObject();

        } catch (Exception e) {
            System.out.println("OOPS" + e);
            e.printStackTrace();
        }
    }
}

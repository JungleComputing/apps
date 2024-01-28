package IO;

import java.io.*;

public class ReadFile {
    public static void read(String filename, Graph g, boolean directed) throws Exception {
        File input_file = new File(filename);
        DataInputStream data_in = new DataInputStream(new BufferedInputStream(new FileInputStream(input_file)));

        int a;
        int b;

        while(true) {
            try {
                a = data_in.readInt();
                b = data_in.readInt();

                g.addEdge(a, b);

                if(!directed) {
                    g.addEdge(b, a);
                }
            }
            catch(EOFException eof) {
                System.out.println ("End of File");
                break;
            }
        }
    }
}
//package dsearchDC;

import java.util.Vector;
import java.io.BufferedReader;
import java.io.FileReader;

public class FileSequences implements java.io.Serializable {
    private Vector sequences;

    private String fileName;

    public FileSequences(String fileName) {
        sequences = new Vector();
        this.fileName = fileName;
    }

    public void createFileSequnces() {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(fileName));

            String line = bf.readLine();

            while (line != null) {
                if (line.charAt(0) == '>') {
                    Sequence seq = new Sequence();
                    seq.setSequenceName(line);

                    Vector tail = new Vector();
                    while ((line = bf.readLine()) != null
                        && line.charAt(0) != '>') {
                        tail.add(line);
                    }
                    seq.setSequenceBody(tail);

                    sequences.add(seq);
                }
            }
        } catch (Exception e) {
            System.out.println("MyException in processFile: " + e.toString());
        }
    }

    public Vector getSequences() {
        return sequences;
    }

    public void zero() {
        sequences = null;
    }
}

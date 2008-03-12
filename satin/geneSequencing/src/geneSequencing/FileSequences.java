package geneSequencing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class FileSequences implements java.io.Serializable {
    private ArrayList<Sequence> sequences;

    public FileSequences(String fileName) {
        sequences = new ArrayList<Sequence>();
        createFileSequences(fileName);
    }

    private void createFileSequences(String fileName) {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(fileName));

            String line = bf.readLine();

            while (line != null) {
                if (line.charAt(0) == '>') {

                    String name = line;

                    ArrayList<String> tail = new ArrayList<String>();
                    while ((line = bf.readLine()) != null
                        && line.charAt(0) != '>') {
                        tail.add(line);
                    }

                    sequences.add(new Sequence(name, tail));
                }
            }
        } catch (Exception e) {
            throw new Error("Exception in processFile: " + e.toString());
        }
    }

    public ArrayList<Sequence> getSequences() {
        return sequences;
    }

    public void zero() {
        sequences = null;
    }
    
    public int size() {
        return sequences.size();
    }

    public int maxLength() {
        int maxlen = 0;
        for (Sequence seq : sequences) {
            int len = seq.length();
            if (len > maxlen) {
                maxlen = len;
            }
        }
        return maxlen;
    }
}

package geneSequencing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class FileSequences implements java.io.Serializable {
    private ArrayList<Sequence> sequences;

    private String fileName;

    public FileSequences(String fileName) {
        sequences = new ArrayList<Sequence>();
        this.fileName = fileName;
        createFileSequences();
    }

    private void createFileSequences() {
        try {
            BufferedReader bf = new BufferedReader(new FileReader(fileName));

            String line = bf.readLine();

            while (line != null) {
                if (line.charAt(0) == '>') {
                    Sequence seq = new Sequence();
                    seq.setSequenceName(line);

                    ArrayList<String> tail = new ArrayList<String>();
                    while ((line = bf.readLine()) != null
                        && line.charAt(0) != '>') {
                        tail.add(line);
                    }
                    seq.setSequenceBody(tail);

                    sequences.add(seq);
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
}

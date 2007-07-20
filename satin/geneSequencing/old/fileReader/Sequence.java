//package dsearchDCf;

import java.util.Vector;
import java.io.RandomAccessFile;

public class Sequence implements java.io.Serializable
{
    private String fileName;
    private long   sequenceNamePointer;
    private Vector sequenceBodyPointers;

    private String sequenceName;
    private String sequenceBody;
    private int score;
    private String alignment;

    public Sequence()
    {
        sequenceNamePointer = 0;
        sequenceBodyPointers = new Vector();

        sequenceName = new String();
        sequenceBody = new String();
        score        = 0;
        alignment    = "not calculated";
    }

    public Sequence(String fileName) //used
    {
        this.fileName = fileName;
        sequenceNamePointer = 0;
        sequenceBodyPointers = new Vector();

        sequenceName = new String();
        sequenceBody = new String();
        score        = 0;
        alignment    = "not calculated";
    }

    public Sequence(String name, String body) // used in SharedData
    {
        sequenceName = name;
        sequenceBody = body;
    }

    public Sequence(Sequence seq)
    {
        sequenceName = new String(seq.getSequenceName());
        sequenceBody = new String(seq.getSequenceBody());
        score        = new Integer(seq.getSequenceScore());
        alignment    = new String(seq.getSequenceAlignment());

        fileName     = new String(seq.getFilename());
        sequenceNamePointer = new Long(seq.getSequenceNamePointer());
        sequenceBodyPointers = new Vector(seq.getSequenceBodyPointers());

    }

    public String getFilename()
    {
        return fileName;
    }

    public String getSequenceName()
    {
        return sequenceName;
    }

    public String getSequenceBody()
    {
        return sequenceBody;
    }

    public int getSequenceScore()
    {
        return score;
    }

    public String getSequenceAlignment()
    {
        return alignment;
    }
    public void setSequenceName(String name)
    {
        sequenceName = name;
    }

    public void setSequenceBody(String body)
    {
        sequenceBody = body;
    }

    public void setSequenceScore(int score)
    {
        this.score = score;
    }

    public void setSequenceAlignment(String alignment)
    {
        this.alignment = new String(alignment);
    }

    public void setSequenceNamePointer(long pointer)
    {
        sequenceNamePointer = pointer;
    }

    public void setSequenceBodyPointers(Vector pointers)
    {
        sequenceBodyPointers = pointers;
    }

    public void addToSequenceBodyPointers(long pointer)
    {
        sequenceBodyPointers.add(pointer);
    }

    public long getSequenceNamePointer()
    {
        return sequenceNamePointer;
    }

    public Vector getSequenceBodyPointers()
    {
        return sequenceBodyPointers;
    }

    public void createSequenceName() throws Throwable
    {
        RandomAccessFile file = new RandomAccessFile(fileName,"r");

        file.seek(sequenceNamePointer);
        sequenceName = file.readLine();

        file.close();
    }

    public void createSequenceBody() throws Throwable
    {
        RandomAccessFile file = new RandomAccessFile(fileName,"r");

        for(int i = 0; i < sequenceBodyPointers.size(); i++)
        {
            file.seek((Long)sequenceBodyPointers.get(i));
            sequenceBody = sequenceBody.concat(file.readLine());
        }

        file.close();
    }           
}

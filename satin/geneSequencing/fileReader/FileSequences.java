//package dsearchDCf;

import java.util.Vector;
import java.io.RandomAccessFile;

public class FileSequences implements java.io.Serializable
{
    private Vector sequencesPointers;
    private String fileName;

    public FileSequences(String fileName)
    {
        sequencesPointers = new Vector();
        this.fileName = fileName;
    }

    public void createFileSequncesPointers()
    {
        RandomAccessFile file;
        String line;

        try
        {
            Sequence sequence = new Sequence(fileName);
            file = new RandomAccessFile(fileName,"r");
            long beginPointer = file.getFilePointer();
            line = file.readLine();

            while(line != null)
            {
                if(line.charAt(0) == '>')
                {
                    sequencesPointers.add(sequence);
                    sequence = new Sequence(fileName);
                    sequence.setSequenceNamePointer(beginPointer);
                }
                else
                {
                    sequence.addToSequenceBodyPointers(beginPointer);
                }

                beginPointer = file.getFilePointer();
                line = file.readLine();
            }
            if(line == null)
                sequencesPointers.add(sequence);

            sequencesPointers.remove(0);
        }
        catch(Exception ex)
        {
            System.out.println("MyException in createFilePointers: " + ex.toString());
        }
    }


    public Vector getFileSequencesPointers()
    {
        return sequencesPointers;
    }
}

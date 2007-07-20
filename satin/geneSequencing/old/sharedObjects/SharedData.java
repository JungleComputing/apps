//package dsearchDC_so;

import java.util.Vector;

public class SharedData
       extends ibis.satin.SharedObject
       implements SharedDataInterface
{
    private FileSequences querySequences;
    private FileSequences databaseSequences;

    public SharedData()
    {
    }

    public void updateQuerySeqs(FileSequences querySeqs)
    {
        querySequences = querySeqs;
    }

    public void updateDatabaseSeqs(FileSequences databaseSeqs)
    {
        databaseSequences = databaseSeqs;
    }

    public Vector getQuerySeqs(Vector seqsPointers)
    {
        Vector qS = new Vector();

        for(int i = 0; i < seqsPointers.size(); i++)
        {
            int position = (Integer)seqsPointers.get(i);
            Sequence currentSeq = (Sequence)querySequences.getSequences().get(position);

            qS.add(currentSeq);
        }

        return qS;
    }

    public Vector getDatabaseSeqs(Vector seqsPointers)
    {
        Vector dbS = new Vector();

        for(int i = 0; i < seqsPointers.size(); i++)
        {
            int position = (Integer)seqsPointers.get(i);
            Sequence currentSeq = (Sequence)databaseSequences.getSequences().get(position);

            dbS.add(currentSeq);
        }
          
        return dbS;
    }
}
//package dsearchDC_so;

public interface SharedDataInterface extends ibis.satin.WriteMethodsInterface
{    
    public void updateQuerySeqs(FileSequences querySeqs);
    public void updateDatabaseSeqs(FileSequences databaseSeqs);
}

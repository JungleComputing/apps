package geneSequencing.sharedObjects;

import geneSequencing.FileSequences;

public interface SharedDataInterface extends ibis.satin.WriteMethodsInterface
{    
    public void updateQuerySeqs(FileSequences querySeqs);
    public void updateDatabaseSeqs(FileSequences databaseSeqs);
}

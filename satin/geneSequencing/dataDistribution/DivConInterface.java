//package dsearchDC;

import java.util.ArrayList;
import java.util.Vector;

public interface DivConInterface extends ibis.satin.Spawnable {
    public ArrayList<ResSeq> spawn_splitQuerySequences(Vector workUnit);

    public ArrayList<ResSeq> spawn_splitDatabaseSequences(Vector workUnit);
}

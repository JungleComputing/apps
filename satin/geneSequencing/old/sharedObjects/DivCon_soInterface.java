//package dsearchDC_so;

import java.util.Vector;

public interface DivCon_soInterface extends ibis.satin.Spawnable
{
    public Vector spawn_splitQuerySeqs(Vector workUnit, SharedData sharedData);
    public Vector spawn_splitDatabaseSeqs(Vector workUnit, SharedData sharedData);
}
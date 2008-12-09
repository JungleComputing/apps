package mc_barnes.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ibis.ipl.IbisIdentifier;
import ibis.ipl.Location;

public class ClusterFinder {
    
    private final ArrayList<Set<IbisIdentifier>> clusters
            = new ArrayList<Set<IbisIdentifier>>();
    
    public ClusterFinder(IbisIdentifier[] members) {        
        Arrays.sort(members);
        
        Set<IbisIdentifier> clusterSet = new HashSet<IbisIdentifier>();
        
        clusters.add(clusterSet);
        clusterSet.add(members[0]);
        
        Location cluster = members[0].location().getParent();
        
        for (int i = 1; i < members.length; i++) {
            IbisIdentifier id = members[i];
            Location idCluster = id.location().getParent();
            if (! idCluster.equals(cluster)) {
                clusterSet = new HashSet<IbisIdentifier>();
                cluster = idCluster;
                clusters.add(clusterSet);
            }
            clusterSet.add(id);
        }        
    }
    
    public ArrayList<Set<IbisIdentifier>> getClusters() {
        return new ArrayList<Set<IbisIdentifier>>(clusters);
    }
    
    public int numClusters() {
        return clusters.size();
    }
    
    public Set<IbisIdentifier> getCluster(int id) {
        return new HashSet<IbisIdentifier>(clusters.get(id));
    }
}

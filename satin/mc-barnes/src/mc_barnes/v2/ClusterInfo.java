package mc_barnes.v2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ibis.ipl.IbisIdentifier;
import ibis.ipl.Location;

public class ClusterInfo {
    
    private final ArrayList<Set<IbisIdentifier>> clusters
            = new ArrayList<Set<IbisIdentifier>>();
    
    private int myClusterNo = 0;
    private int myClusterIndex = 0;
    private int myIndex = 0;
    private int[] indicesInCluster;
    
    public ClusterInfo(IbisIdentifier[] members, IbisIdentifier me) { 
        
        Arrays.sort(members);
        
        Set<IbisIdentifier> clusterSet = new HashSet<IbisIdentifier>();
        
        clusters.add(clusterSet);
        clusterSet.add(members[0]);
        
        indicesInCluster = new int[members.length];
        indicesInCluster[0] = 0;
        
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
            indicesInCluster[i] = clusterSet.size() - 1;
            if (id.equals(me)) {
                myClusterNo = clusters.size() - 1;
                myClusterIndex = clusterSet.size() - 1;
                myIndex = i;
            }
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
    
    public int myClusterNo() {
        return myClusterNo;
    }
    
    public int myIndexInCluster() {
        return myClusterIndex;
    }
    
    public int myClusterSize() {
        return clusterSize(myClusterNo);
    }
    
    public int clusterSize(int i) {
        return clusters.get(i).size();
    }
    
    public int myIndex() {
        return myIndex;
    }
}

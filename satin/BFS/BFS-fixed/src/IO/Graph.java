package IO;

import java.util.ArrayList;

/* This class represents a directed graph using adjacency list representation */
public final class Graph implements java.io.Serializable {
    public int V;   // No. of vertices
    private transient ArrayList<Integer> tmpAdj[]; // Adjacency Lists
    public int[][] adj;
    
    public Graph(int v) {
        V = v;
        tmpAdj = new ArrayList[v];

        // Add an adjacency list for every node
        for (int i=0; i<v; ++i)
            tmpAdj[i] = new ArrayList<Integer>();
    }

    // Function to add an edge to the graph
    public void addEdge(int v,int w) {
        tmpAdj[v].add(w);
    }
    
    // copy the array lists into arrays of the correct size
    public void done() {
        adj = new int[V][];
        for (int i=0; i<V; ++i) {
            adj[i] = new int[tmpAdj[i].size()];
            for(int j=0; j<adj[i].length; j++) {
                adj[i][j] = tmpAdj[i].get(j);
            }
        }

        tmpAdj = null; // free the memory
    }
}

package bfs;

import IO.Graph;

interface BFSInterface extends ibis.satin.Spawnable {
    public void search(Graph g, int node, int target, int count, boolean[] visited);
}
package bfs_so;

import IO.Graph;

interface BFS_SO_Interface extends ibis.satin.Spawnable {
    void search(Graph g, int node, int target, int count, Visited visited);
}
package bfs_so_ia;

import IO.Graph;

interface BFS_SO_IA_Interface extends ibis.satin.Spawnable {
    public void search(Graph g, int node, int target, int count, Visited visited) throws SearchResultFound;
}
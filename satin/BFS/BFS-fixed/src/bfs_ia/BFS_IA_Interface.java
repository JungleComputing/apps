package bfs_ia;

import IO.Graph;

interface BFS_IA_Interface extends ibis.satin.Spawnable {
    public void search(Graph g, int node, int target, int count, boolean[] visited) throws SearchResultFound;
}
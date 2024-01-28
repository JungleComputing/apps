package queue_bfs;


import IO.Graph;

import java.util.LinkedList;

interface QUEUEInterface extends ibis.satin.Spawnable {
    public void spawn_search(Graph g, int s, int target, boolean[] visited, LinkedList<Integer> queue, boolean[] finished);
}
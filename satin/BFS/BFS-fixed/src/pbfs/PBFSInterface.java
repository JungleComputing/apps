package pbfs;

import IO.Graph;

import java.util.LinkedList;

interface PBFSInterface extends ibis.satin.Spawnable {
    public void spawn_search(Frontier curr, Frontier next, Graph g, int target, boolean[] visited, int[] found);
}
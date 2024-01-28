package pbfs;

import IO.Graph;

import java.util.LinkedList;

public class Frontier implements java.io.Serializable {
    LinkedList<Integer> unvisited = new LinkedList<>();
    int N;

    public int getN() {
        return N;
    }

    public int getEdge(int n) {
        return unvisited.get(n);
    }

    public void load(int node, Graph g, boolean[] visited) {
        for (int n : g.adj[node]) {
            if (!visited[n]) {
                visited[n] = true;
                unvisited.add(n);
                N++;
            }
        }
    }

    public void set(int count, LinkedList<Integer> edges) {
        unvisited = edges;
        N = count;
    }

    public void split(Frontier other) {
        int half = N/2;
        LinkedList<Integer> temp1 = new LinkedList<>(unvisited.subList(0, half));
        LinkedList<Integer> temp2 = new LinkedList<>(unvisited.subList(half, N));
        unvisited = temp1;
        other.set(N - half, temp2);
        N = half;
    }

    public void merge(Frontier other) {
        unvisited.addAll(other.unvisited);
        N += other.N;
    }
}

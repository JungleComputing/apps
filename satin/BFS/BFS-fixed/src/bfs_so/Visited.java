package bfs_so;

final class Visited extends ibis.satin.SharedObject implements VisitedInterface, java.io.Serializable {
    private boolean[] visited;
    int iteration = 0;

    public void set(boolean[] v) {
        visited = v;
    }

    public boolean get(int i) {
        return visited[i];
    }

    public int getIteration() {
        return iteration;
    }

    public void update(int index, int count) {
        visited[index] = true;
        iteration = count;
    }
}

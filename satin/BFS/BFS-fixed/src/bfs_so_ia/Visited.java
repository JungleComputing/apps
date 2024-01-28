package bfs_so_ia;

final class Visited extends ibis.satin.SharedObject implements VisitedInterface {
    private boolean[] visited;
    int iteration = 0;

    public void set(boolean[] v) {
        visited = v;
    }

    public boolean get(int i) {
        return visited[i];
    }

    public void update(int index, int count) {
        visited[index] = true;
        iteration = count;
    }
}

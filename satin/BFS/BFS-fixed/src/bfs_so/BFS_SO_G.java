package bfs_so;

/* Java program for BFS traversal from a given source vertex to a target
 * Satin version
*/

import IO.Graph;
import IO.ReadFile;

import java.io.*;
import java.util.LinkedList;

// This class represents BFS using Satin on a graph
class BFS_SO_G extends ibis.satin.SatinObject implements BFS_SO_Interface, Serializable {

    // Guard: make sure that data represents the right iteration.
    public boolean guard_search(Graph g, int node, int target, int count, Visited visited) {
        return (visited.getIteration() == count - 1);
    }

    // Do BFS traversal.
    public void search(Graph g, int node, int target, int count, Visited visited) {
        // Mark the current node as visited
        visited.update(node, count);

        // If this is the target node throw the found-inlet
        if(node == target) {
            System.err.println("FOUND " + target);
            System.err.println("Number of nodes traveled in this branch: " + count);
            return;
        }

        // At some point, we should stop spawning new threads and just continue locally and sequentially. We have enough parallelism now.
        if(count > 1000) {
            // Create a queue for BFS.
            LinkedList<Integer> queue = new LinkedList<Integer>();
            queue.add(node);

            while (queue.size() != 0) {
                // Dequeue a node from queue.
                node = queue.poll();
                count++;

                // Check if this node is the target node.
                if (node == target) {
                    System.out.println("FOUND " + target);
                    System.out.println("Number of nodes traveled in this branch: " + count);

                    return;
                }

                // Get all adjacent nodes of the dequeued node s.
                // If an adjacent has not been visited, mark it visited and enqueue it.
                for (int n : g.adj[node]) {
                    if (!visited.get(n)) {
                        visited.update(n, count);
                        queue.add(n);
                    }
                }
            }
            return;
        }

        // For every adjacent node
        for (int i : g.adj[node]) {
            // If it is not visited
            if (!visited.get(i)) {
                // Try and spawn a BFS, and catch the found-inlet
                search(g, i, target, count + 1, visited);
            }
        }
        sync();
    }

    // Create the graph by reading a given file (while being timed)
    Graph createGraph(int number, String file, boolean directed) {
        System.out.println("Reading file and creating graph");
        long startTime = System.nanoTime();
        Graph g = new Graph(number);

        try {
            ReadFile.read(file, g, directed);
        } catch(Exception e) {
            System.err.println("Caught Exception: " + e.getMessage());
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Duration of reading file and creating graph: " + duration + "ns or " + duration / 1000000000.0 + "s\n");

        return g;
    }

    // Main function. test the BFS function by loading a graph and timing a BFS operation.
    public static void main(String args[]) {
        BFS_SO_G s = new BFS_SO_G();
        // Pause Satin while loading the graph, to avoid overhead.
        pause();

        int root = 0;
        int end = 301783;

//        String file = "files/p2p-Gnutella08.dat";
//        int number = 6301;
//        boolean directed = true;

//        String file = "files/roadNet-CA.dat";
////        int number = 1965206;
//        int number = 1980000;
//        boolean directed = false;

//        String file = "files/soc-LiveJournal1.dat";
//        int number = 4847571;
//        boolean directed = true;


//        String file = "files/Amazon0312.dat";
//        int number = 400727;
//        boolean directed = true;
        String file = "files/Amazon0505.dat";
        int number = 410236;
        boolean directed = true;
//        String file = "files/Amazon0601.dat";
//        int number = 403394;
//        boolean directed = true;
//        String file = "files/web-Google.dat";
//        int number = 875713;
//        boolean directed = true;

        Graph g = s.createGraph(number, file, directed);

        System.out.println("Following is Breadth First Traversal using Satin (With the use of Shared Objects and a Guard)"+
                "(starting from vertex " + root + ", searching vertex " + end + ")");

        // Resume Satin.
        resume();

        // Mark all the vertices as not visited (By default set as false)
        Visited visited = new Visited();
        visited.set(new boolean[g.V]);
        visited.exportObject();

        long startTime = System.nanoTime();

        // Run the BFS, and abort when the found-inlet is thrown
        s.search(g, root, end, 0, visited);
        s.sync();

        long endTime = System.nanoTime();

        long duration = (endTime - startTime);
        System.out.println("\nDuration of BFS: " + duration + "ns or " + duration / 1000000000.0 + "s");
    }
}

package queue_bfs;

import IO.Graph;
import IO.ReadFile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

// This class represents BFS on a graph using a queue
class BFS_queue extends ibis.satin.SatinObject implements QUEUEInterface, java.io.Serializable {

    public void spawn_search(Graph g, int s, int target, boolean[] visited, LinkedList<Integer> queue, boolean[] finished) {
        // Check if this node is the target node.
        if(s == target) {
            System.out.println("FOUND " + target);
            finished[0] = true;
        }

        // Get all adjacent nodes of the dequeued node s.
        // If an adjacent has not been visited, mark it visited and enqueue it.
        for (int n : g.adj[s]) {
            if (!visited[n]) {
                visited[n] = true;
                queue.add(n);
            }
        }
    }

    // Do BFS traversal.
    void search(Graph g, int s, int target) {
        // Keep track of the amount of nodes visited, for reference.
        int nodesTraveled = 0;

        // Mark all the vertices as not visited (By default set as false).
        boolean visited[] = new boolean[g.V];

        // Create a queue for BFS.
        LinkedList<Integer> queue = new LinkedList<Integer>();

        // Mark the current node as visited and enqueue it.
        visited[s] = true;
        queue.add(s);

        boolean[] finished = {false};

        // While the result has not been found
        while (true) {
            // If the queue is empty, sync to fill the queue.
            if(queue.size() == 0) {
                sync();

                // If still empty, break from the loop. The entire traversal is done.
                if (queue.size() == 0) {
                    System.out.println("FAILED after this number of nodes traveled: " + nodesTraveled);
                    break;
                }
            }

            if(finished[0]) {
                System.out.println("FOUND after this number of nodes traveled: " + nodesTraveled);
                break;
            }

            // Dequeue a node from queue.
            s = queue.poll();
            nodesTraveled++;

            spawn_search(g, s, target, visited, queue, finished);
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
        BFS_queue s = new BFS_queue();

        pause();

        int root = 0;
        int end = 301783;
        int res = -1;

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

        System.out.println("Following is Breadth First Traversal parallel, but with queue in Satin"+
                "(starting from vertex " + root + ", searching vertex " + end + ")");

        resume();

        long startTime = System.nanoTime();
        s.search(g, root, end);
        long endTime = System.nanoTime();

        long duration = (endTime - startTime);
        System.out.println("\nDuration of BFS: " + duration + "ns or " + duration / 1000000000.0 + "s");
    }
}

// This code is partly based on work by Aakash Hasija
// http://www.geeksforgeeks.org/breadth-first-traversal-for-a-graph/
// http://www.geeksforgeeks.org/depth-first-traversal-for-a-graph/
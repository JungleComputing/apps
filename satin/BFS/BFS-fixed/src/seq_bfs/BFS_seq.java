package seq_bfs;/* Java program for BFS traversal from a given source vertex to a target.
 * Sequential version.
*/

import IO.Graph;
import IO.ReadFile;

import java.io.*;
import java.util.*;

// This class represents BFS on a graph using a queue
class BFS_seq {
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

        while (queue.size() != 0) {
            // Dequeue a node from queue.
            s = queue.poll();
            nodesTraveled++;

            // Check if this node is the target node.
            if(s == target) {
                System.out.println("FOUND " + target);
                System.out.println("Number of nodes traveled: " + nodesTraveled);
                return;
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

        System.out.println("FAILED after this number of nodes traveled: " + nodesTraveled);
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
        BFS_seq s = new BFS_seq();

        if(args.length != 2) {
            System.out.println("Not the right amount of arguments. Needed 2 (first is the number of the dataset, second is the full or all test.)");
            return;
        }

        String file;
        int number;
        boolean directed;

        if(args[0].equals("0")) {
            file = "files/p2p-Gnutella08.dat";
            number = 6301;
            directed = true;
        } else if(args[0].equals("1")) {
            file = "files/roadNet-CA.dat";
            // int number = 1965206;
            number = 1980000;
            directed = false;
        } else if(args[0].equals("2")) {
            file = "files/soc-LiveJournal1.dat";
            number = 4847571;
            directed = true;
        } else if(args[0].equals("3")) {
            file = "files/Amazon0312.dat";
            number = 400727;
            directed = true;
        } else if(args[0].equals("4")) {
            file = "files/Amazon0505.dat";
            number = 410236;
            directed = true;
        } else if(args[0].equals("5")) {
            file = "files/Amazon0601.dat";
            number = 403394;
            directed = true;
        } else if(args[0].equals("6")) {
            file = "files/web-Google.dat";
            number = 875713;
            directed = true;
        } else {
            System.out.println("First argument not correct. Needed a number, to select the right dataset.");
            return;
        }

        Graph g = s.createGraph(number, file, directed);

        if(args[1].equals("full")) {
            int root = 0;
            int end = number + 1;
            System.out.println("Following is Sequential Breadth First Traversal to find a node not in the graph " +
                    "(starting from vertex " + root + " to traverse the entire graph)");

            long startTime = System.nanoTime();
            s.search(g, root, end);
            long endTime = System.nanoTime();

            long duration = (endTime - startTime);
            System.out.println("\nDuration of BFS: " + duration + "ns or " + duration / 1000000000.0 + "s");
        }
        else if(args[1].equals("all")) {
            System.out.println("Following are 64 Sequential Breadth First Traversal to find a random node " +
                    "starting from a random node, to test the BFS.");

            Random rand = new Random();
            int root, end;
            long startTime = 0;
            long endTime = 0;

            for(int i = 0; i < 64; i++) {
                root = rand.nextInt(number);
                end = rand.nextInt(number);

                System.out.print("Search: " + root + " - " + end + ". ");

                startTime += System.nanoTime();
                s.search(g, root, end);
                endTime += System.nanoTime();
            }

            long duration = (endTime - startTime);
            System.out.println("\nDuration of 64 BFS': " + duration + "ns or " + duration / 1000000000.0 + "s");
            System.out.println("\nAverage duration of BFS: " + duration / 64.0 + "ns or " + duration / 64.0 / 1000000000.0 + "s");
        }
        else {
            System.out.println("Second argument not correct. Needed 'full' for an entire traversal of the graph, or 'all' to test a couple different searches.");
        }
    }
}

// This code is partly based on work by Aakash Hasija
// http://www.geeksforgeeks.org/breadth-first-traversal-for-a-graph/
// http://www.geeksforgeeks.org/depth-first-traversal-for-a-graph/
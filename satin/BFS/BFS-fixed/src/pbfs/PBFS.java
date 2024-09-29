package pbfs;


import IO.Graph;
import IO.ReadFile;

import java.io.*;
import java.util.LinkedList;

// This class represents BFS using Satin on a graph
class PBFS extends ibis.satin.SatinObject implements PBFSInterface, Serializable {
    int THRESHOLD = 50;

    public void search(Graph g, int node, int target, int[] found, boolean[] visited) {
        Frontier curr = new Frontier();
        Frontier next = new Frontier();

        curr.load(node, g, visited);

        while(curr.getN() != 0) {
            spawn_search(curr, next, g, target, visited, found);
            sync();
            curr = next;
            next = new Frontier();
        }
    }

    public void seq(Frontier curr, Frontier next, Graph g, int target, boolean[] visited, int[] found) {
        for(int i = 0; i < curr.getN(); i++) {
            int node = curr.getEdge(i);
            found[1]++;

            if(node == target) {
                System.err.println("\nFOUND!");
                found[0] = found[1];
                return;
            }

            next.load(node, g, visited);
        }
    }

    // Do BFS traversal.
    public void spawn_search(Frontier curr, Frontier next, Graph g, int target, boolean[] visited, int[] found) {
        if(curr.getN() < THRESHOLD) {
            seq(curr, next, g, target, visited, found);
        } else {
            Frontier curr2 = new Frontier();
            Frontier next2 = new Frontier();

            curr.split(curr2);

            spawn_search(curr, next, g, target, visited, found);
            spawn_search(curr2, next2, g, target, visited, found);
            sync();

            next.merge(next2);
        }
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
        PBFS s = new PBFS();
        // Pause Satin while loading the graph, to avoid overhead.
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

        System.out.println("Following is Breadth First Traversal using Satin PBFS"+
                "(starting from vertex " + root + ", searching vertex " + end + ")");

        // Resume Satin.
        resume();

        // Mark all the vertices as not visited (By default set as false)
        boolean[] visited = new boolean[g.V];

        long startTime = System.nanoTime();

        // Run the BFS, and abort when the found-inlet is thrown
        int[] found = {-1, 0};
        s.search(g, root, end, found, visited);

        long endTime = System.nanoTime();

        long duration = (endTime - startTime);
        if(found[0] >= 0) {
            System.out.println("FOUND on node " + found[0] + " In total traveled " + found[1]);
        } else {
            System.out.println("In total traveled " + found[1]);
        }

        System.out.println("\nDuration of BFS: " + duration + "ns or " + duration / 1000000000.0 + "s");
    }
}
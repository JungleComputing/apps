package test;

import IO.ReadFile;
import IO.Graph;

import bfs.BFS;
import bfs_ia.BFS_IA;

public class Test extends ibis.satin.SatinObject implements java.io.Serializable{

    public void Test_all_full(String[] files, int[] lengths, boolean[] directed) {
        Graph g;
        BFS bfs = new BFS();
        BFS_IA bfs_ia = new BFS_IA();
        for(int i = 0; i < files.length; i++) {
            g = new Graph(lengths[i]);

            try {
                ReadFile.read(files[i], g, directed[i]);
            } catch (Exception e) {
                System.err.println("Caught Exception: " + e.getMessage());
                continue;
            }

            bfs.search(g, 0, lengths[i] + 1, lengths[i], new boolean[lengths[i]]);
            sync();
            try {
                bfs_ia.search(g, 0, lengths[i] + 1, 0, new boolean[lengths[i]]);
                sync();
            } catch (bfs_ia.SearchResultFound e) {
                System.err.println("Found at ...");
            }
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


    public static void main(String[] args) {
        Test test = new Test();
        String[] files = {"files/Amazon0312.dat", "files/Amazon0505.dat", "files/Amazon0601.dat"};
        int[] lengths = {400727, 410236, 403394};
        boolean[] directed = {true, true, true};

        test.Test_all_full(files, lengths, directed);
    }
}

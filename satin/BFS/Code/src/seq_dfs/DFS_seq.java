/*
 * Copyright 2017 Patrick Goddijn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package seq_dfs;

/* Java program for DFS traversal from a given source vertex to a target.
 * Sequential version.
*/

import IO.Config;
import IO.Graph;
import IO.ReadFile;

import java.io.*;
import java.util.*;

// This class represents DFS on a graph using a queue
public final class DFS_seq {
    // Do DFS traversal.
    int search(Graph g, int s, int target) {
        // Keep track of the amount of nodes visited, for reference.
        int nodesTraveled = 0;

        // Mark all the vertices as not visited (By default set as false).
        boolean visited[] = new boolean[g.V];

        // Create a queue for DFS.
        LinkedList<Integer> stack = new LinkedList<Integer>();

        // Mark the current node as visited and enqueue it.
        visited[s] = true;
        stack.push(s);

        while (stack.size() != 0) {
            // Dequeue a node from queue.
            s = stack.pop();
            nodesTraveled++;

            // Check if this node is the target node.
            if(s == target) {
                System.out.println("FOUND " + target);
                System.out.println("Number of nodes traveled: " + nodesTraveled);
                return nodesTraveled;
            }

            // Get all adjacent nodes of the dequeued node s.
            // If an adjacent has not been visited, mark it visited and enqueue it.
            for (int n : g.adj[s]) {
                if (!visited[n]) {
                    visited[n] = true;
                    stack.push(n);
                }
            }
        }

        System.out.println("FAILED after this number of nodes traveled: " + nodesTraveled);
        return nodesTraveled;
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

    // Main function. test the DFS function by loading a graph and timing a DFS operation.
    public static void main(String args[]) {
        try {
            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("output2.txt", true)), true));
        } catch (Exception e) {
            System.out.println("Caught Exception: " + e.getMessage());
        }

        DFS_seq s = new DFS_seq();

        if(args.length != 2) {
            System.out.println("Not the right amount of arguments. Needed 2 (first is the number of the dataset, second is the full or all test.)");
            return;
        }

        String file;
        int number;
        boolean directed;
        int starter = 0;

        if (args[0].equals("0")) {
            file = "files/p2p-Gnutella08.dat";
            number = 6301;
            directed = true;
        } else if (args[0].equals("1")) {
            file = "files/Amazon0312.dat";
            number = 400727;
            directed = true;
        } else {
            System.out.println("First argument not correct. Needed a number, to select the right dataset.");
            return;
        }

        Graph g = s.createGraph(number, file, directed);

        if(args[1].equals("full")) {
            int root = starter;
            int end = number + 1;
            System.out.println("Following is Sequential Depth First Traversal to find a node not in the graph " +
                    "(starting from vertex " + root + " to traverse the entire graph)");

            long startTime = System.nanoTime();
            int x = s.search(g, root, end);
            long endTime = System.nanoTime();

            long duration = (endTime - startTime);
            System.out.println("\nDuration of DFS: " + duration + "ns or " + duration / 1000000000.0 + "s");
        }
        else if(args[1].equals("all")) {
            System.out.println("Following are 64 Sequential Depth First Traversal to find a random node " +
                    "starting from a random node, to test the DFS.");

            Random rand = new Random();
            int root, end;
            long startTime;
            long endTime;
            long duration = 0;
            int total = 0;

            for(int i = 0; i < 64; i++) {
                root = rand.nextInt(number);
                end = rand.nextInt(number);

                // Avoid picking to trivial numbers
                while(g.adj[root].length == 0) {
                    root = rand.nextInt(number);
                }

                while(g.adj[end].length == 0) {
                    end = rand.nextInt(number);
                }

                System.out.println("Search: " + root + " - " + end + ". ");

                startTime = System.nanoTime();
                total += s.search(g, root, end);
                endTime = System.nanoTime();
                duration += (endTime - startTime);
            }

            System.out.println("\nDuration of 64 DFS': " + duration + "ns or " + duration / 1000000000.0 + "s");
            System.out.println("\nAverage duration of DFS: " + duration / 64.0 + "ns or " + duration / 64.0 / 1000000000.0 + "s");
            System.out.println("\nTotal nodes traveled by DFS: " + total + " and Average nodes traveled " + total / 64.0);
        }
        else if(args[1].equals("par")) {
            System.out.println("Following are " + Config.NR_PARALLEL_SEARCHES + " Sequential Depth First Traversal to find a random node " +
                    "starting from a random node, to test the DFS.");

            Random rand = new Random();
            int root[] = new int[Config.NR_PARALLEL_SEARCHES];
            int end[] = new int[Config.NR_PARALLEL_SEARCHES];
            long startTime;
            long endTime;
            long duration;
            int total = 0;

            startTime = System.nanoTime();
            for(int i = 0; i < Config.NR_PARALLEL_SEARCHES; i++) {
                root[i] = rand.nextInt(number);
                end[i] = rand.nextInt(number);

                // Avoid picking to trivial numbers
                while (g.adj[root[i]].length == 0) {
                    root[i] = rand.nextInt(number);
                }

                while (g.adj[end[i]].length == 0) {
                    end[i] = rand.nextInt(number);
                }
            }

            for(int i = 0; i < Config.NR_PARALLEL_SEARCHES; i++) {
                System.out.println("Search: " + root[i] + " - " + end[i] + ". ");

                total += s.search(g, root[i], end[i]);
            }

            endTime = System.nanoTime();
            duration = (endTime - startTime);

            System.out.println("\nDuration of " + Config.NR_PARALLEL_SEARCHES + " Search': " + duration + "ns or " + duration / 1000000000.0 + "s");
            System.out.println("\nTotal nodes traveled by DFS: " + total + " and Average nodes traveled " + total / (Config.NR_PARALLEL_SEARCHES * 1.0));
        }
        else {
            System.out.println("Second argument not correct. Needed 'full' for an entire traversal of the graph, or 'all' to test a couple different searches.");
        }
    }
}

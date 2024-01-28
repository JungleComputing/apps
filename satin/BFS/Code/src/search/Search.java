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

package search;

/* Java program for Search traversal from a given source vertex to a target
 * Satin version
*/

import IO.Graph;
import IO.ReadFile;

import java.io.*;
import java.util.LinkedList;
import java.util.Random;


// This class represents Search using Satin on a graph
public final class Search extends ibis.satin.SatinObject implements SearchInterface, Serializable {

    static int nodesVisited = 0;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { System.out.println("total number of nodes visited on this machine: " + nodesVisited); }
        });
    }
    
    // Do Search traversal.
    public void search(Graph g, int node, int target, int count, boolean[] visited) {
        // Mark the current node as visited
        visited[node] = true;
        nodesVisited++;

        // If this is the target node throw the found-inlet
        if(node == target) {
            System.out.println("FOUND " + target);
            System.out.println("Number of nodes traveled in this branch: " + count);
            return;
        }

        // At some point, we should stop spawning new threads and just continue locally and sequentially. We have enough parallelism now.
        if(count > IO.Config.MAX_SPAWN_DEPTH) {

            // Create a queue for Search.
            LinkedList<Integer> queue = new LinkedList<Integer>();
            queue.add(node);

            while (queue.size() != 0) {
                // Dequeue a node from queue.
                node = queue.poll();
                count++;
                nodesVisited++;

                // Check if this node is the target node.
                if (node == target) {
                    System.out.println("FOUND " + target);
                    System.out.println("Number of nodes traveled in this branch: " + count);

                    return;
                }

                // Get all adjacent nodes of the dequeued node s.
                // If an adjacent has not been visited, mark it visited and enqueue it.
                for (int n : g.adj[node]) {
                    if (!visited[n]) {
                        visited[n] = true;
                        queue.add(n);
                    }
                }
            }
            return;
        }

        // For every adjacent node
        for (int i : g.adj[node]) {
            // If it is not visited
            if (!visited[i]) {
                // Spawn a Search
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
            System.out.println("Caught Exception: " + e.getMessage());
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Duration of reading file and creating graph: " + duration + "ns or " + duration / 1000000000.0 + "s\n");

        return g;
    }

    // Main function. Test the Search function by loading a graph and timing a Search operation.
    public static void main(String args[]) {
        try {
            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("output.txt", true)), true));
        } catch (Exception e) {
            System.out.println("Caught Exception: " + e.getMessage());
        }

        Search s = new Search();

        // Pause Satin while loading the graph, to avoid overhead.
        pause();

        if (args.length != 2) {
            System.out.println("Not the right amount of arguments. Needed 2 (first is the number of the dataset, second is the full or all test.)");
            resume();
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
            resume();
            return;
        }
        
        Graph g = s.createGraph(number, file, directed);

        if(args[1].equals("full")) {
            // Mark all the vertices as not visited (By default set as false)
            boolean[] visited = new boolean[g.V];

            int root = starter;
            int end = number + 1;
            System.out.println("Following is Satin Breadth First Traversal to find a node not in the graph " +
                    "(starting from vertex " + root + " to traverse the entire graph)");

            // Resume Satin
            resume();

            long startTime = System.nanoTime();
            s.search(g, root, end, 0, visited);
            s.sync();
            long endTime = System.nanoTime();

            long duration = (endTime - startTime);
            System.out.println("\nDuration of Search: " + duration + "ns or " + duration / 1000000000.0 + "s");
        }
        else if(args[1].equals("all")) {
            System.out.println("Following are " + IO.Config.NR_PARALLEL_SEARCHES + " Satin Breadth First Traversals to find a random node " +
                    "starting from a random node, to test the Search.");

            Random rand = new Random();
            int root, end;
            long startTime;
            long endTime;
            long duration = 0;
            boolean[] visited;

            // Resume Satin
            resume();

            for(int i = 0; i < IO.Config.NR_PARALLEL_SEARCHES; i++) {
                // Mark all the vertices as not visited (By default set as false)
                visited = new boolean[g.V];
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
                s.search(g, root, end, 0, visited);
                s.sync();
                endTime = System.nanoTime();
                duration += (endTime - startTime);
            }

            System.out.println("\nDuration of "  + IO.Config.NR_PARALLEL_SEARCHES + " Search': " + duration + "ns or " + duration / 1000000000.0 + "s");
            System.out.println("\nAverage duration of Search: " + duration / IO.Config.NR_PARALLEL_SEARCHES + "ns or " + duration / IO.Config.NR_PARALLEL_SEARCHES / 1000000000.0 + "s");
        }
        else if(args[1].equals("par")) {
            System.out.println("Following are "  + IO.Config.NR_PARALLEL_SEARCHES + " Satin Breadth First Traversals (with inlet/abort) to find a random node " +
                    "starting from a random node, to test the Search. They are all done in parallel");

            Random rand = new Random();
            int root[] = new int[IO.Config.NR_PARALLEL_SEARCHES];
            int end[] = new int[IO.Config.NR_PARALLEL_SEARCHES];
            long startTime;
            long endTime;
            long duration = 0;
            boolean visited[][] = new boolean[IO.Config.NR_PARALLEL_SEARCHES][g.V];

            for(int j = 0; j < IO.Config.NR_PARALLEL_SEARCHES; j++) {
                root[j] = rand.nextInt(number);
                end[j] = rand.nextInt(number);

                // Avoid picking to trivial numbers
                while(g.adj[root[j]].length == 0) {
                    root[j] = rand.nextInt(number);
                }

                while(g.adj[end[j]].length == 0) {
                    end[j] = rand.nextInt(number);
                }
            }

            // Resume Satin
            resume();

            startTime = System.nanoTime();
            for(int i = 0; i < IO.Config.NR_PARALLEL_SEARCHES; i++) {
                System.out.println("Search: " + root[i] + " - " + end[i] + ". ");

                // Run the Search
                s.search(g, root[i], end[i], 0, visited[i]);
            }
            s.sync();

            endTime = System.nanoTime();
            duration += (endTime - startTime);

            System.out.println("\nDuration of "  + IO.Config.NR_PARALLEL_SEARCHES + " Search': " + duration + "ns or " + duration / 1000000000.0 + "s");
        }
        else {
            System.out.println("Second argument not correct. Needed 'full' for an entire traversal of the graph, or 'all' to test a couple different searches.");
            resume();
        }
    }
}

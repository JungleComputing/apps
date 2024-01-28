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

package search_ia_so;

/* Java program for Search traversal from a given source vertex to a target
 * Satin version
*/

import IO.Config;
import IO.Graph;
import IO.ReadFile;

import java.io.*;
import java.util.LinkedList;
import java.util.Random;

// This class represents Search using Satin on a graph
public final class Search_IA_SO extends ibis.satin.SatinObject implements Search_IA_SO_Interface, Serializable {

    static int nodesVisited = 0;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() { System.out.println("total number of nodes visited on this machine: " + nodesVisited); }
        });
    }

    // Do Search traversal.
    public void search(Graph g, int node, int target, int count, Visited visited) throws SearchResultFound {
        // Mark the current node as visited
        visited.update(node);
        nodesVisited++;

        // If this is the target node throw the found-inlet
        if(node == target) {
            System.out.println("FOUND " + target);
            System.out.println("Number of nodes traveled in this branch: " + count);
            throw new SearchResultFound(count);
        }

        // At some point, we should stop spawning new threads and just continue locally and sequentially. We have enough parallelism now.
        if(count > Config.MAX_SPAWN_DEPTH) {
            // Create a queue for Search.
            LinkedList<Integer> queue = new LinkedList<Integer>();
            queue.add(node);
            int[] updates = new int[Config.SHARE_UPDATE];
            int update_counter = 0;

            while (queue.size() != 0) {
                // Dequeue a node from queue.
                node = queue.poll();
                count++;
                nodesVisited++;

                // Check if this node is the target node.
                if (node == target) {
                    System.out.println("FOUND " + target);
                    System.out.println("Number of nodes traveled in this branch: " + count);

                    throw new SearchResultFound(count);
                }

                // Get all adjacent nodes of the dequeued node s.
                // If an adjacent has not been visited, mark it visited and enqueue it.
                for (int n : g.adj[node]) {
                    if (!visited.get(n)) {
                        visited.localUpdate(n);
                        queue.add(n);

                        updates[update_counter] = node;
                        update_counter++;

                        if(update_counter == Config.SHARE_UPDATE) {
                            visited.update_list(updates);
                            updates = new int[Config.SHARE_UPDATE];
                            update_counter = 0;
                        }
                    }
                }
            }
            return;
        }

        // For every, non visited, adjacent node: spawn a search.
        for (int i : g.adj[node]) {
            if (!visited.get(i)) {
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

    // Main function. test the Search function by loading a graph and timing a Search operation.
    public static void main(String args[]) {
        try {
            System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("output.txt", true)), true));
        } catch (Exception e) {
            System.out.println("Caught Exception: " + e.getMessage());
        }

        Search_IA_SO s = new Search_IA_SO();

        // Pause Satin while loading the graph, to avoid overhead.
        pause();

        if(args.length != 2) {
            System.out.println("Not the right amount of arguments. Needed 2 (first is the number of the dataset, second is the full or all test.)");
            resume();
            return;
        }

        String file;
        int number;
        boolean directed;
        int res = -1;

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
            Visited visited = new Visited();
            visited.set(new boolean[g.V]);
            visited.exportObject();

            int root = 0;
            int end = number + 1;
            System.out.println("Following is Satin Breadth First Traversal (with inlet/abort and shared objects) to find a node not in the graph " +
                    "(starting from vertex " + root + " to traverse the entire graph)");

            // Resume Satin
            resume();

            long startTime = System.nanoTime();
            // Run the Search, and abort when the found-inlet is thrown
            try {
                s.search(g, root, end, 0, visited);
                s.sync();
            } catch (SearchResultFound x) {
                // We come here only if a result is found
                s.abort(); // kill other jobs that might still be running.
                res = x.count;
                return; // return needed because inlet is handled in separate thread.
            }
            long endTime = System.nanoTime();

            long duration = (endTime - startTime);
            System.out.println("\nDuration of Search: " + duration + "ns or " + duration / 1000000000.0 + "s");
        }
        else if(args[1].equals("all")) {
            System.out.println("Following are 64 Satin Breadth First Traversals (with inlet/abort and shared objects) to find a random node " +
                    "starting from a random node, to test the Search.");

            Random rand = new Random();
            int root, end;
            long startTime;
            long endTime;
            long duration = 0;
            Visited visited;

            // Resume Satin
            resume();

            for(int i = 0; i < 64; i++) {
                // Mark all the vertices as not visited (By default set as false)
                visited = new Visited();
                visited.set(new boolean[g.V]);
                visited.exportObject();

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

                // Run the Search, and abort when the found-inlet is thrown
                try {
                    s.search(g, root, end, 0, visited);
                    s.sync();
                } catch (SearchResultFound x) {
                    // We come here only if a result is found
                    s.abort(); // kill other jobs that might still be running.
                    res = x.count;
                    return; // return needed because inlet is handled in separate thread.
                }

                endTime = System.nanoTime();
                duration += (endTime - startTime);

                if(res >= 0) {
                    System.out.println("found after visiting this many nodes: " + res);
                } else {
                    System.out.println("element not found");
                }
            }

            System.out.println("\nDuration of 64 Search': " + duration + "ns or " + duration / 1000000000.0 + "s");
            System.out.println("\nAverage duration of Search: " + duration / 64.0 + "ns or " + duration / 64.0 / 1000000000.0 + "s");
        }
        else {
            System.out.println("Second argument not correct. Needed 'full' for an entire traversal of the graph, or 'all' to test a couple different searches.");
            resume();
        }
    }
}

The Traveling Salesperson Problem (TSP) computes the shortest path for a
salesperson to visit all cities in a given set exactly once, starting in one
specific city.
We use a branch-and-bound algorithm, which can prune a large part of
the search space. When the length of the current partial route and the
minimal length of a path connecting the remaining cities together are
already longer than the best known solution, the path can be pruned.

The program is parallelized by distributing the search space over the
different processors. Load distribution is done automatically by the Satin
runtime system, by letting idle nodes steal work from nodes that still
have work.  The global minimum is implemented as a replicated object.

The program options are:

[ -seed <seed> ] [ -bound <bound> ] [ -v ] <ntowns>

where <seed> is a random number generator initializator, <bound> is an initial
upperbound of the length of the route, and <ntowns> is the number of cities.
The -v option makes the program (quite) verbose.

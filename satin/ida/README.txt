Iterative Deepening A* (IDA) is a combinatorial seach algorithm, based on
repeated depth-first searches. IDA* tries to find all shortest solution
paths to a given problem by doing a depth-first search up to a certain
maximum depth. If the search fails, it is repeated with a higher search
depth, until a solution is found.
This example program solves the 15-puzzle. From each game position, three
new positions are possible, and examined in parallel by spawning the search
method.

Program options: [ -max <max> ] [ -f <filename> | <length> ]

The starting position is either read from the file specified or generated
by starting with the solution and then shuffling the blanc around in cycles,
using two alternating cycling dimensions, and using <length> steps.
<max> specifies the maximum search depth.

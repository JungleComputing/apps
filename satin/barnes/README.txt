The Barnes-Hut algorithm simulates the evolution of a large set of bodies
under the influence of forces. It can be applied to various domains of
scientific computing, including but not limited to astrophysics, fluid
dynamics, electrostatics and even computer graphics. In our implementation,
the bodies represent planets and stars in the universe. The forces are
calculated using Newton's gravitational theory.

The evolution of the bodies is simulated in discrete time steps. Each of
these time steps corresponds to an iteration of the Barnes-Hut algorithm.
An iteration consists of, among others, a force calculation phase, in which
the force exerted on each body by all other bodies is computed, and
an update phase, in which the new position and velocity is computed
for each body. If
all pairwise interactions between two bodies are computed, the complexity
is O(N^2).

The Barnes-Hut algorithm reduces this complexity to
O(N log N), by using a hierarchical technique.
During the calculation of the forces exerted on a certain body,
this technique exploits the fact that a group of bodies which is
relatively far away from that body can be approximated by a single
virtual body at the center of mass of the group of bodies. This way,
many pairwise interactions do not have to be computed. The precision
factor ``theta'' indicates if a group of bodies is far enough away to use
the optimization.

To make this optimization possible, the bodies are organized in a tree
structure that represents the space the bodies are in. Since the space
we represent is a three dimensional universe, we use an oct-tree to
represent it. The bodies are located in the leaf nodes of the tree.
The tree is recursively subdivided as more bodies are added.

The precision factor theta indicates when a part of the tree
with bodies is approximated by its center of mass. The approximation is
done if the distance to the part of the tree is greater than 
the size of this part of the tree multiplied by theta.
The size of a part of the tree is the length of a side of the cube
represented by the part of the tree.

In each iteration, the tree structure has to be rebuilt
because the bodies have moved to a different part of space. The center of
mass fields of the internal nodes of the tree also have to be computed
each iteration, before the force calculation starts. An iteration thus
consists of the following four phases:

- Tree construction
- Center of mass calculation
- Force calculation
- Body update (calculate new position and velocity)

Since typically more than 90% of the execution time is spent
in the force calculation phase, we chose to parallelize only this
phase. The other three phases are executed sequentially.

The program in fact implements four different versions of the Barnes-Hut
algorithm; see satinbarnes.pdf for details.

-ntc
    use the "necessary tree construction" version.
-seq
    use the 'naive' version.

Other recognized options are:
-v
    make the program verbose
-no-v
    make the program unverbose
-viz
    turn on the vizualizer
-no-viz
    turn off the vizualizer
-min <num>
    specifies the number of bodies below which no jobs are spawned anymore,
    t.i., if a BodyTreeNode has more than <num> bodies beneath it, it is split
    up in separate jobs one more level.

In addition, two integer parameters must be given: the number of bodies,
and the maximum number of bodies per leaf node.

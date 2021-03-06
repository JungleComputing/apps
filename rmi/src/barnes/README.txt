Barnes-Hut is an O(N log N) N-body simulation. This RMI version is based
on the implementation of Blackston and Suel. It is optimized for
distributed-memory architectures. Instead of finding out at runtime which
bodies are needed to compute an interaction, as in the SPLASH-2 version of
Barnes-Hut, this code precomputes where bodies are needed, and sends them in
one collective communication phase before the actual computation starts.

The program options are:
-D
    Use a direct (N^2) acceleration computation method.

-M<num>
    Set the maximum number of bodies per leaf node to <num>.

-N<num>
    Set the total number of bodies to <num>.

-procs <num>
    Use <num> threads for the computation. The default is the number of processors
    on which the application is started.

-tstop <flt>
    Set the end-time to <flt>.

-dtime <flt>
    Set the time step interval to <flt>.

-eps <flt>
    Set epsilon to <flt>.

-distributed
    Run on multiple hosts. The default is to run multithreaded on one host.
    
-serialize
    Pack all data to be sent into arrays of a primitive type, instead of leaving
    this to Java object serialization.

-trim-arrays
    When sending arrays of bodies, the default method is to have a pre-allocated array,
    which has the disadvantage that often too many bodies are sent. With this option,
    the part of the pre-allocated array that must be sent is copied into an array of
    exactly the right size, so that less data is sent, at the cost of an extra copy.

-threads
    Use separate threads for communication.

-gc-interval <num>
    Force a GC every <num> iterations.

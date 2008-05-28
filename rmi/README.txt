This directory contains a collection of Ibis RMI example programs, organized
into a number of sub-directories (some of which may not be present in your
Ibis installation). See the README files in each sub-directory for more
details on the application. The "shared" directory contains some common
code for all these applications.

Some low-level benchmarks and tests:

throughput
    Obsoleted by the rmi-bench program.
latency
    This is a very simple latency measurement program.

Some applications:

barnes
    Barnes-Hut is an O(N log N) N-body simulation. This RMI version is based
    on the implementation of Blackston and Suel.

Other files in this directory are:

build.xml
    Ant build file for building Ibis RMI applications.
    "ant build" (or simply: "ant") will build all applications that
    are present in this directory. "ant clean" will remove
    what "ant build" made.

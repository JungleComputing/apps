This directory contains several implementations of a solver for the
Satisfiability (SAT) problem. Some variants are very simple but
highly parallel, others are more sophisticated, but are more difficult
to parallelize.

All variants take exactly one argument: the name of the problem file.
This file should be in DIMACS file format. That file format is described
in the following document:
www.intellektik.informatik.tu-darmstadt.de/SATLIB/Benchmarks/SAT/satformat.ps

Note that none of the solvers here is comparable to professional SAT
solvers.

The variants:

SATSolver.java		Most advanced version of the solver.
SeqSolver.java          Sequential version of the SATSolver.
DPLLSolver.java         A simple solver variant without a learning mechanism.
Breeder.java            Genetic optimizer of SAT.
SimpleSATSolver.java	Simple, highly parallel solver.

All solver variants except Breeder require the name of a single SAT
problem file.  A large collection is provided in `examples'.  In
particular `examples/benchmark-suite' is useful testing purposes.
Gzip compressed SAT problems are also accepted, provided that they
are stored in a file that ends with `.gz'.

DPLLSolver and SATSolver require the -satin-closed option for all
parallel runs. SATSolver also requires the -Dsatin.tuplespace.numbered=true
option. We realize that a compulsory option should be handled by
the software itself, but for the moment we cannot resolve the
technical problems with that.

The Breeder program takes a list of SAT problems of arbitrary length.

More concretely:

To build the sequential versions:

$ ant clean compile

compiles a sequential version that can be executed with:

$ ~/ibis/bin/ibis-run SATSolver examples/benchmark-suite/qg5-10.cnf

or simpler, run with:

$ ./runseq SATSolver examples/benchmark-suite/qg5-10.cnf

For parallel compilation you must do:

$ ant clean build

To execute it, you need to first start an ibis name server with:

$ ~/ibis/bin/ibis-nameserver [-port NAMESERVER_PORT]

where NAMESERVER_PORT is an arbitrary port number (it is wise to choose
something over 1000).

After the name server has been started you can run it on just your
local processor with:

$ ~/ibis/bin/ibis-run [-ns-port NAMESERVER_PORT] -ns NAMESERVER_HOSTNAME -nhosts 1 -Dsatin.tuplespace.numbered=true SATSolver examples/benchmark-suite/qg5-10.cnf -satin-closed

To run it in parallel on the DAS2 you need to start the ibis-nameserver
on the DAS2 fs0 processor (see above), and you can submit a job with:

$ prun -1 -v ~/ibis/bin/ibis-prun [-ns-port NAMESERVER_PORT] -ns NAMESERVER_HOSTNAME -Dsatin.tuplespace.numbered=true SATSolver examples/benchmark-suite/qg5-10.cnf -satin-closed

Where `2' is the number of processors for this particular job.

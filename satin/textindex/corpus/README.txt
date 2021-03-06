This directory contains several implementations of a solver for the
Satisfiability (SAT) problem. Some variants are very simple but
highly parallel, others are more sophisticated, but are more difficult
to parallelize.

All variants take exactly one argument: the name of the problem file.
This file should be in DIMACS file format, see
www.intellektik.informatik.tu-darmstadt.de/SATLIB/Benchmarks/SAT/satformat.ps

Note that none of the solvers here is comparable to professional SAT
solvers.

The variants:

SATSolver.java		Most advanced version of the solver.
SeqSolver.java          Sequential version of the SATSolver.
DPLLSolver.java         A simple solver variant without a learning mechanism.
Breeder.java            Genetic optimizer of SAT.
SimpleSATSolver.java	Simple, highly parallel solver.

All variants except Breeder take exactly one parameter: a SAT problem
to solve. A large collection is provided in examples.  In particular
examples/benchmark-suite is useful testing purposes.

The Breeder program takes a list of SAT problems of arbitrary length.

All variants also accept gzip compressed SAT problems, provided that
they are stored in a file that ends with `.gz'.

Adaptive numerical integration. The basic idea is that f(x) is replaced
by a straight line from (a, f(a)) to (b, f(b)) and the integral is
approximated by computing the area bounded by the resulting trapezoid.
This process is recursively continued for two subintervals as long as the
area differs significantly from the sum of the areas of the subintervals'
trapezoids.

This program uses f(x) = 0.1 * x * sin(x)

It takes three arguments: the lower x and upper x of the interval,
and an epsilon to decide wether to split some more.
For instance: 0 400 0.0001

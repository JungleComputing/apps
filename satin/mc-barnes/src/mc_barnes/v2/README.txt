This version uses the highthroughput multicast package to broadcast the
result of each processor. The master knows when an iteration is finished,
and it then does a small broadcast that enables the nodes to broadcast their
updates. The nodes are divided into clusters, and for each cluster a separate
broadcast channel is created. This is needed, because each cluster must,
in total, contain the complete data of its broadcast.

This program measures multicast latency.
Each CPU in turn sends <count> requests to CPU 0 which are answered
by a broadcast from CPU 0. The CPU computes the time difference between
sending the request and receiving the broadcast, and subtracts the
one-way latency, thus timing the broadcast latency.
The parameters are:
-count <count>
    the number of invocations (default is 100000)

Matrix multiplication.
This Satin application multiplies two matrices A and B, storing the result in
a matrix C, by recursively dividing A and B into four quadrants each, so that
the result matrix C can be computed as follows:

C11 = A11 * B11 + A12 * B21
C12 = A11 * B12 + A12 * B22
C21 = A21 * B11 + A22 * B21
C22 = A21 * B21 + A22 * B22

These formulas again contain matrix multiplications (on smaller matrices),
on which the same trick can be applied.
The recursion stops when a threshold is reached.

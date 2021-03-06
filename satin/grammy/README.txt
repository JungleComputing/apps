This directory contains a grammar-based text compressor. Given a text,
it constructs a set of grammar rules that produce one sentence: the
input text.

It looks ahead for a number of
compression steps to select the best sequence of backreferences.

usage:

Compress <input-file> <output-file>

There is also a de-compressor:

Decompress <input-file> <output-file>

The program takes the following command-line options:

  -lookahead <n> The number of levels of look-ahead used to determine the
                 best way to compress the text.

  -top <n>       For each compression step, consider the best <n> choices.

  -verify        After compressing the text, decompress it again, and compare
                 against the original to make sure the compressor was correct.


Defaults are -lookahead 2 -top 2.

Note that larger parameters for lookahead and top can quickly lead to
very large execution times. Grammar-based compression is time-consuming,
lookahead makes it even more so.

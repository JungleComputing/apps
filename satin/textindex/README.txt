This directory contains a small program to produce an index of a set
of files. That is, as output it produces a list of files it has examined,
and a list of all the words that occur in these files, plus for each word
a list of files that the word occurs in.

During indexing, and in the output file, ranges of file numbers are
contracted by a negative number that indicates the end of the range.
Thus the range of occurences

40 41 42 43 44

is contracted to

40 -44


The program is invoked via TextIndex. It expects the name of a file
to write the index to, and a list of files and directories to index.

For example:

java <ibis flags> TextIndex ixfile ~/ibis/src

Filenames starting with a '.' (including '.' itself and '..') are
ignored, as are files/directories with the name 'CVS'. Filenames
ending with '.gz' are assumed to be compressed with gzip, and are
compressed on the fly.


// File: $Id$

// A replacement step, represented as a list of text positions and a length.
class Step {
    int occurences[];
    int len;

    public Step( int occ[], int l )
    {
        occurences = occ;
        len = l;
    }

    public String toString()
    {
        String res = "([";

        for( int i=0; i<occurences.length; i++ ){
            if( i != 0 ){
                res += ",";
            }
            res += occurences[i];
        }
        return res + "],len=" + len + ")";
    }
}

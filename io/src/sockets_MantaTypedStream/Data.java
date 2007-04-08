package sockets_MantaTypedStream;

/* $Id$ */


import java.io.Serializable;

public final class Data implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6600053565774847459L;

    double v1, v2, v3, v4;

    Data next;

    public Data(double value, Data next) {
        v1 = v2 = v3 = v4 = value;
        this.next = next;
    }
}
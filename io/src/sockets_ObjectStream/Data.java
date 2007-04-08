package sockets_ObjectStream;

/* $Id$ */


import java.io.Serializable;

public class Data implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4804566312427740059L;

    double value;

    Data next;

    public Data() {
    }

    public Data(double value, Data next) {
        this.value = value;
        this.next = next;
    }
}
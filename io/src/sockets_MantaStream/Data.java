package sockets_MantaStream;

/* $Id$ */


import java.io.Serializable;

public class Data implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6521743232374414218L;

    double value;

    Data next;

    public Data() {
    }

    public Data(double value, Data next) {
        this.value = value;
        this.next = next;
    }
}
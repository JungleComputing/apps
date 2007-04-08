package more.list.normal;

/* $Id: Data.java 2844 2004-11-24 10:52:27Z ceriel $ */

import java.io.Serializable;

public class Data implements Serializable { 

    /**
     * 
     */
    private static final long serialVersionUID = 8349376578930524618L;

    public static final int OBJECT_SIZE = 4*4+4;

    int v1, v2, v3, v4;
    Data next;

    public Data(int value, Data next) { 
	v1 = v2 = v3 = v4 = value;
	this.next  = next;
    } 
} 

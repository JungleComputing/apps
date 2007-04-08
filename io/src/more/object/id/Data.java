package more.object.id;

/* $Id: Data.java 2844 2004-11-24 10:52:27Z ceriel $ */

import java.io.Serializable;
import java.net.InetAddress;

public final class Data implements Serializable { 
    /**
     * 
     */
    private static final long serialVersionUID = -7658680409483852655L;
    InetAddress address;
    String name;

    Data(String name, InetAddress me) { 
	this.name = name;
	this.address = me;
    }
} 

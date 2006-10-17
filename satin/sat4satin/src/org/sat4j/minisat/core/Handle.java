/*
 * Created on 6 mai 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.sat4j.minisat.core;

import java.io.Serializable;

/**
 * This class simply holds a reference to a object. The goal is to simulate the
 * passing of a parameter by reference when this is useful. It simply holds a
 * reference to an object (with public access).
 * 
 * @author roussel
 */
public class Handle<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public T obj;
}

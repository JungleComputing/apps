/*
 * Created on 9 juil. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.core;

/**
 * This interface is to be implemented by the classes wanted to be notified of
 * the falsification of a literal.
 * 
 * @author leberre
 */
public interface Propagatable {

    /**
     * Propagate the truth value of a literal in constraints in which that
     * literal is falsified.
     * 
     * @param s
     *            something able to perform unit propagation
     * @param p
     *            the literal being propagated. Its negation must appear in the
     *            constraint.
     * @return false iff an inconsistency (a contradiction) is detected.
     */
    boolean propagate(UnitPropagationListener s, int p);

}
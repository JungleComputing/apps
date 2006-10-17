/*
 * Created on 15 juin 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.core;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface ILits2 extends ILits {

    /**
     * To know the number of binary clauses in which the literal occurs. Please
     * note that this method should only be used in conjunction with the
     * BinaryClauses data structure.
     * 
     * @param p
     * @return the number of binary clauses.
     */
    int nBinaryClauses(int p);

    /**
     * Method to create a binary clause.
     * 
     * @param lit1 the first literal of the clause
     * @param lit2 the second literal of the clause
     */
    void binaryClauses(int lit1, int lit2);

}
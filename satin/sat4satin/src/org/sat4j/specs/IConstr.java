package org.sat4j.specs;

/**
 * The most general abstraction for handling a constraint.
 * 
 * @author leberre
 *
 */
public interface IConstr {

    /**
     * @return true iff the clause was learnt during the search
     */
    boolean learnt();

    /**
     * @return the number of literals in the constraint.
     */
    int size();

    /**
     * returns the ith literal in the constraint
     * 
     * @param i the index of the literal
     * @return a literal
     */
    int get(int i);

}
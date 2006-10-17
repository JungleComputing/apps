package org.sat4j.minisat.core;

public interface SearchListener {

    /**
     * decision variable
     * 
     * @param p
     */
    void assuming(int p);

    /**
     * Unit propagation
     * 
     * @param p
     */
    void propagating(int p);

    /**
     * backtrack on a decision variable
     * 
     * @param p
     */
    void backtracking(int p);

    /**
     * adding forced variable (conflict driven assignment)
     */
    void adding(int p);

    /**
     * learning a new clause
     * 
     * @param c
     */
    void learn(Constr c);

    /**
     * delete a clause
     */
    void delete(int[] clause);

    /**
     * a conflict has been found.
     * 
     */
    void conflictFound();

    /**
     * a solution is found.
     * 
     */
    void solutionFound();

    /**
     * starts a propagation
     */
    void beginLoop();
}

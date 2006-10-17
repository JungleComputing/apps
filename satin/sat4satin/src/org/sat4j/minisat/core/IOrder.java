package org.sat4j.minisat.core;

import java.io.PrintStream;

public interface IOrder {

    void setLits(ILits lits);

    /**
     * Appel�e quand une nouvelle variable est cr��e.
     */
    void newVar();

    /**
     * Appel�e lorsque plusieurs variables sont cr��es
     * 
     * @param howmany
     *            le nombre de variables cr��es
     */
    void newVar(int howmany);

    /**
     * S�lectionne une nouvelle variable, non affect�e, ayant l'activit�
     * la plus �lev�e.
     * 
     * @return Lit.UNDEFINED si aucune variable n'est trouv�e
     */
    int select();

    /**
     * M�thode appel�e quand la variable x est d�saffect�e.
     * 
     * @param x
     */
    void undo(int x);

    /**
     * To be called when the activity of a literal changed.
     * 
     * @param p a literal. The associated variable will be updated.
     */
    void updateVar(int p);

    /**
     * that method has the responsability to initialize all arrays in the
     * heuristics. PLEASE CALL super.init() IF YOU OVERRIDE THAT METHOD.
     */
    void init();

    Object clone();

    void printStat(PrintStream out, String prefix);

    void setVarDecay(double d);
    
    void varDecayActivity();
    
    /**
     * To obtain the current activity of a variable. 
     * @param p a literal
     * @return the activity of the variable associated to that literal.
     */
    double varActivity(int p);

    /* Added for satin: */
    void setActivity(int var, double val);
}

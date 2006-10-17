package org.sat4j.minisat.core;

import java.io.PrintStream;

public interface IOrder {

    void setLits(ILits lits);

    /**
     * Appelee quand une nouvelle variable est creee.
     */
    void newVar();

    /**
     * Appelee lorsque plusieurs variables sont creees
     * 
     * @param howmany
     *            le nombre de variables creees
     */
    void newVar(int howmany);

    /**
     * Selectionne une nouvelle variable, non affectee, ayant l'activite
     * la plus elevee.
     * 
     * @return Lit.UNDEFINED si aucune variable n'est trouvee
     */
    int select();

    /**
     * Methode appelee quand la variable x est desaffectee.
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

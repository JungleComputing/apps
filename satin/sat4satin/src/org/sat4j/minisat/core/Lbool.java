/*
 * SAT4J: a SATisfiability library for Java   
 * Copyright (C) 2004 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas Een and Niklas Serensson.
 * Proceedings of the Sixth International Conference on Theory 
 * and Applications of Satisfiability Testing, LNCS 2919, 
 * pp 502-518, 2003.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *  
 */

package org.sat4j.minisat.core;

/*
 * Created on 9 oct. 2003 Probleme : comment rendre opposite final ?
 */

/**
 * @author leberre Cette classe represente les valeurs booleennes qui
 *         peuvent etre associees aux litteraux.
 */
public enum Lbool {

    FALSE("F"), TRUE("T"), UNDEFINED("U");

    static {
        // on cree ici les regles de la negation
        FALSE.opposite = TRUE;
        TRUE.opposite = FALSE;
        UNDEFINED.opposite = UNDEFINED;
    }

    private Lbool(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Negation booleenne
     * 
     * @return la negation de la valeur boolenne. La negation de la valeur
     *         UNDEFINED est UNDEFINED.
     */
    public Lbool not() {
        return opposite;
    }

    /**
     * Une valeur booleenne est representee par T,F ou U.
     * 
     * @return l'une des trois lettres
     */
    @Override
    public String toString() {
        return symbol;
    }

    /**
     * Le symbole representant la valeur booleenne
     */
    private final String symbol;

    /**
     * la valeur booleenne opposee
     */
    private Lbool opposite;

}

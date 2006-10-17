/*
 * SAT4J: a SATisfiability library for Java   
 * Copyright (C) 2004 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas E�n and Niklas S�rensson.
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
 * Created on 9 oct. 2003 Probl�me : comment rendre opposite final ?
 */

/**
 * @author leberre Cette classe repr�sente les valeurs bool�ennes qui
 *         peuvent �tre associ�es aux litt�raux.
 */
public enum Lbool {

    FALSE("F"), TRUE("T"), UNDEFINED("U");

    static {
        // on cr�e ici les r�gles de la n�gation
        FALSE.opposite = TRUE;
        TRUE.opposite = FALSE;
        UNDEFINED.opposite = UNDEFINED;
    }

    private Lbool(String symbol) {
        this.symbol = symbol;
    }

    /**
     * N�gation bool�enne
     * 
     * @return la n�gation de la valeur bool�nne. La n�gation de la valeur
     *         UNDEFINED est UNDEFINED.
     */
    public Lbool not() {
        return opposite;
    }

    /**
     * Une valeur bool�enne est repr�sent�e par T,F ou U.
     * 
     * @return l'une des trois lettres
     */
    @Override
    public String toString() {
        return symbol;
    }

    /**
     * Le symbole repr�sentant la valeur bool�enne
     */
    private final String symbol;

    /**
     * la valeur bool�enne oppos�e
     */
    private Lbool opposite;

}

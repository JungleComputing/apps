/*
 * MiniSAT in Java, a Java based-SAT framework
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

import java.io.Serializable;

/*
 * Created on 9 oct. 2003
 */

/**
 * @author leberre Implantation d'une queue e l'aide d'un tableau
 */
public final class IntQueue implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int INITIAL_QUEUE_CAPACITY = 10;

    /**
     * Add an element to the queue.
     * The queue is supposed to be large enough for that!
     * @param x the element to add
     */
    public void insert(final int x) {
       // ensure(size + 1);
        assert size < myarray.length;
        myarray[size++] = x;
    }

    /**
     * returns the nexdt element in the queue.
     * Unexpected results if the queue is empty!
     * 
     * @return the firsst element on the queue
     */
    public int dequeue() {
        assert first < size;
        return myarray[first++];
    }

    /**
     * Vide la queue
     */
    public void clear() {
        size = 0;
        first = 0;
    }

    /**
     * Pour connaetre la taille de la queue.
     * 
     * @return le nombre d'elements restant dans la queue
     */
    public int size() {
        return size - first;
    }

    /**
     * Utilisee pour accroetre dynamiquement la taille de la queue.
     * 
     * @param nsize
     *            la taille maximale de la queue
     */
    public void ensure(final int nsize) {
        if (nsize >= myarray.length) {
            int[] narray = new int[Math.max(nsize, size * 2)];
            System.arraycopy(myarray, 0, narray, 0, size);
            myarray = narray;
        }
    }

    @Override
    public String toString() {
        StringBuffer stb = new StringBuffer();
        stb.append(">");
        for (int i = first; i < size - 1; i++) {
            stb.append(myarray[i]);
            stb.append(" ");
        }
        if (first != size) {
            stb.append(myarray[size - 1]);
        }
        stb.append("<");
        return stb.toString();
    }

    private int[] myarray = new int[INITIAL_QUEUE_CAPACITY];

    private int size = 0;

    private int first = 0;

}

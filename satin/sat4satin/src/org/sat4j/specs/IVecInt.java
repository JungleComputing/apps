/*
 * SAT4J: a SATisfiability library for Java Copyright (C) 2004-2006 Daniel Le Berre
 * 
 * Based on the original minisat specification from:
 * 
 * An extensible SAT solver. Niklas E?n and Niklas S?rensson. Proceedings of the
 * Sixth International Conference on Theory and Applications of Satisfiability
 * Testing, LNCS 2919, pp 502-518, 2003.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */

package org.sat4j.specs;

/**
 * An abstraction for the vector of int used on the library.
 * 
 * @author leberre
 */
public interface IVecInt extends Iterable<Integer> {

    public abstract int size();

    /**
     * Remove the latest nofelems elements from the vector
     * 
     * @param nofelems
     */
    public abstract void shrink(int nofelems);

    public abstract void shrinkTo(int newsize);

    /**
     * d�pile le dernier �l�ment du vecteur. Si le vecteur est vide, ne
     * fait rien.
     */
    public abstract IVecInt pop();

    public abstract void growTo(int newsize, final int pad);

    public abstract void ensure(int nsize);

    public abstract IVecInt push(int elem);

    /**
     * Push the element in the Vector without verifying if there is room for it.
     * USE WITH CAUTION!
     * 
     * @param elem
     */
    void unsafePush(int elem);

    int unsafeGet(int eleem);

    public abstract void clear();

    public abstract int last();

    public abstract int get(int i);

    public abstract void set(int i, int o);

    public abstract boolean contains(int e);

    /**
     * C'est op�rations devraient se faire en temps constant. Ce n'est pas le
     * cas ici.
     * 
     * @param copy
     */
    public abstract void copyTo(IVecInt copy);

    /**
     * @param is
     */
    public abstract void copyTo(int[] is);

    /*
     * Copie un vecteur dans un autre (en vidant le premier), en temps constant.
     */
    public abstract void moveTo(IVecInt dest);

    public abstract void moveTo2(IVecInt dest);

    public abstract void moveTo(int[] dest);

    /**
     * Move elements inside the vector. The content of the method is equivalent
     * to: <code>vec[dest] = vec[source]</code>
     * 
     * @param dest
     *            the index of the destination
     * @param source
     *            the index of the source
     */
    void moveTo(int dest, int source);

    /**
     * Insert an element at the very begining of the vector. The former first
     * element is appended to the end of the vector in order to have a constant
     * time operation.
     * 
     * @param elem
     *            the element to put first in the vector.
     */
    public abstract void insertFirst(final int elem);

    /**
     * Enleve un element qui se trouve dans le vecteur!!!
     * 
     * @param elem
     *            un element du vecteur
     */
    public abstract void remove(int elem);

    /**
     * Delete the ith element of the vector. The latest element of the vector
     * replaces the removed element at the ith indexer.
     * 
     * @param i
     *            the indexer of the element in the vector
     * @return the former ith element of the vector that is now removed from the
     *         vector
     */
    public abstract int delete(int i);

    public abstract void sort();

    public abstract void sortUnique();

    /**
     * To know if a vector is empty
     * 
     * @return true iff the vector is empty.
     * @since 1.6
     */
    boolean isEmpty();
}
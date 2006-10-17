/*
 * Created on 20 d�c. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.sat4j.specs;

import java.util.Comparator;

/**
 * An abstraction on the type of vector used in the library.
 * 
 * @author leberre 
 */
public interface IVec<T> extends Iterable<T> {
    /**
     * @return the number of elements contained in the vector
     */
    int size();

    /**
     * Remove nofelems from the Vector. It is assumed that the number of
     * elements to remove is smaller or equals to the current number of elements
     * in the vector
     * 
     * @param nofelems
     *            the number of elements to remove.
     */
    void shrink(int nofelems);

    /**
     * reduce the Vector to exactly newsize elements
     * 
     * @param newsize
     *            the new size of the vector.
     */
    void shrinkTo(final int newsize);

    /**
     * Pop the last element on the stack. It is assumed that the stack is not
     * empty!
     */
    void pop();

    void growTo(final int newsize, final T pad);

    void ensure(final int nsize);

    IVec<T> push(final T elem);

    /**
     * To push an element in the vector when you know you have space for it.
     * 
     * @param elem
     */
    void unsafePush(T elem);

    /**
     * Insert an element at the very begining of the vector. The former first
     * element is appended to the end of the vector in order to have a constant
     * time operation.
     * 
     * @param elem
     *            the element to put first in the vector.
     */
    void insertFirst(final T elem);

    void insertFirstWithShifting(final T elem);

    void clear();

    /**
     * return the latest element on the stack. It is assumed that the stack is
     * not empty!
     * 
     * @return the last (top) element on the stack
     */
    T last();

    T get(int i);

    void set(int i, T o);

    /**
     * Enleve un element qui se trouve dans le vecteur!!!
     * 
     * @param elem
     *            un element du vecteur
     */
    void remove(T elem);

    /**
     * Delete the ith element of the vector. The latest element of the vector
     * replaces the removed element at the ith indexer.
     * 
     * @param i
     *            the indexer of the element in the vector
     * @return the former ith element of the vector that is now removed from the
     *         vector
     */
    T delete(int i);

    /**
     * Ces op�rations devraient se faire en temps constant. Ce n'est pas le
     * cas ici.
     * 
     * @param copy
     */
    void copyTo(IVec<T> copy);

    <E> void copyTo(E[] dest);

    /**
     * Move the content of the vector into dest. Note that the vector become
     * empty. The content of the vector is appended to dest.
     * 
     * @param dest
     *            the vector where top put the content of this vector
     */
    void moveTo(IVec<T> dest);

    /**
     * Move elements inside the vector.
     * The content of the method is equivalent to: 
     * <code>vec[dest] = vec[source]</code>
     * 
     * @param dest the index of the destination
     * @param source the index of the source
     */
    void moveTo(int dest, int source);
    
    /*
     * @param comparator
     */
    void sort(Comparator<T> comparator);

    void sortUnique(Comparator<T> comparator);

    Object clone();
}

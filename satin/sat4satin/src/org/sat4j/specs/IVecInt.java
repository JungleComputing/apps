/*
 * Created on 20 dec. 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
     * depile le dernier element du vecteur. Si le vecteur est vide, ne
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
     * C'est operations devraient se faire en temps constant. Ce n'est pas le
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
     * Move elements inside the vector.
     * The content of the method is equivalent to: 
     * <code>vec[dest] = vec[source]</code>
     * 
     * @param dest the index of the destination
     * @param source the index of the source
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

    public abstract Object clone();
}

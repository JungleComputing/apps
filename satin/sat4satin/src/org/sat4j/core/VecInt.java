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

package org.sat4j.core;

import java.io.Serializable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

import org.sat4j.specs.IVecInt;

/*
 * Created on 9 oct. 2003
 */

/**
 * A vector specific for primitive integers, widely used in the solver.
 * 
 * @author leberre
 */
public class VecInt implements Serializable, IVecInt {

    private static final long serialVersionUID = 1L;

    private static final int RANDOM_SEED = 91648253;

    public static final IVecInt EMPTY = new VecInt() {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void shrink(int nofelems) {
        }

        @Override
        public void shrinkTo(int newsize) {
        }

        @Override
        public IVecInt pop() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void growTo(int newsize, int pad) {
        }

        @Override
        public void ensure(int nsize) {
        }

        @Override
        public IVecInt push(int elem) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void unsafePush(int elem) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
        }

        @Override
        public int last() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int get(int i) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(int i, int o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(int e) {
            return false;
        }

        @Override
        public void copyTo(IVecInt copy) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void copyTo(int[] is) {
        }

        @Override
        public void moveTo(IVecInt dest) {
        }

        @Override
        public void moveTo2(IVecInt dest) {
        }

        @Override
        public void moveTo(int[] dest) {
        }

        @Override
        public void insertFirst(int elem) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(int elem) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int delete(int i) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sort() {
        }

        @Override
        public void sortUnique() {
        }
    };

    public VecInt() {
        this(5);
    }

    public VecInt(int size) {
        myarray = new int[size];
    }

    /**
     * Construit un vecteur contenant de taille size rempli e l'aide de size
     * pad.
     * 
     * @param size
     *            la taille du vecteur
     * @param pad
     *            l'objet servant e remplir le vecteur
     */
    public VecInt(int size, int pad) {
        myarray = new int[size];
        for (int i = 0; i < size; i++) {
            myarray[i] = pad;
        }
        nbelem = size;
    }

    public int size() {
        return nbelem;
    }

    /**
     * Remove the latest nofelems elements from the vector
     * 
     * @param nofelems
     */
    public void shrink(int nofelems) {
        assert nofelems >= 0;
        assert nofelems <= size();
        nbelem -= nofelems;
    }

    public void shrinkTo(int newsize) {
        assert newsize >= 0;
        assert newsize < nbelem;
        nbelem = newsize;
    }

    /**
     * depile le dernier element du vecteur. Si le vecteur est vide, ne
     * fait rien.
     */
    public IVecInt pop() {
        assert size() != 0;
        --nbelem;
        return this;
    }

    public void growTo(int newsize, final int pad) {
        assert newsize > size();
        ensure(newsize);
        while (--newsize >= 0) {
            myarray[nbelem++] = pad;
        }
    }

    public void ensure(int nsize) {
        // WAS: if (nsize >= myarray.length) {
        if (nsize > myarray.length) {
            int[] narray = new int[Math.max(nsize, nbelem * 2)];
            System.arraycopy(myarray, 0, narray, 0, nbelem);
            myarray = narray;
        }
    }

    public IVecInt push(int elem) {
        ensure(nbelem + 1);
        myarray[nbelem++] = elem;
        return this;
    }

    public void unsafePush(int elem) {
        myarray[nbelem++] = elem;
    }

    public void clear() {
        nbelem = 0;
    }

    public int last() {
        assert nbelem > 0;
        return myarray[nbelem - 1];
    }

    public int get(int i) {
        assert i >= 0;
        assert i < nbelem;
        return myarray[i];
    }

    public int unsafeGet(int i) {
        return myarray[i];
    }

    public void set(int i, int o) {
        assert i >= 0;
        assert i < nbelem;
        myarray[i] = o;
    }

    public boolean contains(int e) {
        for (int i = 0; i < nbelem; i++) {
            if (myarray[i] == e) {
                return true;
            }
        }
        return false;
    }

    /**
     * C'est operations devraient se faire en temps constant. Ce n'est pas le
     * cas ici.
     * 
     * @param copy
     */
    public void copyTo(IVecInt copy) {
        /*
         * int nsize = nbelem + copy.nbelem; copy.ensure(nsize); for (int i = 0;
         * i < nbelem; i++) { copy.myarray[i + copy.nbelem] = myarray[i]; }
         * copy.nbelem = nsize;
         */
        VecInt ncopy = (VecInt) copy;
        int nsize = nbelem + ncopy.nbelem;
        ncopy.ensure(nsize);
        System.arraycopy(myarray, 0, ncopy.myarray, ncopy.nbelem, nbelem);
        ncopy.nbelem = nsize;
    }

    @Override
    public Object clone() {
	VecInt clone = new VecInt(this.myarray.length);
	copyTo(clone);
	return clone;
    }

    /**
     * @param is
     */
    public void copyTo(int[] is) {
        assert is.length >= nbelem;
        System.arraycopy(myarray, 0, is, 0, nbelem);
    }

    /*
     * Copie un vecteur dans un autre (en vidant le premier), en temps constant.
     */
    public void moveTo(IVecInt dest) {
        copyTo(dest);
        nbelem = 0;
    }

    public void moveTo2(IVecInt dest) {
        VecInt ndest = (VecInt) dest;
        int s = ndest.nbelem;
        int tmp[] = ndest.myarray;
        ndest.myarray = myarray;
        ndest.nbelem = nbelem;
        myarray = tmp;
        nbelem = s;
        nbelem = 0;
    }

    public void moveTo(int dest, int source) {
        myarray[dest]=myarray[source];
    }
    
    public void moveTo(int[] dest) {
        System.arraycopy(myarray, 0, dest, 0, nbelem);
        nbelem = 0;
    }

    /**
     * Insert an element at the very begining of the vector. The former first
     * element is appended to the end of the vector in order to have a constant
     * time operation.
     * 
     * @param elem
     *            the element to put first in the vector.
     */
    public void insertFirst(final int elem) {
        if (nbelem > 0) {
            push(myarray[0]);
            myarray[0] = elem;
            return;
        }
        push(elem);
    }

    /**
     * Enleve un element qui se trouve dans le vecteur!!!
     * 
     * @param elem
     *            un element du vecteur
     */
    public void remove(int elem) {
        assert size() > 0;
        int j = 0;
        for (; myarray[j] != elem; j++) {
            assert j < size();
        }
        for (; j < size() - 1; j++) {
            myarray[j] = myarray[j + 1];
        }
        pop();
    }

    /**
     * Delete the ith element of the vector. The latest element of the vector
     * replaces the removed element at the ith indexer.
     * 
     * @param i
     *            the indexer of the element in the vector
     * @return the former ith element of the vector that is now removed from the
     *         vector
     */
    public int delete(int i) {
        assert i >= 0;
        assert i < nbelem;
        int ith = myarray[i];
        myarray[i] = myarray[--nbelem];
        return ith;
    }

    private int nbelem;

    private int[] myarray;

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.int#toString()
     */
    @Override
    public String toString() {
        StringBuffer stb = new StringBuffer();
        for (int i = 0; i < nbelem - 1; i++) {
            stb.append(myarray[i]);
            stb.append(",");
        }
        if (nbelem > 0) {
            stb.append(myarray[nbelem - 1]);
        }
        return stb.toString();
    }

    private static Random rand = new Random(RANDOM_SEED);

    void selectionSort(int from, int to) {
        int i, j, best_i;
        int tmp;

        for (i = from; i < to - 1; i++) {
            best_i = i;
            for (j = i + 1; j < to; j++) {
                if (myarray[j] < myarray[best_i]) {
                    best_i = j;
                }
            }
            tmp = myarray[i];
            myarray[i] = myarray[best_i];
            myarray[best_i] = tmp;
        }
    }

    void sort(int from, int to) {
        int width = to - from;
        if (to - from <= 15) {
            selectionSort(from, to);
        } else {
            int pivot = myarray[rand.nextInt(width) + from];
            int tmp;
            int i = from - 1;
            int j = to;

            for (;;) {
                do {
                    i++;
                } while (myarray[i] < pivot);
                do {
                    j--;
                } while (pivot < myarray[j]);

                if (i >= j) {
                    break;
                }

                tmp = myarray[i];
                myarray[i] = myarray[j];
                myarray[j] = tmp;
            }

            sort(from, i);
            sort(i, to);
        }
    }

    /**
     * sort the vector using a custom quicksort.
     */
    public void sort() {
        sort(0, nbelem);
    }

    public void sortUnique() {
        int i, j;
        int last;
        if (nbelem == 0) {
            return;
        }

        sort(0, nbelem);
        i = 1;
        last = myarray[0];
        for (j = 1; j < nbelem; j++) {
            if (last < myarray[j]) {
                last = myarray[i] = myarray[j];
                i++;
            }
        }

        nbelem = i;
    }

     /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IVecInt) {
            IVecInt v = (IVecInt) obj;
            if (v.size() != size()) {
                return false;
            }
            for (int i = 0; i < size(); i++) {
                if (v.get(i) != get(i)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int sum = 0;
        for (int i = 0; i < nbelem; i++) {
            sum += myarray.hashCode() / nbelem;
        }
        return sum;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.specs.IVecInt2#pushAll(org.sat4j.specs.IVecInt2)
     */
    public void pushAll(IVecInt vec) {
        VecInt nvec = (VecInt) vec;
        int nsize = nbelem + nvec.nbelem;
        ensure(nsize);
        System.arraycopy(nvec.myarray, 0, myarray, nbelem, nvec.nbelem);
        nbelem = nsize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sat4j.specs.IVecInt2#isSubsetOf(org.sat4j.specs.IVecInt2)
     */
    public boolean isSubsetOf(IVecInt vec) {
        boolean isSubSet = (this.size() <= vec.size());

        int i = 0;
        int j = 0;
        while (isSubSet && (i < this.size()) && (j < vec.size())) {
            while ((j < vec.size()) && (vec.get(j) < this.get(i))) {
                j++;
            }
            if (j < vec.size()) {
                isSubSet = (this.get(i) == vec.get(j));
            } else {
                isSubSet = false;
            }
            i++;
        }
        return isSubSet;
    }

    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private int i = 0;

            public boolean hasNext() {
                return i < nbelem;
            }

            public Integer next() {
                if (i == nbelem) {
                    throw new NoSuchElementException();
                }
                return myarray[i++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}

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
package org.sat4j.minisat.core;

import java.io.Serializable;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;

/**
 * Heap implementation used to maintain the variables order in some heuristics.
 * 
 * @author daniel
 * 
 */
public class Heap implements Serializable {

    /*
     * default serial version id
     */
    private static final long serialVersionUID = 1L;

    private final static int left(int i) {
        return i << 1;
    }

    private final static int right(int i) {
        return (i << 1) ^ 1;
    }

    private final static int parent(int i) {
        return i >> 1;
    }

    private final boolean comp(int a, int b) {
        return activity[a] > activity[b];
    }

    private final IVecInt heap = new VecInt(); // heap of ints

    private final IVecInt indices = new VecInt(); // int -> index in heap

    private final double[] activity;

    @SuppressWarnings("PMD")
    final void percolateUp(int i) {
        int x = heap.get(i);
        while (parent(i) != 0 && comp(x, heap.get(parent(i)))) {
            heap.set(i, heap.get(parent(i)));
            indices.set(heap.get(i), i);
            i = parent(i);
        }
        heap.set(i, x);
        indices.set(x, i);
    }

    final void percolateDown(int i) {
        int x = heap.get(i);
        while (left(i) < heap.size()) {
            int child = right(i) < heap.size()
                    && comp(heap.get(right(i)), heap.get(left(i))) ? right(i)
                    : left(i);
            if (!comp(heap.get(child), x))
                break;
            heap.set(i, heap.get(child));
            indices.set(heap.get(i), i);
            i = child;
        }
        heap.set(i, x);
        indices.set(x, i);
    }

    boolean ok(int n) {
        return n >= 0 && n < indices.size();
    }

    public Heap(double[] activity) {
        this.activity = activity;
        heap.push(-1);
    }

    public void setBounds(int size) {
        assert (size >= 0);
        indices.growTo(size, 0);
    }

    public boolean inHeap(int n) {
        assert (ok(n));
        return indices.get(n) != 0;
    }

    public void increase(int n) {
        assert (ok(n));
        assert (inHeap(n));
        percolateUp(indices.get(n));
    }

    public boolean empty() {
        return heap.size() == 1;
    }

    public void insert(int n) {
        assert (ok(n));
        indices.set(n, heap.size());
        heap.push(n);
        percolateUp(indices.get(n));
    }

    public int getmin() {
        int r = heap.get(1);
        heap.set(1, heap.last());
        indices.set(heap.get(1), 1);
        indices.set(r, 0);
        heap.pop();
        if (heap.size() > 1)
            percolateDown(1);
        return r;
    }

    public boolean heapProperty() {
        return heapProperty(1);
    }

    public boolean heapProperty(int i) {
        return i >= heap.size()
                || ((parent(i) == 0 || !comp(heap.get(i), heap.get(parent(i))))
                        && heapProperty(left(i)) && heapProperty(right(i)));
    }

}

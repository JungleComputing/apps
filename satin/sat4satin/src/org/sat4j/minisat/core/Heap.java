package org.sat4j.minisat.core;

import java.io.Serializable;

import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;

public class Heap implements Serializable, Cloneable {

    /*
     * default serial version id 
     */
    private static final long serialVersionUID = 1L;

    private final static int left(int i) {
        return i + i;
    }

    private final static int right(int i) {
        return i + i + 1;
    }

    private final static int parent(int i) {
        return i >> 1;
    }

    private final boolean comp(int a, int b) {
        return activity[a] > activity[b];
    }

    private IVecInt heap = new VecInt(); // heap of ints

    private IVecInt indices = new VecInt(); // int -> index in heap

    // private final double [] activity;
    private double [] activity;
    
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
            if (!comp(heap.get(child), x)) {
                break;
            }
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

    public Heap(double [] activity) {
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
        if (heap.size() > 1) {
            percolateDown(1);
        }
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

    public void setActivity(double [] activity) {
        this.activity = activity;
    }

    @Override
    public Object clone() {
	Heap clone;

	try {
	    clone = (Heap) super.clone();
	}
	catch (CloneNotSupportedException e) {
	    throw new InternalError(e.toString());
	}

	clone.heap = (VecInt) this.heap.clone();
	clone.indices = (VecInt) this.indices.clone();
	// NOTE: caller should update activity using setActivity!
	clone.activity = null;

	return clone;
    }
}

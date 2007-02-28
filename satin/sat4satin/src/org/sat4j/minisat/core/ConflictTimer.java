package org.sat4j.minisat.core;

import java.io.Serializable;

abstract class ConflictTimer implements Serializable {
    
    private int counter;
    private final int bound;
    
    ConflictTimer(final int bound)  {
        this.bound = bound;
        counter = 0;
    }
    
    void reset() {
        counter = 0;
    }
    
    void newConflict() {
        counter++;
        if (counter==bound) {
            run();
            counter = 0;
        }
    }
    
    abstract void run();
}

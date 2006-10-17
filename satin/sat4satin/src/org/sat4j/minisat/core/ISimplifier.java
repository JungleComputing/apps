package org.sat4j.minisat.core;

import org.sat4j.specs.IVecInt;

interface ISimplifier extends java.io.Serializable {
    void simplify(IVecInt outLearnt);
}

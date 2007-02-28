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
package org.sat4j.tools;

import java.io.Serializable;

import org.sat4j.core.VecInt;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;

/**
 * Very simple Dimacs array reader. Allow solvers to read the constraints from
 * arrays that effectively contain Dimacs formatted lines (without the
 * terminating 0).
 * 
 * Adaptation of org.sat4j.reader.DimacsReader.
 * 
 * @author dlb
 * @author or
 * @author fuhs
 */
public class DimacsArrayReader implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final ISolver solver;

    public DimacsArrayReader(ISolver solver) {
        this.solver = solver;
    }

    protected boolean handleConstr(int gateType, int output, int[] inputs)
            throws ContradictionException {
        IVecInt literals = new VecInt(inputs);
        solver.addClause(literals);
        return true;
    }

    /**
     * @param gateType
     *            gateType[i] is the type of gate i according to the Extended
     *            Dimacs specs; ignored in DimacsArrayReader, but important for
     *            inheriting classes
     * @param outputs
     *            outputs[i] is the number of the output; ignored in
     *            DimacsArrayReader
     * @param inputs
     *            inputs[i] contains the clauses in DimacsArrayReader; an
     *            overriding class might have it contain the inputs of the
     *            current gate
     * @param maxVar
     *            the maximum number of assigned ids
     * @throws ContradictionException
     *             si le probleme est trivialement inconsitant
     */
    public IProblem parseInstance(int[] gateType, int[] outputs,
            int[][] inputs, int maxVar) throws ContradictionException {
        solver.reset();
        solver.newVar(maxVar);
        for (int i = 0; i < outputs.length; ++i) {
            handleConstr(gateType[i], outputs[i], inputs[i]);
        }
        return solver;
    }

    public String decode(int[] model) {
        StringBuilder stb = new StringBuilder(4 * model.length);
        for (int i = 0; i < model.length; i++) {
            stb.append(model[i]);
            stb.append(" ");
        }
        stb.append("0");
        return stb.toString();
    }

    protected ISolver getSolver() {
        return solver;
    }
}

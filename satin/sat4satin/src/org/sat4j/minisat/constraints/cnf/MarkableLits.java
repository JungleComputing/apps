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
package org.sat4j.minisat.constraints.cnf;

import java.util.HashSet;
import java.util.Set;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.core.IMarkableLits;
import org.sat4j.specs.IVecInt;

public class MarkableLits extends Lits implements IMarkableLits {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private int[] marks;

    private static final int DEFAULTMARK = 1;

    @Override
    public void init(int nvar) {
        super.init(nvar);
        marks = new int[(nvar << 1) + 2];
    }

    public void setMark(int p, int mark) {
        assert marks != null;
        assert p > 1;
        assert p < marks.length;
        marks[p] = mark;
    }

    public void setMark(int p) {
        setMark(p, DEFAULTMARK);
    }

    public int getMark(int p) {
        return marks[p];
    }

    public boolean isMarked(int p) {
        return marks[p] != MARKLESS;
    }

    public void resetMark(int p) {
        marks[p] = MARKLESS;
    }

    public void resetAllMarks() {
        for (int i = 2; i < marks.length; i++)
            resetMark(i);
    }

    public IVecInt getMarkedLiterals() {
        IVecInt marked = new VecInt();
        for (int i = 2; i < marks.length; i++) {
            if (isMarked(i))
                marked.push(i);
        }
        return marked;
    }

    public IVecInt getMarkedLiterals(int mark) {
        IVecInt marked = new VecInt();
        for (int i = 2; i < marks.length; i++) {
            if (getMark(i) == mark)
                marked.push(i);
        }
        return marked;
    }

    public IVecInt getMarkedVariables() {
        IVecInt marked = new VecInt();
        for (int i = 2; i < marks.length; i += 2) {
            if (isMarked(i) || isMarked(i + 1))
                marked.push(i >> 1);
        }
        return marked;
    }

    public IVecInt getMarkedVariables(int mark) {
        IVecInt marked = new VecInt();
        for (int i = 2; i < marks.length; i += 2) {
            if (getMark(i) == mark || getMark(i + 1) == mark)
                marked.push(i >> 1);
        }
        return marked;
    }

    public Set<Integer> getMarks() {
        Set<Integer> markers = new HashSet<Integer>();
        for (int m : marks)
            if (m != MARKLESS)
                markers.add(m);
        return markers;
    }
}

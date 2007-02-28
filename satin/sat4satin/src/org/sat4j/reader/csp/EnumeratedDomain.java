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
package org.sat4j.reader.csp;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class EnumeratedDomain implements Domain {

    private final int[] values;

    public EnumeratedDomain(int[] values) {
        this.values = values;
    }

    public int get(int i) {
        return values[i];
    }

    public int size() {
        return values.length;
    }

    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            private int i = 0;

            public boolean hasNext() {
                return i < values.length;
            }

            public Integer next() {
                if (i == values.length)
                    throw new NoSuchElementException();
                return values[i++];
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public int pos(int value) {
        for (int i = 0; i < values.length; i++)
            if (values[i] == value)
                return i;
        return -1;
    }
}

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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

/**
 * Class allowing to express the search as a tree in the dot language. The
 * resulting file can be viewed in a tool like <a
 * href="http://www.graphviz.org/">Graphviz</a>
 * 
 * To use only on small benchmarks.
 * 
 * @author daniel
 * 
 */
public class DotSearchListener implements SearchListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final transient Stack<String> pile;

    private transient String currentNodeName = null;

    private transient Writer out;

    private transient boolean estOrange = false;

    public DotSearchListener(final String fileNameToSave) {
        pile = new Stack<String>();
        try {
            out = new FileWriter(fileNameToSave);
        } catch (IOException e) {
            System.err.println("Problem when created file.");
        }
    }

    public final void assuming(final int p) {
        final Integer absP = Math.abs(p);
        String newName;

        if (currentNodeName == null) {
            newName = absP.toString();
            pile.push(newName);
            saveLine(lineTab("\"" + newName + "\"" + "[label=\"" + newName
                    + "\", shape=circle, color=blue, style=filled]"));
        } else {
            newName = currentNodeName;
            pile.push(newName);
            saveLine(lineTab("\"" + newName + "\"" + "[label=\""
                    + absP.toString()
                    + "\", shape=circle, color=blue, style=filled]"));
        }
        currentNodeName = newName;
    }

    public final void propagating(final int p) {
        String newName = currentNodeName + "." + p;

        if (currentNodeName == null) {
            saveLine(lineTab("\"null\" [label=\"\", shape=point]"));
        }
        final String couleur = estOrange ? "orange" : "green";

        saveLine(lineTab("\"" + newName + "\"" + "[label=\""
                + Integer.toString(p) + "\",shape=point, color=black]"));
        saveLine(lineTab("\"" + currentNodeName + "\"" + " -- " + "\""
                + newName + "\"" + "[label=" + "\" " + Integer.toString(p)
                + "\", fontcolor =" + couleur + ", color = " + couleur
                + ", style = bold]"));
        currentNodeName = newName;
        estOrange = false;
    }

    public final void backtracking(final int p) {
        final String temp = pile.pop();

        saveLine("\"" + temp + "\"" + "--" + "\"" + currentNodeName + "\""
                + "[label=\"\", color=red, style=dotted]");
        currentNodeName = temp;
    }

    public final void adding(final int p) {
        estOrange = true;
    }

    public final void learn(final Constr clause) {
    }

    public final void delete(final int[] clause) {
    }

    public final void conflictFound() {
        saveLine(lineTab("\"" + currentNodeName
                + "\" [label=\"\", shape=box, color=\"red\", style=filled]"));
    }

    public final void solutionFound() {
        saveLine(lineTab("\"" + currentNodeName
                + "\" [label=\"\", shape=box, color=\"green\", style=filled]"));
    }

    public final void beginLoop() {
    }

    public final void start() {
        saveLine("graph G {");
    }

    public final void end(Lbool result) {
        saveLine("}");
    }

    private final String lineTab(final String line) {
        return "\t" + line;
    }

    private final void saveLine(final String line) {
        try {
            out.write(line + '\n');
            if ("}".equals(line)) {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
 * Created on 9 juil. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.core;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface Undoable {

    /**
     * M?thode appel?e lors du backtrack
     * 
     * @param p
     *            un litt?ral d?saffect?
     */
    void undo(int p);

}

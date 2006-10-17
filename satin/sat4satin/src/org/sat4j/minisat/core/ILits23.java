/*
 * Created on 15 juin 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.minisat.core;

/**
 * @author leberre To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface ILits23 extends ILits2 {

    int nTernaryClauses(int p);

    void ternaryClauses(int lit1, int lit2, int lit3);
}
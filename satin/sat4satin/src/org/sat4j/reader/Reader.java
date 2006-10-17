/*
 * Created on 15 sept. 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sat4j.reader;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;

/**
 * A reader is responsible to feed an ISolver from a text file
 * and to convert the model found by the solver to a textual representation.
 * 
 * @author leberre
 */
public interface Reader {

    IProblem parseInstance(String filename) throws FileNotFoundException,
        ParseFormatException, IOException, ContradictionException;

    /**
     * Produce a model using the reader format.
     * 
     * @param model
     *            a model using the Dimacs format.
     * @return a human readable view of the model.
     */
    String decode(int[] model);
}

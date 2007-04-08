package more.tree.normal;

/* $Id: DITree.java 2844 2004-11-24 10:52:27Z ceriel $ */

import java.io.Serializable;

public class DITree implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 3875749550772145341L;

    public static final int OBJECT_SIZE = 4*4+2*4;

    DITree	left;
    DITree	right;

    int i;
    int i1;
    int i2;
    int i3;

    public DITree(int size) {
	int leftSize = size / 2;
	if (leftSize > 0) {
	    this.left = new DITree(leftSize);
	}
	if (size - leftSize - 1 > 0) {
	    this.right = new DITree(size - leftSize - 1);
	}
    }
}






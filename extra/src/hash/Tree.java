package hash;

/* $Id$ */

// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Tree.java

import java.io.Serializable;
import java.util.HashSet;

public final class Tree implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public Tree(int j) {
        int k = j / 2;
        if (k > 0)
            left = new Tree(k);
        if (j - k - 1 > 0)
            right = new Tree(j - k - 1);
    }

    public final void generated_WriteObject(HashSet<Tree> h)
            throws java.io.IOException {
        if (! h.contains(this)) {
            h.add(this);
            if (right != null)
                right.generated_WriteObject(h);
            if (left != null)
                left.generated_WriteObject(h);
        }
    }

    public static final int PAYLOAD = 16;

    Tree left;

    Tree right;

    int i;

    int i1;

    int i2;

    int i3;
}

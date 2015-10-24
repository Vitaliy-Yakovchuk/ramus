/*
 * Created on 18/8/2005
 */
package com.ramussoft.pb.data;

/**
 * @author ZDD
 */
public class RowProperty {
    public int tag;

    public String name;

    public boolean canHaveChilds = false;

    public boolean simple = true;

    public int newParent = -1;

    public RowProperty(final int tag, final String name, final boolean canHaveChilds,
                       final int newParent) {
        this.tag = tag;
        this.name = name;
        this.canHaveChilds = canHaveChilds;
        this.newParent = newParent;
    }

    public RowProperty(final int tag, final String name, final boolean canHaveChilds,
                       final boolean simple) {
        this.tag = tag;
        this.name = name;
        this.canHaveChilds = canHaveChilds;
        this.simple = simple;
    }
}

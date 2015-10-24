/*
 * Created on 2/8/2005
 */
package com.ramussoft.pb.data;

/**
 * @author ZDD
 */
public class RowOption {
    public Object row;

    public RowOption(final Object o) {
        row = o;
    }

    public boolean expanded = true;

    public boolean showButton = false;

    public boolean visible = true;

    public boolean big = false;

    public boolean checked = false;
}

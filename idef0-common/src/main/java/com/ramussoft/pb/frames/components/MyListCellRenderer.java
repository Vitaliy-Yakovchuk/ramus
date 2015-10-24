/*
 * Created on 1/8/2005
 */
package com.ramussoft.pb.frames.components;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.dsoft.pb.idef.ResourceLoader;

/**
 * @author ZDD
 */
public class MyListCellRenderer extends DefaultListCellRenderer {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList,
     * java.lang.Object, int, boolean, boolean)
     */
    @Override
    public Component getListCellRendererComponent(final JList arg0, final Object arg1,
                                                  final int arg2, final boolean arg3, final boolean arg4) {
        final String tmp = (String) arg1 + " | "
                + ResourceLoader.getString("example");
        final Component c = super.getListCellRendererComponent(arg0, tmp, arg2, arg3,
                arg4);
        c.setFont(new Font((String) arg1, 0, c.getFont().getSize()));
        return c;
    }
}
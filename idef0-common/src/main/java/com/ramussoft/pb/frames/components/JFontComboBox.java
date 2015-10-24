/*
 * Created on 13/8/2005
 */
package com.ramussoft.pb.frames.components;

import java.awt.GraphicsEnvironment;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.ListCellRenderer;

/**
 * @author ZDD
 */
public class JFontComboBox extends JComboBox {

    public static final String fns[] = GraphicsEnvironment
            .getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    static ListCellRenderer mr = new MyListCellRenderer();

    public JFontComboBox() {
        super();
        final DefaultComboBoxModel model = new DefaultComboBoxModel();
        setModel(model);
        setMaximumRowCount(16);

        setRenderer(mr);
        for (final String element : fns)
            addItem(element);
        setEditable(true);
    }
}

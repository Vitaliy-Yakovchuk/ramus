/*
 * Created on 15/10/2004
 */
package com.ramussoft.pb.frames.components;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JColorChooser;
import javax.swing.JPanel;

/**
 * @author ZDD
 */
public class ColorChooser16 extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JColorChooser jColorChooser = null;

    /**
     * This is the default constructor
     */
    public ColorChooser16() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setLayout(new BorderLayout());
        this.setSize(300, 200);
        this.add(getJColorChooser(), java.awt.BorderLayout.CENTER);
    }

    public void setColor(final Color color) {
        jColorChooser.setColor(color);
    }

    public Color getColor() {
        return jColorChooser.getColor();
    }

    /**
     * This method initializes jColorChooser
     *
     * @return javax.swing.JColorChooser
     */
    private JColorChooser getJColorChooser() {
        if (jColorChooser == null) {
            jColorChooser = new JColorChooser();
        }
        return jColorChooser;
    }
}

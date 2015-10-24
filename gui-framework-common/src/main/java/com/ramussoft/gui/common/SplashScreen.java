package com.ramussoft.gui.common;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;

public abstract class SplashScreen extends JWindow {

    /**
     *
     */
    private static final long serialVersionUID = 1179724982613480085L;

    private JLabel label;

    public SplashScreen() {
        super();
        init();
    }

    private void init() {
        label = getLabel();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(label, BorderLayout.CENTER);
        pack();
    }

    protected JLabel getLabel() {
        return new JLabel(new ImageIcon(getClass().getResource(getImageName())));
    }

    protected abstract String getImageName();

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        if (!b)
            dispose();
    }
}

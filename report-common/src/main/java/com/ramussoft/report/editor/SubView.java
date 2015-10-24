package com.ramussoft.report.editor;

import java.awt.BorderLayout;

import javax.swing.Action;
import javax.swing.JPanel;

public abstract class SubView extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = -3402941378947751916L;

    public SubView() {
        super(new BorderLayout());
    }

    public abstract String getTitle();

    public abstract Action[] getActions();

}

package com.ramussoft.gui.common;

import javax.swing.Action;

import javax.swing.JComponent;
import javax.swing.JPanel;

public abstract class AbstractTabbedView extends AbstractUniqueView implements TabbedView {

    public AbstractTabbedView(GUIFramework framework) {
        super(framework);
    }

    @Override
    public Action[] getActions() {
        return new Action[]{};
    }

    @Override
    public JComponent createComponent() {
        return new JPanel();
    }
}

package com.ramussoft.idef0;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import com.ramussoft.common.Qualifier;

public abstract class AbstractModelsPanel extends JPanel {

    public AbstractModelsPanel() {
        super(new BorderLayout());
    }

    // Функція глючила, тому її прибрали
    // public abstract void createFolder();

    public abstract void openDiagram();

    public abstract void showSelection(OpenDiagram diagram);

    public abstract void createElement(Qualifier qualifier);

    public abstract void deleteSelected();

    public abstract void expandAll();

    public abstract void collapseAll();

    public abstract void close();

    public abstract boolean isDisabeUpdate();

}

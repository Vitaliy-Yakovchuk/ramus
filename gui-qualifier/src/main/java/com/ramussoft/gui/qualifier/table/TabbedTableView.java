package com.ramussoft.gui.qualifier.table;

import java.awt.BorderLayout;

import com.ramussoft.gui.common.AbstractTabbedView;
import com.ramussoft.gui.common.GUIFramework;

public class TabbedTableView extends AbstractTabbedView {

    public static final String MAIN_TABBED_VIEW = "MainTabbedView";

    public TabbedTableView(GUIFramework framework) {
        super(framework);
    }

    @Override
    public String getId() {
        return MAIN_TABBED_VIEW;
    }

    @Override
    public String getDefaultWorkspace() {
        return "Workspace.Qualifiers";
    }

    @Override
    public String getDefaultPosition() {
        return BorderLayout.CENTER;
    }

}

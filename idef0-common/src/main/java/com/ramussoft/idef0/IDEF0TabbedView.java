package com.ramussoft.idef0;

import com.ramussoft.gui.common.AbstractTabbedView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.TabbedTableView;

public class IDEF0TabbedView extends AbstractTabbedView {

    protected static final String IDEF0_TAB_VIEW = TabbedTableView.MAIN_TABBED_VIEW;

    public IDEF0TabbedView(GUIFramework framework) {
        super(framework);
    }

    @Override
    public String getDefaultWorkspace() {
        return "Workspace.IDEF0";
    }

    @Override
    public String getId() {
        return IDEF0_TAB_VIEW;
    }

}

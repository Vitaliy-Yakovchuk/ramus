package com.ramussoft.gui.qualifier.table;

import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;

public abstract class AbstractElementActionPlugin implements
        ElementActionPlugin {

    protected GUIFramework framework;

    @Override
    public String getString(String key) {
        return GlobalResourcesManager.getString(key);
    }

    @Override
    public void setFramework(GUIFramework framework) {
        this.framework = framework;
    }

}

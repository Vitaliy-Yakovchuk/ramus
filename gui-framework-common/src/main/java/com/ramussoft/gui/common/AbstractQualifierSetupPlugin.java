package com.ramussoft.gui.common;

public abstract class AbstractQualifierSetupPlugin implements
        QualifierSetupPlugin {

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

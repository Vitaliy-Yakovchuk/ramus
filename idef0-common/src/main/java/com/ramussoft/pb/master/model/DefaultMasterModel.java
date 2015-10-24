package com.ramussoft.pb.master.model;

public class DefaultMasterModel extends AbstractMasterModel {

    private final Properties[] properties;

    public DefaultMasterModel(final Properties[] properties) {
        this.properties = properties;
    }

    public Properties getPanel(final int i) {
        return properties[i];
    }

    public int getPanelCount() {
        return properties.length;
    }

}

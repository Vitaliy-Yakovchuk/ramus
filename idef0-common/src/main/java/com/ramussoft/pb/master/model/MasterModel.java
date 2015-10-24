package com.ramussoft.pb.master.model;

public interface MasterModel {
    int getPanelCount();

    Properties getPanel(int i);

    String[] getKeys();

    Property getProperty(String key);
}

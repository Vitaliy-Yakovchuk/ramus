package com.ramussoft.report;

import java.util.ResourceBundle;

import com.ramussoft.gui.common.GlobalResourcesManager;

public class ReportResourceManager {

    private static ResourceBundle bundle = ResourceBundle
            .getBundle("com.ramussoft.report.reportgui");

    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return GlobalResourcesManager.getString(key);
        }
    }
}

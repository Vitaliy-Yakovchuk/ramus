package com.ramussoft.chart.gui;

import java.util.ResourceBundle;

import com.ramussoft.gui.common.GlobalResourcesManager;

public class ChartResourceManager {

    private static ResourceBundle bundle = ResourceBundle
            .getBundle("com.ramussoft.chart.labels");

    public static String getString(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return GlobalResourcesManager.getString(key);
        }
    }

}

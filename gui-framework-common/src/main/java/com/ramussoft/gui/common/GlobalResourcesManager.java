package com.ramussoft.gui.common;

import java.util.ResourceBundle;

public class GlobalResourcesManager {

    private static final ResourceBundle global = ResourceBundle
            .getBundle("com.ramussoft.gui.global");

    public static String getString(String key) {
        if (key == null)
            return "Null";
        // throw new NullPointerException("Key == null");
        try {
            return global.getString(key);
        } catch (Exception e) {
            // System.err.println("Key: " + key + ", not found");
            return null;
        }
    }

}

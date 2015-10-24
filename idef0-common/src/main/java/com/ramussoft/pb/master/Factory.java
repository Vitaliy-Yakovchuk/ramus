package com.ramussoft.pb.master;

import java.util.ResourceBundle;

import com.dsoft.pb.idef.ResourceLoader;

public class Factory {
    private static ResourceBundle bundle = ResourceBundle.getBundle(
            "com.dsoft.pb.master.local", ResourceLoader.getLocale());

    public static String getNullString(final String key) {
        return bundle.getString(key);
    }

    public static String getString(final String key) {
        try {
            return bundle.getString(key);
        } catch (final Exception e) {
            return ResourceLoader.getString(key);
        }
    }
}

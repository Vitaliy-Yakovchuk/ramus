package com.ramussoft.pb;

import javax.swing.JFrame;

import com.ramussoft.common.Metadata;

public class Main {

    public static DataPlugin dataPlugin;

    public static JFrame getMainFrame() {
        return null;
    }

    public static String getProgramName() {
        return Metadata.getApplicationName();
    }

    public static String getVersion() {
        return Metadata.getApplicationVersion();
    }

}

package com.ramussoft.gui.common.prefrence;

import java.io.File;

public abstract class OptionsDirectoryGetter {

    public abstract String getProgramName();

    public String getDirectoryName() {
        String res = getDirectoryNameA();
        if (res.endsWith(File.separator))
            return res.substring(0, res.length() - File.separator.length());
        if (res.endsWith("/"))
            return res.substring(0, res.length() - 1);
        return res;
    }

    private String getDirectoryNameA() {
        try {

            String userHome = null;
            userHome = System.getProperty("user.home");
            String applicationId = getProgramName();
            if (SystemUtils.IS_OS_WINDOWS) {
                File appDataDir = null;
                String appDataEV = System.getenv("APPDATA");
                if ((appDataEV != null) && (appDataEV.length() > 0)) {
                    appDataDir = new File(appDataEV);
                } else
                    return getDefaultDirectoryName();
                String vendorId = getVendorId();
                if (appDataDir.isDirectory()) {
                    String path = appDataEV + "\\" + vendorId + "\\"
                            + applicationId;
                    return path;
                } else {
                    return getDefaultDirectoryName();
                }
            } else if (SystemUtils.IS_OS_MAC_OSX) {
                // ${userHome}/Library/Application Support/${applicationId}
                String path = "Library/Application Support/" + applicationId
                        + "/";
                return new File(userHome, path).getAbsolutePath();
            } else {
                return getDefaultDirectoryName();
            }

        } catch (Exception e) {
            return getDefaultDirectoryName();
        }
    }

    public String getVendorId() {
        return "Ramussoft";
    }

    private String getDefaultDirectoryName() {
        String home = SystemUtils.USER_HOME;
        if (!home.endsWith(File.separator))
            home += File.separator;
        String pn = "." + getProgramName().toLowerCase().replaceAll(" ", "-");
        return home + pn;
    }

}

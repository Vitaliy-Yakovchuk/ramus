package com.ramussoft.core.impl;

public class SystemUtils {

    private static final String OS_NAME_WINDOWS_PREFIX = "Windows";

    public static final String OS_NAME = getSystemProperty("os.name");

    public static final boolean IS_OS_MAC_OSX = getOSMatches("Mac OS X");

    public static final boolean IS_OS_WINDOWS = getOSMatches(OS_NAME_WINDOWS_PREFIX);

    public SystemUtils() {
        super();
    }

    private static boolean getOSMatches(String osNamePrefix) {
        if (OS_NAME == null) {
            return false;
        }
        return OS_NAME.startsWith(osNamePrefix);
    }

    private static String getSystemProperty(String property) {
        try {
            return System.getProperty(property);
        } catch (SecurityException ex) {
            System.err
                    .println("Caught a SecurityException reading the system property '"
                            + property
                            + "'; the SystemUtils property value will default to null.");
            return null;
        }
    }

}

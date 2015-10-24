package com.ramussoft.common;

public class FileVersionException extends Exception {

    public FileVersionException(String string) {
        super(string);
    }

    public FileVersionException(String[] names, String[] filePlugins,
                                String plugin) {
        super("File plugin not registered: \"" + plugin
                + "\" Please use other version of the application.");
    }

    /**
     *
     */
    private static final long serialVersionUID = -1652463406548052813L;

}

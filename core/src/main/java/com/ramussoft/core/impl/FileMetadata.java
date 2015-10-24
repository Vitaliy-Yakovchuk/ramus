package com.ramussoft.core.impl;

import java.util.Properties;

public class FileMetadata {

    private String[] plugins;

    private String applicationVersion;

    private String fileOpenMinimumVersion;

    public FileMetadata(Properties properties) {
        int count = Integer.parseInt(properties.getProperty("PluginCount"));
        plugins = new String[count];
        for (int i = 0; i < count; i++) {
            plugins[i] = properties.getProperty("Plugin_" + i);
        }

        applicationVersion = properties.getProperty("ApplicationVersion");
        fileOpenMinimumVersion = properties
                .getProperty("FileOpenMinimumVersion");
    }

    /**
     * @param plugins the plugins to set
     */
    public void setPlugins(String[] plugins) {
        this.plugins = plugins;
    }

    /**
     * @return the plugins
     */
    public String[] getPlugins() {
        return plugins;
    }

    /**
     * @param applicationVersion the applicationVersion to set
     */
    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    /**
     * @return the applicationVersion
     */
    public String getApplicationVersion() {
        return applicationVersion;
    }

    /**
     * @param fileOpenMinimumVersion the fileOpenMinimumVersion to set
     */
    public void setFileOpenMinimumVersion(String fileOpenMinimumVersion) {
        this.fileOpenMinimumVersion = fileOpenMinimumVersion;
    }

    /**
     * @return the fileOpenMinimumVersion
     */
    public String getFileOpenMinimumVersion() {
        return fileOpenMinimumVersion;
    }

}

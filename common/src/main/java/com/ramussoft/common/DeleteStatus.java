package com.ramussoft.common;

import java.io.Serializable;

public class DeleteStatus implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 7469103614514999745L;

    private String pluginName;

    private Delete delete = Delete.WARNING;

    private String pluginAnswer;

    /**
     * @param pluginName the pluginName to set
     */
    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * @return the pluginName
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * @param delete the delete to set
     */
    public void setDelete(Delete delete) {
        this.delete = delete;
    }

    /**
     * @return the delete
     */
    public Delete getDelete() {
        return delete;
    }

    /**
     * @param pluginAnswer the pluginAnswer to set
     */
    public void setPluginAnswer(String pluginAnswer) {
        this.pluginAnswer = pluginAnswer;
    }

    /**
     * @return the pluginAnswer
     */
    public String getPluginAnswer() {
        return pluginAnswer;
    }
}

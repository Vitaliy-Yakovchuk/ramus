package com.ramussoft.core.impl;

import com.ramussoft.common.FileVersionException;

public class FileMinimumVersionException extends FileVersionException {

    /**
     *
     */
    private static final long serialVersionUID = -1750537043746624615L;

    private String minimumVersion;

    public FileMinimumVersionException(String string) {
        super("Minimum version to open file: " + string);
        this.minimumVersion = string;
    }

    public Object getMinimumVersion() {
        return minimumVersion;
    }

}

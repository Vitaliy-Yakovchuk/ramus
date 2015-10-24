package com.ramussoft.common;

import java.text.MessageFormat;

public class QualifierNotExistsException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 3304260831998373580L;


    public QualifierNotExistsException(long id) {
        super(MessageFormat.format("Qualifier with {0} id not exists", id));
    }
}

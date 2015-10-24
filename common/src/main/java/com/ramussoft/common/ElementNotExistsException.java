package com.ramussoft.common;

import java.text.MessageFormat;

public class ElementNotExistsException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 3304260831998373580L;


    public ElementNotExistsException(long id) {
        super(MessageFormat.format("Element with {0} id not exists", id));
    }
}

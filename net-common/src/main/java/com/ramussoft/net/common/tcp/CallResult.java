package com.ramussoft.net.common.tcp;

import java.io.Serializable;

public class CallResult implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3019849116040788261L;

    public Object result;

    public EvenstHolder holder;

    public Exception exception;

    public Object getResult() {
        return result;
    }

    public EvenstHolder getHolder() {
        return holder;
    }

    public Exception getException() {
        return exception;
    }
}

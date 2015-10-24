package com.ramussoft.net.common.tcp;

import java.io.Serializable;

public class CallParameters implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6452209941433267022L;

    public String methodName;

    public Object[] parameters;

    public CallParameters(String methodName, Object[] parameters) {
        this.methodName = methodName;
        this.parameters = parameters;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }

}

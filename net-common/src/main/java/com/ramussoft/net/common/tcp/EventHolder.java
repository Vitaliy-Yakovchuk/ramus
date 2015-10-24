package com.ramussoft.net.common.tcp;

import java.io.Serializable;

public class EventHolder implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3372395378048177203L;

    private String interfaceName;

    private String methodName;

    private Object[] parameters;

    public EventHolder(String interfaceName, String methodName, Object[] parameters) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameters = parameters;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getParameters() {
        return parameters;
    }
}

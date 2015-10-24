package com.ramussoft.eval;

public class UnknownValuesException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -9088090479301091378L;

    private String[] values;

    public UnknownValuesException(String[] values) {
        this.values = values;
    }

    public String[] getValues() {
        return values;
    }

}

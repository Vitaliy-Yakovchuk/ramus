package com.ramussoft.eval.event;

public class FunctionsChangeEvent {

    private String[] functionNames;

    /**
     * @param functionNames the functionNames to set
     */
    public FunctionsChangeEvent(String[] functionNames) {
        this.functionNames = functionNames;
    }

    /**
     * @return the functionNames
     */
    public String[] getFunctionNames() {
        return functionNames;
    }

}

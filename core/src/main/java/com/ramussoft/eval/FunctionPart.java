package com.ramussoft.eval;

public class FunctionPart extends Part {

    private Function function;

    private String name;

    public FunctionPart(Value[] values, String functionName) {
        super(values);
        this.name = functionName;
    }

    @Override
    public EObject calculate() {
        EObject[] params = new EObject[values.length];
        for (int i = 0; i < params.length; i++)
            params[i] = values[i].get();
        return function.calculate(params);
    }

    public Function getFunction() {
        return function;
    }

    public String getName() {
        return name;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    @Override
    protected void space(StringBuffer sb) {
        sb.append(";");
    }

    @Override
    public void fill(StringBuffer sb) {
        sb.append(name);
        sb.append('(');
        super.fill(sb);
        sb.append(')');
    }

    public void setName(String name) {
        this.name = name;
    }

}

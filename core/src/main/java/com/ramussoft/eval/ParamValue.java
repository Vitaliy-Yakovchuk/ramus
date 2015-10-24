package com.ramussoft.eval;

public class ParamValue implements Value {

    private Value parent;

    public ParamValue(Value parent) {
        this.parent = parent;
    }

    @Override
    public void fill(StringBuffer sb) {
        parent.fill(sb);
        sb.append(';');
    }

    @Override
    public EObject get() {
        return parent.get();
    }

}

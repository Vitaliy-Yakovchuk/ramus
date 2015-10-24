package com.ramussoft.eval;

public class PValue implements Value {

    private Value parent;

    public PValue(Value parent) {
        this.parent = parent;
    }

    @Override
    public void fill(StringBuffer sb) {
        parent.fill(sb);
    }

    @Override
    public EObject get() {
        return parent.get();
    }

}

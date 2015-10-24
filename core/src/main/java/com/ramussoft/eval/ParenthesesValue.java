package com.ramussoft.eval;

public class ParenthesesValue implements Value {

    private Value parent;

    public ParenthesesValue(Value parent) {
        this.parent = parent;
    }

    @Override
    public EObject get() {
        return parent.get();
    }

    @Override
    public void fill(StringBuffer sb) {
        sb.append('(');
        parent.fill(sb);
        sb.append(')');

    }

}

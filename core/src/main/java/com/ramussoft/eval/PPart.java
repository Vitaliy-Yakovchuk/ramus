package com.ramussoft.eval;

public class PPart extends Part {

    public PPart(Value[] values) {
        super(values);
    }

    @Override
    public EObject calculate() {
        return values[0].get();
    }

    @Override
    protected void space(StringBuffer sb) {
    }

    @Override
    public void fill(StringBuffer sb) {
        sb.append('(');
        super.fill(sb);
        sb.append(')');
    }

}

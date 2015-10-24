package com.ramussoft.eval;

public abstract class Part {

    protected Value[] values;

    public Part(Value[] values) {
        this.values = values;
    }

    public abstract EObject calculate();

    public void fill(StringBuffer sb) {
        if (values.length > 0) {
            values[0].fill(sb);
        }
        for (int i = 1; i < values.length; i++) {
            space(sb);
            values[i].fill(sb);
        }
    }

    protected abstract void space(StringBuffer sb);

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        fill(sb);
        return sb.toString();
    }
}

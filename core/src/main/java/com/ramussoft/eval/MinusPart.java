package com.ramussoft.eval;

public class MinusPart extends MathPart {

    public MinusPart(Value[] values) {
        super(values);
    }

    @Override
    protected EObject calc(EObject a, EObject b) {
        return new EObject(a.doubleValue() - b.doubleValue());
    }

    @Override
    protected void space(StringBuffer sb) {
        sb.append('-');
    }

}

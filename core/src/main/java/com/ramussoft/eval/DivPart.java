package com.ramussoft.eval;

public class DivPart extends MathPart {

    public DivPart(Value[] values) {
        super(values);
    }

    @Override
    protected EObject calc(EObject a, EObject b) {
        return new EObject(a.doubleValue() / b.doubleValue());
    }

    @Override
    protected void space(StringBuffer sb) {
        sb.append('/');
    }

}

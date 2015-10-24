package com.ramussoft.eval;

public class PowerPart extends MathPart {

    public PowerPart(Value[] values) {
        super(values);
    }

    @Override
    protected EObject calc(EObject a, EObject b) {
        return new EObject(Math.pow(a.doubleValue(), b.doubleValue()));
    }

    @Override
    protected void space(StringBuffer sb) {
        sb.append('^');
    }

}

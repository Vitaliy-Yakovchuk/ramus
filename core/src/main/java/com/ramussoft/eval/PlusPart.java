package com.ramussoft.eval;

public class PlusPart extends MathPart {

    public PlusPart(Value[] values) {
        super(values);
    }

    @Override
    protected EObject calc(EObject a, EObject b) {
        if ((a.getValue() instanceof String)
                || (b.getValue() instanceof String))
            return new EObject(a.stringValue() + b.stringValue());
        return new EObject(a.doubleValue() + b.doubleValue());
    }

    @Override
    protected void space(StringBuffer sb) {
        sb.append('+');
    }

}

package com.ramussoft.eval;

public class EqualsPart extends MathPart {

    public EqualsPart(Value[] values) {
        super(values);
    }

    @Override
    protected EObject calc(EObject a, EObject b) {
        if (a.getValue() != null) {
            if (a.getValue().equals(b.getValue()))
                return new EObject(1);
        } else {
            if (b.getValue() == null)
                return new EObject(1);
        }
        return new EObject(0);
    }

    @Override
    protected void space(StringBuffer sb) {
        sb.append('=');
    }

}

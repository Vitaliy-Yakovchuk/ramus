package com.ramussoft.eval;

public class NotEqualsPart extends MathPart {

    public NotEqualsPart(Value[] values) {
        super(values);
    }

    @Override
    protected EObject calc(EObject a, EObject b) {
        EqualsPart equalsPart = new EqualsPart(values);
        if (equalsPart.calc(a, b).longValue() != 0l)
            return new EObject(0);
        return new EObject(1);
    }

    @Override
    protected void space(StringBuffer sb) {
        sb.append("!=");
    }

}

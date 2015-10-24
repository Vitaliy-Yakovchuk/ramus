package com.ramussoft.eval;

public class NotPart extends MathPart {

    public NotPart(Value[] values) {
        super(values);
    }

    @Override
    protected EObject calc(EObject a, EObject b) {
        if (a.getValue() != null) {
            throw new RuntimeException("Wrong construction: " + this.toString());
        }
        if (b.getValue() instanceof String) {
            if (b.stringValue().length() > 0)
                return new EObject(0);
            else
                return new EObject(1);
        }
        if (b.longValue() != 0l)
            return new EObject(0);
        else
            return new EObject(1);
    }

    @Override
    protected void space(StringBuffer sb) {
        sb.append("!");
    }

}

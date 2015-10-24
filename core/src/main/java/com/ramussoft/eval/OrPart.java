package com.ramussoft.eval;

public class OrPart extends MathPart {

    public OrPart(Value[] values) {
        super(values);
    }

    @Override
    protected EObject calc(EObject a, EObject b) {
        long i1;
        long i2;
        if (a.getValue() instanceof String)
            i1 = a.stringValue().length();
        else
            i1 = a.longValue();

        if (b.getValue() instanceof String)
            i2 = b.stringValue().length();
        else
            i2 = b.longValue();

        if (i1 != 0 || i2 != 0)
            return new EObject(1);
        return new EObject(0);
    }

    @Override
    protected void space(StringBuffer sb) {
        sb.append('|');
    }

}

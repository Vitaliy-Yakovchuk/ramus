package com.ramussoft.eval;

public class QuestionSPart extends MathPart {

    public QuestionSPart(Value[] values) {
        super(values);
    }

    @Override
    protected EObject calc(EObject a, EObject b) {
        if ((a.isBoolean()) && (a.longValue() == 0l))
            return b;
        return a;
    }

    @Override
    protected void space(StringBuffer sb) {
        sb.append(':');
    }

}

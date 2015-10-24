package com.ramussoft.eval;

public class QuestionPart extends MathPart {

    public QuestionPart(Value[] values) {
        super(values);
    }

    @Override
    protected EObject calc(EObject a, EObject b) {
        if (a.getValue() instanceof String) {
            if (a.stringValue().length() > 0)
                return b;
        } else {
            if (a.longValue() != 0l)
                return b;
        }
        return new EObject(Boolean.FALSE);
    }

    @Override
    protected void space(StringBuffer sb) {
        sb.append('?');

    }

}

package com.ramussoft.eval;

import java.text.Collator;

public abstract class MathPart extends Part {

    protected static Collator collator = Collator.getInstance();

    public MathPart(Value[] values) {
        super(values);
    }

    @Override
    public EObject calculate() {
        if (values.length > 0) {
            EObject a;
            a = values[0].get();
            for (int i = 1; i < values.length; i++) {
                EObject b = values[i].get();
                a = calc(a, b);
            }
            return a;
        }
        return new EObject(0d);
    }

    protected abstract EObject calc(EObject a, EObject b);

}

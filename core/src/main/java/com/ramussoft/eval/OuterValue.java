package com.ramussoft.eval;

public class OuterValue extends ConstValue {

    public OuterValue(EObject value) {
        super(null);
    }

    public void setValue(EObject value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

}

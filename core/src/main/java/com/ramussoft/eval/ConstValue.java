package com.ramussoft.eval;

public class ConstValue implements Value {

    protected EObject value;

    public ConstValue(EObject value) {
        this.value = value;
    }

    @Override
    public EObject get() {
        return value;
    }

    @Override
    public void fill(StringBuffer sb) {
        if (value.getValue() != null) {
            if (value.getValue() instanceof String)
                sb.append("\'" + value.stringValue() + "\'");
            else
                sb.append(value.stringValue());
        }
    }

}

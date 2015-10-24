package com.ramussoft.eval;

public class MorePart extends MathPart {

    public MorePart(Value[] values) {
        super(values);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected EObject calc(EObject a, EObject b) {
        if ((a.getValue() instanceof String)
                || (b.getValue() instanceof String)) {
            int compare = collator.compare(a.stringValue(), b.stringValue());
            if (compare > 0)
                return new EObject(1);
            return new EObject(0);
        }
        if ((a.getValue() instanceof Comparable)
                && (b.getValue() instanceof Comparable)) {
            int compare = ((Comparable) a.getValue()).compareTo(b.getValue());
            if (compare > 0)
                return new EObject(1);
            return new EObject(0);
        }
        if (a.getValue() == null)
            return new EObject(0);
        if (b.getValue() == null)
            return new EObject(1);
        return new EObject(0);
    }

    @Override
    protected void space(StringBuffer sb) {
        sb.append('>');
    }

}

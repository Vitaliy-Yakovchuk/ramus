package com.ramussoft.eval;

public class WorkStringPart extends Part {

    private Eval[] evals;

    private String[] strings;

    public WorkStringPart(Eval[] evals, String[] strings) {
        super(null);
        this.evals = evals;
        this.strings = strings;
    }

    @Override
    public EObject calculate() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < strings.length; i++) {
            sb.append(strings[i]);
            if (i < evals.length) {
                Object object = evals[i].calculate();
                if (object instanceof Double) {
                    sb.append(Eval.format.format(object));
                } else {
                    if (object != null)
                        sb.append(object.toString());
                }
            }
        }
        return new EObject(sb.toString());
    }

    @Override
    protected void space(StringBuffer sb) {
    }

    @Override
    public void fill(StringBuffer sb) {
        sb.append('\"');
        if (strings.length > 0)
            sb.append(strings[0]);
        for (int i = 1; i < strings.length; i++) {
            sb.append("#{");
            sb.append(evals[i - 1].toString());
            sb.append('}');
            sb.append(strings[i]);
        }
        sb.append('\"');
    }

}

package com.ramussoft.eval;

public class PartValue implements Value {

    private Part part;

    public PartValue(Part part) {
        this.part = part;
    }

    @Override
    public EObject get() {
        return part.calculate();
    }

    @Override
    public void fill(StringBuffer sb) {
        part.fill(sb);
    }

}

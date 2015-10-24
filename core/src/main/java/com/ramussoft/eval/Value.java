package com.ramussoft.eval;

public interface Value {

    EObject get();

    void fill(StringBuffer sb);
}

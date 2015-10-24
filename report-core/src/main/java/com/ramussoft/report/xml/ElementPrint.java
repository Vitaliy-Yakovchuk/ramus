package com.ramussoft.report.xml;

import java.util.List;
import java.util.Map;

import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Out;

public abstract class ElementPrint {

    protected Map<String, String> attributes;

    public ElementPrint(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public abstract void print(Out out, Data data);

    public void print(List<Object> out, Data data) {
        throw new RuntimeException(
                "Call void print(Out out, Data data) instead of this");
    }

}

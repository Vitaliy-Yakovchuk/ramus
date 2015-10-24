package com.ramussoft.report.xml;

import java.util.List;

import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Out;

public class TextPrint extends ElementPrint {

    private String text;

    public TextPrint(String text) {
        super(null);
        this.text = text;
    }

    @Override
    public void print(Out out, Data data) {
        out.print(text);
    }

    @Override
    public void print(List<Object> out, Data data) {
        out.add(text);
    }

}

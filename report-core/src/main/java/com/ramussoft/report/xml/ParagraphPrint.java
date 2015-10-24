package com.ramussoft.report.xml;

import java.util.Hashtable;

import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Out;

public class ParagraphPrint extends LabelPrint {

    public ParagraphPrint(Hashtable<String, String> attributes, Data data) {
        super(attributes, data);
    }

    @Override
    public void print(Out out, Data data) {
        if (data.isPrintFor(attributes.get("printFor"))) {
            super.print(out, data);
        }
    }

    @Override
    protected void afterPrint(Out out, Data data) {
        out.print("</p>");
    }

    @Override
    protected void beforePrint(Out out, Data data) {
        String style = getStyle();
        if (style == "")
            out.print("<p>");
        else
            out.print("<p style=\"" + style + "\">");
    }

}

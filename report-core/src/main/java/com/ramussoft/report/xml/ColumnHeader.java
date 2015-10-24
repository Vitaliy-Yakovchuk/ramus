package com.ramussoft.report.xml;

import java.util.Hashtable;

import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Out;

public class ColumnHeader extends LabelPrint {

    public ColumnHeader(Hashtable<String, String> attributes, Data data) {
        super(attributes, data);
    }

    @Override
    public void print(Out out, Data data) {
        String style = getStyle();
        if (style.length() > 0)
            style = " style=\"" + style + "\"";
        if (attributes.get("ColumntWidth") == null)
            out.println("<th" + style + ">");
        else
            out.println("<th" + style + " width=\""
                    + attributes.get("ColumntWidth") + "\">");
        super.print(out, data);
        out.println("</th>");
    }

    @Override
    protected void afterPrint(Out out, Data data) {
    }

    @Override
    protected void beforePrint(Out out, Data data) {
    }
}

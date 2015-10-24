package com.ramussoft.report.xml;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Out;

public class ColumnBody extends ElementPrint {

    private final String text;

    private TablePrint tablePrint;

    public ColumnBody(Hashtable<String, String> attributes) {
        super(attributes);
        String text = attributes.get("Text");
        if (text == null)
            text = "";
        this.text = text;
    }

    public void setTablePrint(TablePrint tablePrint) {
        this.tablePrint = tablePrint;
    }

    @Override
    public void print(Out out, Data data) {
        throw new RuntimeException(
                "call void print(List<Object> buffer, Data data) instead of this");

    }

    @Override
    public void print(List<Object> out, Data data) {
        for (ElementPrint print : build())
            print.print(out, data);
    }

    public String getTdStartTag() {
        String style = getStyle();
        if (style.length() > 0)
            style = " style=\"" + style + "\"";
        return "<td" + style + ">";
    }

    protected List<ElementPrint> build() {
        List<ElementPrint> result = new ArrayList<ElementPrint>();

        StringBuffer sb = new StringBuffer();
        boolean inQuery = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c == '\\')
                    && (i + 1 < text.length())
                    && ((text.charAt(i + 1) == '[') || (text.charAt(i + 1) == ']')))
                sb.append(text.charAt(i + 1));
            else {
                if (inQuery) {
                    if (c == ']') {
                        inQuery = false;
                        result
                                .add(new TableBodyQuery(sb.toString(),
                                        tablePrint));
                        sb = new StringBuffer();
                    } else
                        sb.append(c);
                } else {
                    if (c == '[') {
                        inQuery = true;
                        if (sb.length() > 0) {
                            result.add(new TextPrint(sb.toString()));
                            sb = new StringBuffer();
                        }
                    } else
                        sb.append(c);
                }
            }

        }

        if (sb.length() > 0)
            result.add(new TextPrint(sb.toString()));

        return result;
    }

    protected String getStyle() {
        String style = attributes.get("Style");
        if (style == null)
            style = "";
        String tmp;
        if ((tmp = attributes.get("FontType")) != null) {
            if (tmp.equals("1"))
                style += "font-weight: bold;";
            else if (tmp.equals("2"))
                style += "font-style: italic; ";
            else if (tmp.equals("3"))
                style += "font-style: italic;font-weight: bold;";
        }
        if ((tmp = attributes.get("Font")) != null) {
            style += " font-family: " + tmp + ";";
        }
        if ((tmp = attributes.get("FontSize")) != null) {
            style += "font-size: " + tmp + ";";
        }
        if ((tmp = attributes.get("TextAlign")) != null) {
            style += "text-align: " + tmp + ";";
        }
        return style;
    }
}

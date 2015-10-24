package com.ramussoft.report.xml;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Out;

public abstract class LabelPrint extends ElementPrint {

    private final String text;

    private boolean printed;

    private List<ElementPrint> elements;

    public LabelPrint(Hashtable<String, String> attributes, Data data) {
        super(attributes);
        String text = attributes.get("Text");
        if (text == null)
            this.text = "";
        else
            this.text = attributes.get("Text");
        elements = build(data);
    }

    @Override
    public void print(Out out, Data data) {
        printed = false;
        for (ElementPrint element : elements)
            if (element instanceof TextPrint) {
                printed = true;
                beforePrint(out, data);
            }
        if (elements.size() > 0) {
            for (ElementPrint element : elements)
                element.print(out, data);
        }

        if (printed)
            afterPrint(out, data);

    }

    protected abstract void beforePrint(Out out, Data data);

    protected abstract void afterPrint(Out out, Data data);

    protected List<ElementPrint> build(Data data) {
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
                        result.add(new QueryPrint(sb.toString(), data) {

                            @Override
                            protected void afterPrint(Out out, Data data) {
                            }

                            @Override
                            protected void beforePrint(Out out, Data data) {
                                if (!printed) {
                                    LabelPrint.this.beforePrint(out, data);
                                    printed = true;
                                }
                            }

                        });
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

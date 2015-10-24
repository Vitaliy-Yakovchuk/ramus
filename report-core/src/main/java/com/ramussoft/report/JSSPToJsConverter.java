package com.ramussoft.report;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

public class JSSPToJsConverter {

    private String code;

    public JSSPToJsConverter(String code) {
        this.code = code;
    }

    public String convert() {
        Source source = new Source(code);
        source.fullSequentialParse();

        int from = 0;

        StringBuffer result = new StringBuffer();

        for (StartTag tag : source.getAllStartTags()) {
            if (tag.getName().startsWith("%")) {

                addLines(result, source, from, tag.getBegin());

                from = tag.getEnd();

                String script = tag.toString();
                if (script.length() > 4) {
                    if (script.charAt(2) == '=') {
                        result.append("doc.print(");
                        result.append(script.substring(3, script.length() - 2));
                        result.append(");");
                    } else {
                        result.append(script.substring(2, script.length() - 2));
                    }
                }
            }
        }

        addLines(result, source, from, source.getEnd());

        return result.toString();
    }

    private void addLines(StringBuffer result, Source source, int from, int to) {
        CharSequence toAdd = source.subSequence(from, to);
        int length = toAdd.length();
        result.append("doc.print(\"");
        for (int i = 0; i < length; i++) {
            char c = toAdd.charAt(i);
            if (c != '\r') {
                if (c == '\n') {
                    result.append("\\n\");\n");
                    if (i == length - 1)
                        return;
                    result.append("doc.print(\"");
                } else {
                    if ((c == '\\') || (c == '\"') || (c == '\'')) {
                        result.append('\\');
                        result.append(c);
                    } else {
                        if (c == '\t') {
                            result.append("\\t");
                        } else
                            result.append(c);
                    }
                }
            }
        }
        result.append("\");");
    }

}

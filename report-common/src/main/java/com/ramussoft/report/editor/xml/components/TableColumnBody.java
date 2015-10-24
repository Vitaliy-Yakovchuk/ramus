package com.ramussoft.report.editor.xml.components;

import static com.ramussoft.report.editor.xml.Attribute.*;
import static com.ramussoft.report.ReportResourceManager.getString;

import java.util.List;

import com.ramussoft.report.editor.xml.Attribute;

public class TableColumnBody extends ColumnElement {

    /**
     *
     */
    private static final long serialVersionUID = 4253191482583444131L;

    @Override
    protected void createAttributes(List<Attribute> list) {
        text = new Attribute(TEXT, "Text",
                getString("ReportAttribute.text"));
        list
                .add(text);
        list
                .add(new Attribute(FONT, "Font",
                        getString("ReportAttribute.font")));
        list.add(new Attribute(INTEGER, "FontSize",
                getString("ReportAttribute.size")));
        list.add(new Attribute(FONT_TYPE, "FontType",
                getString("ReportAttribute.fontType")));
        list.add(new Attribute(TEXT_ALIGMENT, "TextAlign",
                getString("ReportAttribute.textAlign")));
        list.add(new Attribute(TEXT, "Style",
                getString("ReportAttribute.style")));
    }

    @Override
    protected String getXMLName() {
        return "BodyElement";
    }
}

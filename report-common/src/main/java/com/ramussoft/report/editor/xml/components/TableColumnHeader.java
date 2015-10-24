package com.ramussoft.report.editor.xml.components;

import static com.ramussoft.report.ReportResourceManager.getString;
import static com.ramussoft.report.editor.xml.Attribute.FONT;
import static com.ramussoft.report.editor.xml.Attribute.FONT_TYPE;
import static com.ramussoft.report.editor.xml.Attribute.INTEGER;
import static com.ramussoft.report.editor.xml.Attribute.TEXT;
import static com.ramussoft.report.editor.xml.Attribute.TEXT_ALIGMENT;

import java.util.List;

import com.ramussoft.report.editor.xml.Attribute;

public class TableColumnHeader extends ColumnElement {

    /**
     *
     */
    private static final long serialVersionUID = 3814529122864938703L;

    @Override
    protected void createAttributes(List<Attribute> list) {
        text = new Attribute(TEXT, "Text", getString("ReportAttribute.text"));
        list.add(text);
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
        list.add(new Attribute(TEXT, "ColumntWidth",
                getString("ReportAttribute.columnWidth")));
        list.add(new Attribute(TEXT, "ColumnOrderNumber",
                getString("ReportAttribute.orderNumber")));
    }

    @Override
    protected String getXMLName() {
        return "TitleElement";
    }

}

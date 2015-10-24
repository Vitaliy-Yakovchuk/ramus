package com.ramussoft.report.editor.xml.components;

import java.awt.Graphics2D;
import java.util.List;

import org.xml.sax.SAXException;

import com.ramussoft.report.editor.xml.Attribute;
import com.ramussoft.report.editor.xml.ReportSaveXMLReader;
import com.ramussoft.reportgef.gui.Diagram;
import com.ramussoft.reportgef.model.Bounds;

import static com.ramussoft.report.editor.xml.Attribute.*;
import static com.ramussoft.report.ReportResourceManager.getString;

/**
 * Just to store data to xml file
 */

public class Report extends XMLComponent {

    /**
     *
     */
    private static final long serialVersionUID = -2168227716028248486L;

    @Override
    public boolean isY() {
        return false;
    }

    @Override
    public void paint(Graphics2D g, Bounds bounds, Diagram diagram) {
    }

    @Override
    public void storeToXML(ReportSaveXMLReader reportSaveXMLReader)
            throws SAXException {
    }

    @Override
    protected void createAttributes(List<Attribute> list) {
        list.add(new Attribute(BASE_QUALIFIER, "BaseRow",
                getString("ReportAttribute.qualifier")));
        list.add(new Attribute(MODEL, "ReportFunction",
                getString("ReportAttribute.model")));
        list.add(new Attribute(BOOLEAN, "SameBaseQualifier",
                getString("ReportAttribute.sameBaseQualifier")));
    }

    @Override
    protected String getXMLName() {
        return "Report";
    }

    @Override
    public String getTypeName() {
        return "Report";
    }

}

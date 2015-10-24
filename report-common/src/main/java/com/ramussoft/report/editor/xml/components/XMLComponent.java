package com.ramussoft.report.editor.xml.components;

import java.awt.Font;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.SAXException;

import com.ramussoft.report.editor.xml.Attribute;
import com.ramussoft.report.editor.xml.ReportSaveXMLReader;
import com.ramussoft.reportgef.Component;

public abstract class XMLComponent extends Component {

    /**
     *
     */
    private static final long serialVersionUID = -3482854653747856625L;

    protected double width;

    protected double height;

    private List<Attribute> attributes;

    public XMLComponent() {
        this.width = super.getMinWidth();
        this.height = super.getMinHeight();
        attributes = new ArrayList<Attribute>();
        createAttributes(attributes);
    }

    protected abstract void createAttributes(List<Attribute> list);

    protected abstract String getXMLName();

    public abstract String getTypeName();

    @Override
    public double getMinHeight() {
        return height;
    }

    @Override
    public double getMinWidth() {
        return width;
    }

    public void setWidth(double widthForCompontns) {
        this.width = widthForCompontns;
    }

    public abstract boolean isY();

    @Override
    public boolean isResizeableX() {
        return false;
    }

    @Override
    public boolean isResizeableY() {
        return false;
    }

    @Override
    public Font getDefaultFont() {
        return new Font("Dialog", 0, 8);
    }

    public void storeToXML(ReportSaveXMLReader reader) throws SAXException {
        reader.startElement(getXMLName());
        for (Attribute attribute : attributes)
            attribute.storeToXML(reader);
        reader.endElement(getXMLName());
    }

    public void setXMLAttribute(Attribute attribute, Object value) {
        attribute.setValue(value);
    }

    /**
     * @return the attributes
     */
    public List<Attribute> getXMLAttributes() {
        return attributes;
    }

}

package com.ramussoft.report.editor.xml.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Stroke;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.util.List;

import javax.swing.JLabel;

import com.ramussoft.report.editor.xml.Attribute;
import com.ramussoft.reportgef.gui.Diagram;
import com.ramussoft.reportgef.model.Bounds;
import com.ramussoft.reportgef.model.QBounds;

import static com.ramussoft.report.editor.xml.Attribute.*;
import static com.ramussoft.report.ReportResourceManager.getString;

public class Label extends XMLComponent {

    /**
     *
     */
    private static final long serialVersionUID = 5443867486083584354L;

    protected JLabel label = new JLabel();

    protected Attribute text;

    private Stroke stroke = createSelectionStroke();

    public Label() {
        height = 30;
        label.setForeground(Color.black);
    }

    @Override
    public void paint(Graphics2D g, Bounds aBounds, Diagram diagram) {
        QBounds bounds = (QBounds) aBounds;
        label.setLocation(0, 0);
        Dimension size = new Dimension((int) bounds.getSize().getWidth() - 4,
                (int) bounds.getSize().getHeight());
        label.setPreferredSize(size);
        label.setSize(label.getPreferredSize());
        g.setColor(Color.black);
        Line2D line = new Line2D.Double(8, getMinHeight(), getMinWidth() - 16,
                getMinHeight());
        Stroke stroke = g.getStroke();
        g.setStroke(this.stroke);
        g.draw(line);
        g.setStroke(stroke);
        paintText(g);
    }

    protected void paintText(Graphics2D g) {
        g.translate(2, 0);
        label.paint(g);
        g.translate(-2, 0);
    }

    private Stroke createSelectionStroke() {
        float[] f = new float[4];
        f[0] = 3;
        f[1] = 3;
        f[2] = 3;
        f[3] = 3;

        return new BasicStroke(0.5f, BasicStroke.CAP_SQUARE,
                BasicStroke.JOIN_MITER, 1, f, 0);

    }

    @Override
    public boolean isResizeableX() {
        return false;
    }

    @Override
    public boolean isResizeableY() {
        return false;
    }

    @Override
    public boolean isY() {
        return true;
    }

    @Override
    protected String getXMLName() {
        return "Label";
    }

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
        list.add(new Attribute(BOOLEAN, "ConnectWithNextTable",
                getString("ReportAttribute.connectoToNextTable")));
        list.add(new Attribute(PRINT_FOR, "printFor",
                getString("ReportAttribute.printFor")));
        list.add(new Attribute(TEXT, "Style",
                getString("ReportAttribute.style")));
    }

    @Override
    public void setXMLAttribute(Attribute attribute, Object object) {
        if (object != null)
            if (attribute.equals(text))
                label.setText(object.toString());
        super.setXMLAttribute(attribute, object);
    }

    @Override
    public String getTypeName() {
        return "Label";
    }

}

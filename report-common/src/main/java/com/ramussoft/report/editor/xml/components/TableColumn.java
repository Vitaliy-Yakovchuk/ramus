package com.ramussoft.report.editor.xml.components;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

import com.ramussoft.report.ReportResourceManager;
import com.ramussoft.report.editor.xml.Attribute;
import com.ramussoft.reportgef.gui.Diagram;
import com.ramussoft.reportgef.model.Bounds;
import com.ramussoft.reportgef.model.Dimension2DImpl;
import com.ramussoft.reportgef.model.QBounds;

public class TableColumn extends XMLComponent {

    /**
     *
     */
    private static final long serialVersionUID = -2023890730948167857L;

    private Table table;

    private TableColumnBody columnBody;

    private TableColumnHeader columnHeader;

    public TableColumn() {
        height = 65;
    }

    @Override
    public boolean isY() {
        return false;
    }

    @Override
    public void paint(Graphics2D g, Bounds bounds, Diagram diagram) {
        QBounds qBounds = (QBounds) bounds;
        columnHeader.paint(g, createHeaderBounds(qBounds), diagram);
        g.translate(0, 35);
        columnBody.paint(g, createBodyBounds(qBounds), diagram);
        g.translate(0, -35);
        g.draw(new Line2D.Double(0, 0, 0, height));
    }

    private Bounds createHeaderBounds(QBounds mBounds) {
        QBounds bounds = new QBounds();
        Point2D p = mBounds.getLocation();
        bounds.setLocation(new Point2D.Double(p.getX(), p.getY()));
        bounds.setBackground(columnHeader.getDefaultBackground());
        bounds.setFont(columnHeader.getDefaultFont());
        bounds.setFontColor(columnHeader.getDefaultFontColor());
        bounds.setForeground(columnHeader.getDefaultForeground());

        bounds.setSize(new Dimension2DImpl(mBounds.getSize().getWidth(),
                columnHeader.getMinHeight()));
        return bounds;
    }

    private Bounds createBodyBounds(QBounds mBounds) {
        QBounds bounds = new QBounds();
        Point2D p = mBounds.getLocation();
        bounds.setLocation(new Point2D.Double(p.getX(), p.getY() + 25));
        bounds.setBackground(columnBody.getDefaultBackground());
        bounds.setFont(columnBody.getDefaultFont());
        bounds.setFontColor(columnBody.getDefaultFontColor());
        bounds.setForeground(columnBody.getDefaultForeground());

        bounds.setSize(new Dimension2DImpl(mBounds.getSize().getWidth(),
                columnBody.getMinHeight()));
        return bounds;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Table getTable() {
        return table;
    }

    public TableColumnBody getColumnBody() {
        return columnBody;
    }

    public TableColumnHeader getColumnHeader() {
        return columnHeader;
    }

    @Override
    public void setWidth(double widthForCompontns) {
        super.setWidth(widthForCompontns);
        columnBody.setWidth(widthForCompontns);
        columnHeader.setWidth(widthForCompontns);
    }

    @Override
    protected void createAttributes(List<Attribute> list) {
        columnBody = new TableColumnBody();
        columnHeader = new TableColumnHeader();

        columnBody.setTableColumn(this);
        columnHeader.setTableColumn(this);

        List<Attribute> head = columnHeader.getXMLAttributes();
        List<Attribute> body = columnBody.getXMLAttributes();

        String headAdd = ReportResourceManager
                .getString("ReportAttribute.HeadAdd");
        String bodyAdd = ReportResourceManager
                .getString("ReportAttribute.BodyAdd");
        for (Attribute attribute : head) {
            if (body.indexOf(attribute) >= 0)
                attribute.setTitle(attribute.getTitle() + headAdd);
            list.add(attribute);
        }
        for (Attribute attribute : body) {
            if (head.indexOf(attribute) >= 0)
                attribute.setTitle(attribute.getTitle() + bodyAdd);
            list.add(attribute);
        }

    }

    @Override
    protected String getXMLName() {
        return null;
    }

    @Override
    public void setXMLAttribute(Attribute attribute, Object value) {
        if (columnBody.getXMLAttributes().indexOf(attribute) >= 0)
            columnBody.setXMLAttribute(attribute, value);
        if (columnHeader.getXMLAttributes().indexOf(attribute) >= 0)
            columnHeader.setXMLAttribute(attribute, value);
    }

    @Override
    public String getTypeName() {
        return "TableColumn";
    }
}

package com.ramussoft.report.editor.xml.components;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JLabel;

import org.xml.sax.SAXException;

import com.ramussoft.report.editor.xml.Attribute;
import com.ramussoft.report.editor.xml.ReportSaveXMLReader;
import com.ramussoft.report.editor.xml.XMLDiagram;
import com.ramussoft.reportgef.Component;
import com.ramussoft.reportgef.Group;
import com.ramussoft.reportgef.gui.Diagram;
import com.ramussoft.reportgef.model.Bounds;
import com.ramussoft.reportgef.model.QBounds;

import static com.ramussoft.report.editor.xml.Attribute.*;
import static com.ramussoft.report.ReportResourceManager.getString;

public class Table extends XMLComponent {

    /**
     *
     */
    private static final long serialVersionUID = 8302884362190563063L;

    private TableColumn[] columns = new TableColumn[]{};

    private JLabel label = new JLabel(getString("ReportAttribute.query") + ":");

    private Attribute query;

    public Table() {
        height = 90;
        label.setForeground(Color.black);
    }

    @Override
    public boolean isY() {
        return true;
    }

    @Override
    public void paint(Graphics2D g, Bounds bounds, Diagram diagram) {
        double minWidth = getMinWidth();
        double minHeight = getMinHeight();
        label.setSize((int) minWidth - 18, 20);
        RoundRectangle2D rect = new RoundRectangle2D.Double(0, 3, minWidth,
                minHeight - 3, 15, 15);
        g.draw(rect);
        g.translate(6, 0);
        label.paint(g);
        g.translate(-6, 0);
    }

    public void setColumns(TableColumn[] columns) {
        this.columns = columns;
    }

    public TableColumn[] getColumns() {
        return columns;
    }

    public int getIndexOfColumn(TableColumn column) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(column))
                return i;
        }
        return -1;
    }

    public void addColumn(QBounds tableBound, Diagram diagram,
                          TableColumn column) {
        columns = Arrays.copyOf(columns, columns.length + 1);
        columns[columns.length - 1] = column;
        applyComlumnsSize(tableBound, diagram);
    }

    public void applyComlumnsSize(QBounds tableBound, Diagram diagram) {
        double width = getMinWidth();
        double w = width / columns.length;
        double x = tableBound.getLocation().getX();
        for (TableColumn tableColumn : columns) {
            QBounds bounds = (QBounds) diagram.getBounds(tableColumn);
            Dimension2D size = bounds.getSize();
            size.setSize(w, size.getHeight());
            bounds.setLocation(new Point2D.Double(x, getColumnYLocation(
                    tableBound, size)));
            tableColumn.setWidth(w);
            x += w;
        }
    }

    public void applyComlumnsSize(QBounds tableBound, QBounds[] bounds) {
        double width = getMinWidth();
        double w = width / columns.length;
        double x = tableBound.getLocation().getX();
        for (int i = 0; i < columns.length; i++) {
            TableColumn tableColumn = columns[i];
            Dimension2D size = bounds[i].getSize();
            size.setSize(w, size.getHeight());
            bounds[i].setLocation(new Point2D.Double(x, getColumnYLocation(
                    tableBound, size)));
            tableColumn.setWidth(w);
            x += w;
        }
    }

    private double getColumnYLocation(QBounds tableBound, Dimension2D size) {
        return tableBound.getLocation().getY() + height - size.getHeight() - 5;
    }

    public void applyTransformForGroup(Group group, Diagram diagram) {
        List<TableColumn> list = new ArrayList<TableColumn>();
        for (Bounds bounds : group.getBounds()) {
            Component component = diagram.getComponent(bounds);
            if ((component instanceof TableColumn)
                    && (((TableColumn) component).getTable().equals(this)))
                list.add((TableColumn) component);
        }
        if (list.size() <= 0)
            return;

        int index = getNewIndex(group.getTranslate().getX(), list.get(0),
                diagram);
        if (index < 0)
            return;
        int realIndex = -1;
        List<TableColumn> columns = new ArrayList<TableColumn>();
        for (int i = 0; i < this.columns.length; i++) {
            TableColumn column = this.columns[i];
            if (list.indexOf(column) < 0) {
                if (i == index)
                    realIndex = columns.size();
                columns.add(column);
            }
        }
        if (realIndex < 0)
            realIndex = columns.size();
        for (TableColumn column : list) {
            columns.add(realIndex, column);
            realIndex++;
        }
        this.columns = columns.toArray(new TableColumn[columns.size()]);
        applyComlumnsSize((QBounds) diagram.getBounds(this), diagram);
    }

    private int getNewIndex(double tX, TableColumn tableColumn, Diagram diagram) {
        QBounds qBounds = (QBounds) diagram.getBounds(tableColumn);
        double width = getMinWidth();
        double w = width / columns.length;
        double x = qBounds.getLocation().getX() - XMLDiagram.LEFT + tX;
        double index;
        for (index = 0; index < columns.length; index++) {
            if (w * index > x) {
                int i = (int) index;
                if (columns[i].equals(tableColumn))
                    return -1;
                if ((i > 0) && (columns[i - 1].equals(tableColumn))) {
                    return -1;
                }
                return i;
            }
        }
        int i = (int) index;
        if (i < columns.length)
            if (columns[i].equals(tableColumn))
                return -1;
        if ((i > 0) && (columns[i - 1].equals(tableColumn))) {
            return -1;
        }
        return i;
    }

    public void paintPin(Graphics2D g, double tX, TableColumn tableColumn,
                         XMLDiagram diagram) {
        int index = getNewIndex(tX, tableColumn, diagram);
        if (index < 0)
            return;
        paintXPin(g, index, diagram);
    }

    private void paintXPin(Graphics2D g, int index, XMLDiagram diagram) {
        double y = diagram.getBounds(this).getTop() + 15;
        double width = getMinWidth();
        double w = width / columns.length;
        g.setColor(Color.black);
        double x = w * index + XMLDiagram.LEFT;

        double max = 6;

        g
                .setPaint(new GradientPaint(
                        new Point2D.Double(x - max, y - max), Color.green,
                        new Point2D.Double(x + max, y + max), Color.black));

        GeneralPath path1 = new GeneralPath(Path2D.WIND_EVEN_ODD, 4);
        GeneralPath path2 = new GeneralPath(Path2D.WIND_EVEN_ODD, 4);
        path1.moveTo(x, y);
        path1.lineTo(x + max / 2, y - max);
        path1.lineTo(x - max / 2, y - max);
        path1.lineTo(x, y);

        y += 75;

        path2.moveTo(x, y);
        path2.lineTo(x + max / 2, y + max);
        path2.lineTo(x - max / 2, y + max);
        path2.lineTo(x, y);

        g.fill(path1);

        g
                .setPaint(new GradientPaint(
                        new Point2D.Double(x - max, y - max), Color.black,
                        new Point2D.Double(x + max, y + max), Color.green));
        g.fill(path2);

        g.setColor(Color.gray);
        g.draw(path1);
        g.draw(path2);
    }

    public void removeComlumntIfNeed(List<Component> components,
                                     XMLDiagram diagram) {
        List<TableColumn> columns = new ArrayList<TableColumn>();
        for (TableColumn column : this.columns) {
            if (components.indexOf(column) >= 0) {
                columns.add(column);
            }
        }
        if (columns.size() <= this.columns.length) {
            this.columns = columns.toArray(new TableColumn[columns.size()]);
            applyComlumnsSize((QBounds) diagram.getBounds(this), diagram);
        }
    }

    @Override
    protected void createAttributes(List<Attribute> list) {
        query = new Attribute(TEXT, "Quary", getString("ReportAttribute.query"));
        list.add(query);
        list.add(new Attribute(BORDER_SIZE, "Border",
                getString("ReportAttribute.borderSize")));
        list.add(new Attribute(PRINT_FOR, "printFor",
                getString("ReportAttribute.printFor")));
        list.add(new Attribute(TEXT, "Style",
                getString("ReportAttribute.style")));
    }

    @Override
    protected String getXMLName() {
        return "Table";
    }

    @Override
    public void storeToXML(ReportSaveXMLReader reader) throws SAXException {
        reader.startElement(getXMLName());
        for (Attribute attribute : getXMLAttributes())
            attribute.storeToXML(reader);
        for (TableColumn column : columns)
            column.getColumnHeader().storeToXML(reader);
        for (TableColumn column : columns)
            column.getColumnBody().storeToXML(reader);

        reader.endElement(getXMLName());
    }

    @Override
    public void setXMLAttribute(Attribute attribute, Object object) {
        if (object != null)
            if (attribute.equals(query))
                label.setText(getString("ReportAttribute.query") + ": "
                        + object.toString());
        super.setXMLAttribute(attribute, object);
    }

    @Override
    public String getTypeName() {
        return "Table";
    }

}

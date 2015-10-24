package com.ramussoft.report.editor.xml;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import org.xml.sax.SAXException;

import com.ramussoft.report.editor.xml.components.Table;
import com.ramussoft.report.editor.xml.components.TableColumn;
import com.ramussoft.report.editor.xml.components.XMLComponent;
import com.ramussoft.reportgef.Component;
import com.ramussoft.reportgef.Group;
import com.ramussoft.reportgef.gui.Diagram;
import com.ramussoft.reportgef.model.Bounds;
import com.ramussoft.reportgef.model.Dimension2DImpl;
import com.ramussoft.reportgef.model.QBounds;

public class XMLDiagram extends Diagram {

    /**
     *
     */
    private static final long serialVersionUID = -4634250097984820887L;

    private double width = 800;

    private double top = 20;

    public static final double LEFT = 20;

    private QBounds[] yBounds = new QBounds[]{};

    private XMLComponentFramefork framework = new XMLComponentFramefork();

    private ReportEditor editor;

    private AlphaComposite instance = AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER, 1);

    @Override
    public void paintGroup(Graphics2D g, Group selection) {
        super.paintGroup(g, selection);
        Bounds[] bounds = selection.getBounds();
        g.setComposite(instance);
        if (bounds.length > 0) {
            Component component = getComponent(bounds[0]);
            try {
                if (((XMLComponent) component).isY())
                    paintYShow(g, selection.getTranslate().getY(), bounds[0]);
                else
                    paintXShow(g, selection.getTranslate().getX(), bounds[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void paintXShow(Graphics2D g, double tX, Bounds bounds) {
        TableColumn tableColumn = (TableColumn) getComponent(bounds);
        tableColumn.getTable().paintPin(g, tX, tableColumn, this);
    }

    private void paintYShow(Graphics2D g, double tY, Bounds bounds) {
        double yPos = bounds.getTop() + tY;
        int cNumber;
        for (cNumber = 0; cNumber < this.yBounds.length; cNumber++) {
            if (this.yBounds[cNumber].equals(bounds))
                break;
        }
        int number = getYNumber(yPos);
        if (number >= 0) {
            if ((number != cNumber) && (number != cNumber + 1)) {
                paintYPin(g, number);
            }
        }
    }

    private void paintYPin(Graphics2D g, int number) {
        double y = top;
        for (int i = 0; i < number; i++) {
            y += yBounds[i].getSize().getHeight();
        }
        g.setColor(Color.black);

        double max = 6;

        g.setPaint(new GradientPaint(new Point2D.Double(LEFT - max, y - max),
                Color.green, new Point2D.Double(LEFT + max, y + max),
                Color.black));

        GeneralPath path1 = new GeneralPath(Path2D.WIND_EVEN_ODD, 4);
        GeneralPath path2 = new GeneralPath(Path2D.WIND_EVEN_ODD, 4);
        path1.moveTo(LEFT - 5, y);
        path1.lineTo(LEFT - 5 - max, y + max / 2);
        path1.lineTo(LEFT - 5 - max, y - max / 2);
        path1.lineTo(LEFT - 5, y);

        path2.moveTo(5 + LEFT + width, y);
        path2.lineTo(5 + LEFT + width + max, y + max / 2);
        path2.lineTo(5 + LEFT + width + max, y - max / 2);
        path2.lineTo(5 + LEFT + width, y);

        g.fill(path1);
        g.setPaint(new GradientPaint(new Point2D.Double(LEFT + width - max, y
                - max), Color.black, new Point2D.Double(LEFT + max + width, y
                + max), Color.green));
        g.fill(path2);
        g.setColor(Color.gray);
        g.draw(path1);
        g.draw(path2);
    }

    private int getYNumber(double yPos) {
        int i = 0;
        double y = top;
        if (yPos < 0)
            return 0;
        for (Bounds bounds : this.yBounds) {
            if (bounds instanceof QBounds) {
                QBounds qBounds = (QBounds) bounds;
                double h = qBounds.getSize().getHeight();
                y += h;
                if (y - h / 2d > yPos)
                    return i;
                i++;
            }
        }
        return i;
    }

    public QBounds createNewBounds(String componentName) {
        QBounds bounds = new QBounds();
        double y = top;
        for (QBounds bounds2 : this.yBounds) {
            y += bounds2.getSize().getHeight();
        }

        bounds.setLocation(new Point2D.Double(LEFT, y));
        bounds.setComponentType(componentName);

        bounds.setPosition(this.bounds.length);

        Component component = framework.createComponent(this, bounds);

        bounds.setBackground(component.getDefaultBackground());
        bounds.setFont(component.getDefaultFont());
        bounds.setFontColor(component.getDefaultFontColor());
        bounds.setForeground(component.getDefaultForeground());

        bounds.setSize(new Dimension2DImpl(component.getMinWidth(), component
                .getMinHeight()));

        add(bounds, component);

        return new QBounds();
    }

    public void add(Bounds bounds, Component component) {
        this.components = Arrays.copyOf(this.components,
                this.components.length + 1);
        this.bounds = Arrays.copyOf(this.bounds, this.bounds.length + 1);
        this.components[this.components.length - 1] = component;
        this.bounds[this.bounds.length - 1] = bounds;
        if (((XMLComponent) component).isY()) {
            this.yBounds = Arrays.copyOf(this.yBounds, this.yBounds.length + 1);
            this.yBounds[this.yBounds.length - 1] = (QBounds) bounds;
        }
    }

    public double getWidthForCompontns() {
        return width;
    }

    public void applyTransformForGroup(Group group) {
        QBounds bounds = (QBounds) group.getBounds()[0];
        XMLComponent component = (XMLComponent) getComponent(bounds);
        if (component.isY()) {
            double yPos = bounds.getTop() + group.getTranslate().getY();
            int cNumber;
            for (cNumber = 0; cNumber < this.yBounds.length; cNumber++) {
                if (this.yBounds[cNumber].equals(bounds))
                    break;
            }
            int number = getYNumber(yPos);
            if (number >= 0) {
                if ((number != cNumber) && (number != cNumber + 1)) {
                    List<QBounds> list = new ArrayList<QBounds>();
                    List<QBounds> seleted = new ArrayList<QBounds>();
                    for (Bounds b : group.getBounds()) {
                        XMLComponent component2 = (XMLComponent) getComponent(b);
                        if (component2.isY())
                            seleted.add((QBounds) b);
                    }
                    int newPos = -1;
                    int i = 0;
                    for (QBounds q : yBounds) {
                        if (seleted.indexOf(q) < 0) {
                            if (i == number)
                                newPos = list.size();
                            list.add(q);

                        }
                        i++;
                    }
                    if (newPos < 0)
                        newPos = list.size();
                    for (Bounds b : seleted) {
                        list.add(newPos, (QBounds) b);
                        newPos++;
                    }

                    this.yBounds = list.toArray(new QBounds[list.size()]);
                    double y = top;
                    for (QBounds b : yBounds) {
                        b.setLocation(new Point2D.Double(LEFT, y));
                        Component component2 = getComponent(b);
                        if (component2 instanceof Table)
                            ((Table) component2).applyComlumnsSize(b, this);
                        y += b.getSize().getHeight();
                    }

                }
            }
        } else {
            if (component instanceof TableColumn) {
                Table table = ((TableColumn) component).getTable();
                table.applyTransformForGroup(group, this);
            }
        }
        editor.postChanged();
    }

    public void createTableColumn() {
        Table table = getSelectedTable();
        if (table != null) {
            QBounds tableBound = (QBounds) getBounds(table);
            createNewBounds("TableColumn");
            TableColumn column = (TableColumn) components[components.length - 1];
            column.setTable(table);
            table.addColumn(tableBound, this, column);
        }
    }

    private Table getSelectedTable() {
        Group group = editor.getSelection();
        for (Bounds bounds : group.getBounds()) {
            Component component = getComponent(bounds);
            if (component instanceof Table)
                return (Table) component;
        }
        return null;
    }

    public void setEditor(ReportEditor editor) {
        this.editor = editor;
    }

    public ReportEditor getEditor() {
        return editor;
    }

    @Override
    public Dimension2D getSize() {
        double height = top * 2;
        for (int i = 0; i < components.length; i++) {
            XMLComponent component = (XMLComponent) components[i];
            if (component.isY()) {
                height += ((QBounds) bounds[i]).getSize().getHeight();
            }
        }
        final Dimension2DImpl size = new Dimension2DImpl(width + LEFT * 2,
                height);
        if (!size.equals(this.size)) {
            this.size = size;
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (editor != null) {
                        editor.setPreferredSize(new Dimension((int) size
                                .getWidth(), (int) size.getHeight()));
                        editor.revalidate();
                        editor.repaint();
                    }
                }
            });
        }
        return size;
    }

    public void removeSelectedComponents() {
        Bounds[] bounds = editor.getSelection().getBounds();
        List<Integer> list = new ArrayList<Integer>();
        for (Bounds bounds2 : bounds) {
            int index = getIndexOfBounds(bounds2);
            list.add(index);
            Component component = components[index];
            if (component instanceof Table) {
                for (TableColumn column : ((Table) component).getColumns()) {
                    list.add(getIndexOfComponent(column));
                }
            }
        }
        List<Bounds> bounds2 = new ArrayList<Bounds>();
        List<QBounds> yBounds = new ArrayList<QBounds>();
        List<Component> components = new ArrayList<Component>();
        for (int i = 0; i < this.components.length; i++) {
            if (list.indexOf(i) < 0) {
                bounds2.add(this.bounds[i]);
                components.add(this.components[i]);

            }
        }
        this.bounds = bounds2.toArray(new Bounds[bounds2.size()]);
        this.components = components.toArray(new Component[components.size()]);

        for (QBounds qBounds : this.yBounds) {
            if (bounds2.indexOf(qBounds) >= 0) {
                yBounds.add(qBounds);
            }
        }

        this.yBounds = yBounds.toArray(new QBounds[yBounds.size()]);
        editor.getSelection().setBounds(new Bounds[]{});

        double y = top;

        for (int i = 0; i < this.components.length; i++) {
            if (((XMLComponent) this.components[i]).isY()) {
                QBounds e = (QBounds) this.bounds[i];
                e.setLocation(new Point2D.Double(e.getLocation().getX(), y));
                yBounds.add(e);
                y += e.getSize().getHeight();
                if (this.components[i] instanceof Table) {
                    Table table = (Table) this.components[i];
                    table.removeComlumntIfNeed(components, this);
                }
            }
        }
    }

    public int getIndexOfBounds(Bounds b) {
        for (int i = 0; i < bounds.length; i++)
            if (b.equals(bounds[i]))
                return i;
        return -1;
    }

    public void storeToXML(ReportSaveXMLReader reportSaveXMLReader)
            throws SAXException {
        for (QBounds bounds : yBounds) {
            XMLComponent component = (XMLComponent) getComponent(bounds);
            component.storeToXML(reportSaveXMLReader);
        }
    }

    public void loadFromYComponents(List<XMLComponent> yComponents) {
        List<Component> components = new ArrayList<Component>();
        List<Bounds> allBounds = new ArrayList<Bounds>();
        List<QBounds> yBounds = new ArrayList<QBounds>();

        double y = top;

        for (int i = 0; i < yComponents.size(); i++) {

            Component component = yComponents.get(i);
            ((XMLComponent) component).setWidth(width);

            QBounds bounds = new QBounds();

            bounds.setLocation(new Point2D.Double(LEFT, y));
            bounds.setComponentType(((XMLComponent) component).getTypeName());

            bounds.setPosition(allBounds.size());

            bounds.setBackground(component.getDefaultBackground());
            bounds.setFont(component.getDefaultFont());
            bounds.setFontColor(component.getDefaultFontColor());
            bounds.setForeground(component.getDefaultForeground());

            bounds.setSize(new Dimension2DImpl(component.getMinWidth(),
                    component.getMinHeight()));
            components.add(component);
            yBounds.add(bounds);

            y += bounds.getSize().getHeight();
            allBounds.add(bounds);

            if (component instanceof Table) {
                Table table = (Table) component;
                TableColumn[] columns = table.getColumns();
                QBounds[] bounds2 = new QBounds[columns.length];
                for (int j = 0; j < columns.length; j++) {
                    TableColumn column = columns[j];

                    column.setWidth(width);

                    QBounds cBounds = new QBounds();

                    bounds2[j] = cBounds;

                    cBounds.setComponentType(((XMLComponent) column)
                            .getTypeName());

                    cBounds.setPosition(allBounds.size());

                    cBounds.setBackground(column.getDefaultBackground());
                    cBounds.setFont(column.getDefaultFont());
                    cBounds.setFontColor(column.getDefaultFontColor());
                    cBounds.setForeground(column.getDefaultForeground());

                    cBounds.setSize(new Dimension2DImpl(column.getMinWidth(),
                            column.getMinHeight()));

                    components.add(column);
                    allBounds.add(cBounds);
                }
                table.applyComlumnsSize(bounds, bounds2);
            }

        }

        this.components = components.toArray(new Component[components.size()]);
        this.bounds = new Bounds[allBounds.size()];

        for (int i = 0; i < this.bounds.length; i++)
            this.bounds[i] = allBounds.get(i);

        this.yBounds = new QBounds[yBounds.size()];
        for (int i = 0; i < this.yBounds.length; i++)
            this.yBounds[i] = yBounds.get(i);
    }

    public void clear() {
        this.components = new Component[]{};
        this.bounds = new Bounds[]{};
        this.yBounds = new QBounds[]{};
    }

}

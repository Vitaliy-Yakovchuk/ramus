package com.ramussoft.idef0.attribute;

import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.qualifier.table.MetadataGetter;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.frames.components.LineStyleChooser;
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.PaintSector.Pin;

public class LineStyleAttributePlugin extends AbstractAttributePlugin {
    private DefaultTableCellRenderer strokeRenderer = new DefaultTableCellRenderer() {

        private Stroke stroke;

        protected void setValue(Object value) {
            if (value instanceof Stroke) {
                stroke = (Stroke) value;
            } else
                stroke = null;
        }

        public void paint(Graphics gr) {
            super.paint(gr);
            if (stroke == null)
                return;
            final Graphics2D g = (Graphics2D) gr;

            g.setColor(getForeground());
            g.setStroke(stroke);
            g.draw(new Line2D.Double(0, (double) getHeight() / 2, getWidth(),
                    (double) getHeight() / 2));
        }

        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            return super.getTableCellRendererComponent(table,
                    ((PaintSector.Pin) ((MetadataGetter) table).getMetadata())
                            .getSector().getStroke(), isSelected, hasFocus,
                    row, column);
        }

        ;
    };

    private DefaultListCellRenderer comboBoxRenderer = new DefaultListCellRenderer() {

        private Stroke stroke;

        @Override
        public void paint(final Graphics gr) {
            final Graphics2D g = (Graphics2D) gr;
            super.paint(gr);
            if (stroke == null)
                return;

            g.setColor(getForeground());
            g.setStroke(stroke);
            g.draw(new Line2D.Double(0, (double) getHeight() / 2, getWidth(),
                    (double) getHeight() / 2));

        }

        @Override
        public Component getListCellRendererComponent(final JList list,
                                                      final Object value, final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {
            if (value instanceof Stroke) {
                stroke = (Stroke) value;
                return super.getListCellRendererComponent(list, " ", index,
                        isSelected, cellHasFocus);
            } else
                stroke = null;
            return super.getListCellRendererComponent(list, value, index,
                    isSelected, cellHasFocus);
        }
    };

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("IDEF0", "LineStyle");
    }

    @Override
    public TableCellEditor getTableCellEditor(final Engine engine,
                                              final AccessRules rules, final Attribute attribute) {
        final JComboBox box = new JComboBox();
        box.setRenderer(comboBoxRenderer);

        for (Stroke stroke : LineStyleChooser.getStrokes()) {
            box.addItem(stroke);
        }

        return new DefaultCellEditor(box) {
            private Pin pin;

            @Override
            public boolean stopCellEditing() {
                if (box.getSelectedItem() instanceof Stroke) {
                    ((Journaled) engine).startUserTransaction();
                    apply((BasicStroke) box.getSelectedItem(), pin);
                    return super.stopCellEditing();
                }
                return false;
            }

            @Override
            public Component getTableCellEditorComponent(JTable table,
                                                         Object value, boolean isSelected, int row, int column) {
                pin = (Pin) ((MetadataGetter) table).getMetadata();
                return super.getTableCellEditorComponent(table, value,
                        isSelected, row, column);
            }
        };
    }

    @Override
    public TableCellRenderer getTableCellRenderer(Engine engine,
                                                  AccessRules rules, Attribute attribute) {
        return strokeRenderer;
    }

    @Override
    public AttributeEditor getAttributeEditor(final Engine engine,
                                              final AccessRules rules, final Element element,
                                              final Attribute attribute, AttributeEditor old) {
        if (old != null)
            old.close();
        return new AbstractAttributeEditor() {

            private PaintSector.Pin pin;

            private LineStyleChooser component;

            private Stroke stroke;

            {
                component = new LineStyleChooser();
            }

            @Override
            public Object setValue(Object value) {
                this.pin = (PaintSector.Pin) value;
                stroke = pin.getSector().getStroke();
                component.setStroke(stroke);
                return value;
            }

            @Override
            public Object getValue() {
                return pin;
            }

            @Override
            public void apply(Engine engine, Element element,
                              Attribute attribute, Object value) {
                LineStyleAttributePlugin.this.apply(component.getStroke(), pin);
            }

            @Override
            public JComponent getComponent() {
                return component;
            }

            @Override
            public boolean isSaveAnyway() {
                return !stroke.equals(component.getStroke());
            }
        };
    }

    public void apply(Stroke stroke, PaintSector.Pin pin) {
        PaintSector sector = pin.getSector();
        sector.setStroke(stroke);
        sector.copyVisual(Sector.VISUAL_COPY_ADDED);
        pin.getSector().getMovingArea().getRefactor().setUndoPoint();
    }

    @Override
    public String getName() {
        return "IDEF0";
    }

    @Override
    public boolean isCellEditable() {
        return false;
    }
}

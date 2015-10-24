package com.ramussoft.idef0.attribute;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JColorChooser;
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
import com.ramussoft.pb.idef.elements.PaintSector;
import com.ramussoft.pb.idef.elements.PaintSector.Pin;

public class SectorColorAttributePlugin extends AbstractAttributePlugin {
    private DefaultTableCellRenderer colorRenderer = new DefaultTableCellRenderer() {

        private Color color;

        protected void setValue(Object value) {
            if (value instanceof Color) {
                color = (Color) value;
            } else
                color = null;
        }

        public void paint(Graphics g) {
            if (color == null) {
                super.paint(g);
            } else {
                g.setColor(color);
                // g.fillRect(0, 0, getWidth(), getHeight());
                setBackground(color);
                setForeground(color);
                super.paint(g);
            }
        }

        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            return super.getTableCellRendererComponent(table,
                    ((PaintSector.Pin) ((MetadataGetter) table).getMetadata())
                            .getSector().getColor(), isSelected, hasFocus, row,
                    column);
        }

        ;
    };

    private DefaultListCellRenderer comboBoxRenderer = new DefaultListCellRenderer() {

        private Color color;

        @Override
        public void paint(final Graphics gr) {
            final Graphics2D g = (Graphics2D) gr;
            if (color != null) {
                g.setColor(color);
                // g.fill(new Rectangle2D.Double(0, 0, getWidth(),
                // getHeight()));
                setBackground(color);
                setForeground(color);
                super.paint(g);
            } else
                super.paint(gr);
        }

        @Override
        public Component getListCellRendererComponent(final JList list,
                                                      final Object value, final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {
            if (value instanceof Color) {
                color = (Color) value;
                return super
                        .getListCellRendererComponent(list,
                                "####################", index, isSelected,
                                cellHasFocus);
            } else
                color = null;
            return super.getListCellRendererComponent(list, value, index,
                    isSelected, cellHasFocus);
        }
    };

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("IDEF0", "SectorColor");
    }

    @Override
    public TableCellEditor getTableCellEditor(final Engine engine,
                                              final AccessRules rules, final Attribute attribute) {
        final JComboBox box = new JComboBox();
        box.setRenderer(comboBoxRenderer);

        box.addItem(Color.white);
        box.addItem(Color.green);
        box.addItem(Color.blue);
        box.addItem(Color.red);
        box.addItem(Color.yellow);
        box.addItem(Color.cyan);
        box.addItem(Color.magenta);
        box.addItem(Color.orange);
        box.addItem(Color.pink);
        box.addItem(Color.lightGray);
        box.addItem(Color.gray);
        box.addItem(Color.darkGray);
        box.addItem(Color.black);

        return new DefaultCellEditor(box) {
            private Pin pin;

            @Override
            public boolean stopCellEditing() {
                if (box.getSelectedItem() instanceof Color) {
                    ((Journaled) engine).startUserTransaction();
                    apply((Color) box.getSelectedItem(), pin);
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
        return colorRenderer;
    }

    @Override
    public AttributeEditor getAttributeEditor(final Engine engine,
                                              final AccessRules rules, final Element element,
                                              final Attribute attribute, AttributeEditor old) {
        if (old != null)
            old.close();
        return new AbstractAttributeEditor() {

            private PaintSector.Pin pin;

            private JColorChooser component;

            private Color color;

            {
                component = new JColorChooser();
            }

            @Override
            public Object setValue(Object value) {
                this.pin = (PaintSector.Pin) value;
                color = pin.getSector().getColor();
                component.setColor(color);
                return value;
            }

            @Override
            public Object getValue() {
                return pin;
            }

            @Override
            public void apply(Engine engine, Element element,
                              Attribute attribute, Object value) {
                SectorColorAttributePlugin.this
                        .apply(component.getColor(), pin);
            }

            @Override
            public JComponent getComponent() {
                return component;
            }

            @Override
            public boolean isSaveAnyway() {
                return !color.equals(component.getColor());
            }
        };
    }

    public void apply(Color color, PaintSector.Pin pin) {
        PaintSector sector = pin.getSector();
        sector.setColor(color);
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

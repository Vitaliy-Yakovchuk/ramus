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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.dsoft.utils.Options;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;

public class ColorAttributePlugin extends AbstractAttributePlugin {

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
        return new AttributeType("IDEF0", "Color");
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
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
            @Override
            public boolean stopCellEditing() {
                if (box.getSelectedItem() instanceof Color)
                    return super.stopCellEditing();
                return false;
            }
        };
    }

    @Override
    public AttributeEditor getAttributeEditor(Engine engine, AccessRules rules,
                                              Element element, Attribute attribute, final String prefix,
                                              AttributeEditor old) {
        if (old != null)
            old.close();
        return new AbstractAttributeEditor() {

            private JColorChooser chooser = new JColorChooser();

            @Override
            public Object setValue(Object value) {
                if (value != null)
                    chooser.setColor((Color) value);
                else
                    chooser.setColor(Color.black);
                return chooser.getColor();
            }

            @Override
            public Object getValue() {
                return chooser.getColor();
            }

            @Override
            public JComponent getComponent() {
                Options.loadOptions(prefix + "ColorEditor", chooser);
                return chooser;
            }

            @Override
            public void close() {
                super.close();
                Options.saveOptions(prefix + "ColorEditor", chooser);
            }
        };
    }

    @Override
    public TableCellRenderer getTableCellRenderer(Engine engine,
                                                  AccessRules rules, Attribute attribute) {
        return colorRenderer;
    }

    @Override
    public String getName() {
        return "IDEF0";
    }

}

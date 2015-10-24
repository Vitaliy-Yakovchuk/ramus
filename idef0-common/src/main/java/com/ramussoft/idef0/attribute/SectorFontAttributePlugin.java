package com.ramussoft.idef0.attribute;

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.MetadataGetter;
import com.ramussoft.gui.qualifier.table.TableNode;
import com.ramussoft.gui.qualifier.table.TabledAttributePlugin;
import com.ramussoft.gui.qualifier.table.ValueGetter;
import com.ramussoft.gui.qualifier.table.event.Closeable;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.frames.components.JFontChooser;
import com.ramussoft.pb.idef.elements.PaintSector;

public class SectorFontAttributePlugin extends AbstractAttributePlugin
        implements TabledAttributePlugin {

    private DefaultTableCellRenderer fontRenderer = new DefaultTableCellRenderer() {

        protected void setValue(Object value) {
            if (value != null) {
                Font font = (Font) value;
                String text = FontAttributePlugin.fontToText(font);
                this.setText(text);
                this.setFont(font);
            }
        }

        ;

        public java.awt.Component getTableCellRendererComponent(
                javax.swing.JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            return super.getTableCellRendererComponent(table,
                    ((PaintSector.Pin) ((MetadataGetter) table).getMetadata())
                            .getSector().getFont(), isSelected, hasFocus, row,
                    column);
        }

        ;
    };

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("IDEF0", "SectorFont");
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        return null;
    }

    @Override
    public TableCellRenderer getTableCellRenderer(Engine engine,
                                                  AccessRules rules, Attribute attribute) {
        return fontRenderer;
    }

    @Override
    public AttributeEditor getAttributeEditor(final Engine engine,
                                              final AccessRules rules, final Element element,
                                              final Attribute attribute, AttributeEditor old) {
        if (old != null)
            old.close();
        return new AbstractAttributeEditor() {

            private PaintSector.Pin pin;

            private JFontChooser component;

            private Font font;

            {
                component = new JFontChooser();
                ResourceLoader.setJComponentsText(component);
            }

            @Override
            public Object setValue(Object value) {
                this.pin = (PaintSector.Pin) value;
                font = pin.getSector().getFont();
                component.setSelFont(font);
                return value;
            }

            @Override
            public Object getValue() {
                return pin;
            }

            @Override
            public JComponent getComponent() {
                return component;
            }

            @Override
            public void apply(Engine engine, Element element,
                              Attribute attribute, Object value) {
                PaintSector sector = pin.getSector();
                sector.setFont(component.getSelFont());
                sector.copyVisual(Sector.VISUAL_COPY_ADDED);
                pin.getSector().getMovingArea().getRefactor().setUndoPoint();
            }

            @Override
            public boolean isSaveAnyway() {
                return !font.equals(component.getSelFont());
            }
        };
    }

    @Override
    public String getName() {
        return "IDEF0";
    }

    @Override
    public ValueGetter getValueGetter(Attribute attribute, Engine engine,
                                      GUIFramework framework, Closeable model) {
        return new ValueGetter() {

            @Override
            public Object getValue(TableNode node, int index) {
                return "[" + ResourceLoader.getString("font") + "]";
            }
        };
    }
}

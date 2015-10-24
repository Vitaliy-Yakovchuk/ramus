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
import com.ramussoft.gui.qualifier.table.DialogedTableCellEditor;
import com.ramussoft.pb.frames.components.JFontChooser;

public class FontAttributePlugin extends AbstractAttributePlugin {

    private DefaultTableCellRenderer fontRenderer = new DefaultTableCellRenderer() {

        protected void setValue(Object value) {
            if (value != null) {
                Font font = (Font) value;
                String text = fontToText(font);
                this.setText(text);
                this.setFont(font);
            }
        }

        ;
    };

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("IDEF0", "Font");
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        DialogedTableCellEditor cellEditor = new DialogedTableCellEditor(
                engine, rules, attribute, this, framework) {
            @Override
            protected void setValue(Object value) {
                super.setValue("[" + ResourceLoader.getString("font") + "]");
            }
        };
        return cellEditor;
    }

    @Override
    public AttributeEditor getAttributeEditor(Engine engine, AccessRules rules,
                                              Element element, Attribute attribute, AttributeEditor old) {

        if (old != null)
            old.close();

        return new AbstractAttributeEditor() {

            private JFontChooser chooser = new JFontChooser();

            @Override
            public Object setValue(Object value) {
                if (value != null)
                    chooser.setSelFont((Font) value);
                return chooser.getSelFont();
            }

            @Override
            public Object getValue() {
                return chooser.getSelFont();
            }

            @Override
            public JComponent getComponent() {
                return chooser;
            }
        };
    }

    @Override
    public String getName() {
        return "IDEF0";
    }

    @Override
    public TableCellRenderer getTableCellRenderer(Engine engine,
                                                  AccessRules rules, Attribute attribute) {
        return fontRenderer;
    }

    static String fontToText(Font font) {
        String text = font.getName() + " " + font.getSize();
        if ((font.getStyle() & Font.BOLD) == Font.BOLD) {
            if ((font.getStyle() & Font.ITALIC) == Font.ITALIC) {
                text += " " + ResourceLoader.getString("bold_italic");
            } else {
                text += " " + ResourceLoader.getString("bold");
            }
        } else if ((font.getStyle() & Font.ITALIC) == Font.ITALIC) {
            text += " " + ResourceLoader.getString("italic");
        }
        return text;
    }

    @Override
    public boolean isCellEditable() {
        return false;
    }

}

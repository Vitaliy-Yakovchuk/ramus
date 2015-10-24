package com.ramussoft.gui.attribute;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableCellEditor;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Engine;
import com.ramussoft.core.attribute.simple.VariantPropertyPersistent;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributePreferenciesEditor;

public class VariantAttributePlugin extends AbstractAttributePlugin {

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Core", "Variant", true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TableCellEditor getTableCellEditor(final Engine engine,
                                              AccessRules rules, final Attribute attribute) {
        final JComboBox box = new JComboBox();

        final List<VariantPropertyPersistent> list = (List<VariantPropertyPersistent>) engine
                .getAttribute(null, attribute);

        box.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                List<VariantPropertyPersistent> newList = (List<VariantPropertyPersistent>) engine
                        .getAttribute(null, attribute);

                for (VariantPropertyPersistent p : newList) {
                    boolean add = true;
                    for (VariantPropertyPersistent p1 : list)
                        if (p1.getValue().equals(p.getValue()))
                            add = false;
                    if (add) {
                        box.addItem(p.getValue());
                        list.add(p);
                    }
                }

            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        box.setName("Table.editor");
        box.addItem(null);

        for (VariantPropertyPersistent p : list) {
            box.addItem(p.getValue());
        }
        box.setEditable(true);
        return new DefaultCellEditor(box) {
            /**
             *
             */
            private static final long serialVersionUID = 7436784278964767871L;

            @Override
            public Object getCellEditorValue() {
                Object object = super.getCellEditorValue();
                if ("".equals(object))
                    return null;
                return object;
            }
        };
    }

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    public AttributePreferenciesEditor getAttributePreferenciesEditor() {
        return new AttributePreferenciesEditor() {

            private AttributeVariantPanel panel = new AttributeVariantPanel();

            @Override
            public void apply(Attribute attribute, Engine engine,
                              AccessRules accessRules) {
                engine.setAttribute(null, attribute, panel.getData());
            }

            @Override
            public boolean canApply() {
                return true;
            }

            @SuppressWarnings("unchecked")
            @Override
            public JComponent createComponent(Attribute attribute,
                                              Engine engine, AccessRules accessRules) {
                if (attribute != null)
                    panel.load((List<VariantPropertyPersistent>) engine
                            .getAttribute(null, attribute));
                else
                    panel.load(new ArrayList<VariantPropertyPersistent>(0));
                return panel;
            }

        };
    }

}

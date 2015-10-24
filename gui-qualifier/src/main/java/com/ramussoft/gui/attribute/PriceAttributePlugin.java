package com.ramussoft.gui.attribute;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.core.attribute.simple.Price;
import com.ramussoft.core.attribute.simple.PricePersistent;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;

public class PriceAttributePlugin extends AbstractAttributePlugin {

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Core", "Price", true);
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        return null;
    }

    @Override
    public AttributeEditor getAttributeEditor(final Engine engine,
                                              AccessRules rules, final Element element,
                                              final Attribute attribute, AttributeEditor oldAttributeEditor) {
        if (isCellEditable()) {
            if (oldAttributeEditor != null)
                oldAttributeEditor.close();
            return new AbstractAttributeEditor() {

                private PriceEditComponent component = new PriceEditComponent();

                @Override
                public Object setValue(Object value) {
                    component.setPrice((Price) value);
                    return value;
                }

                @Override
                public Object getValue() {
                    Price price = component.getPrice();
                    if (price.getData().length == 0)
                        return null;
                    return price;
                }

                @Override
                public JComponent getComponent() {
                    return component;
                }

            };
        }
        return null;
    }

    @Override
    public TableCellRenderer getTableCellRenderer(Engine engine,
                                                  AccessRules rules, Attribute attribute) {
        return new DefaultTableCellRenderer() {
            /**
             *
             */
            private static final long serialVersionUID = -7922052040779840252L;

            {
                setHorizontalAlignment(SwingConstants.RIGHT);
            }

            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value, boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component component = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                if (value != null)
                    ((JLabel) component).setText(format(value));
                return component;
            }
        };
    }

    protected String format(Object value) {
        Price priceObeject = (Price) value;
        if (priceObeject == null)
            return "";

        Object res = null;

        PricePersistent[] ps = priceObeject.getData();

        for (PricePersistent price : ps) {
            if (price.getValue() != null)
                res = price.getValue();
        }
        if (res == null)
            return "";
        return CurrencyAttributePlugin.currentcyFormat.format(res);
    }

    @Override
    public String getName() {
        return "Core";
    }

}

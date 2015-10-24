package com.ramussoft.gui.attribute;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.localefix.DecimalFormatWithFix;

public class DoubleCellRendererWithFix extends DefaultTableCellRenderer {

    /**
     *
     */
    private static final long serialVersionUID = 4243400887802176769L;

    protected static final DecimalFormatWithFix format = new DecimalFormatWithFix();

    {
        setHorizontalAlignment(SwingConstants.RIGHT);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {

        Component component = super.getTableCellRendererComponent(table, value,
                isSelected, hasFocus, row, column);
        if (value != null) {
            if (value instanceof Number) {
                double v = ((Number) value).doubleValue();
                if ((Double.isNaN(v)) || (Double.isInfinite(v))) {
                    ((JLabel) component).setText(GlobalResourcesManager
                            .getString("WrongData"));
                    return component;
                }
            }
            ((JLabel) component).setText(format.format(value));
        }
        return component;
    }

    public static DecimalFormatWithFix getFormat() {
        return format;
    }
}

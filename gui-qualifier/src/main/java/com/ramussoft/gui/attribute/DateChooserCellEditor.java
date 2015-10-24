package com.ramussoft.gui.attribute;

import java.sql.Timestamp;
import java.util.Date;

import static com.ramussoft.gui.attribute.DateAttributePlugin.DATE_INSTANCE;

public class DateChooserCellEditor extends
        org.jdesktop.swingx.table.DatePickerCellEditor {

    public DateChooserCellEditor() {
        super(DATE_INSTANCE);
    }

    /**
     *
     */
    private static final long serialVersionUID = -5580011474134773805L;

    @Override
    public Date getCellEditorValue() {
        Date value = super.getCellEditorValue();
        if (value == null)
            return null;
        if (value instanceof Timestamp)
            return value;
        return new Timestamp(value.getTime());
    }

};

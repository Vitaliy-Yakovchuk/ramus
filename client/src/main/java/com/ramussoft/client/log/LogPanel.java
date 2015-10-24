package com.ramussoft.client.log;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.ramussoft.common.logger.Event;

import java.awt.BorderLayout;
import java.awt.Component;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class LogPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = -2233991051912109852L;

    private JTable table;

    /**
     * Create the panel.
     */
    public LogPanel(List<Event> events) {
        setLayout(new BorderLayout(0, 0));

        table = new JTable(createEventModel(events)) {

            /**
             *
             */
            private static final long serialVersionUID = 3597930159334465519L;

            private TableCellRenderer dateR = new DefaultTableCellRenderer() {
                /**
                 *
                 */
                private static final long serialVersionUID = -5671695793360653643L;

                private DateFormat dateFormat = DateFormat
                        .getDateTimeInstance();

                @Override
                public Component getTableCellRendererComponent(JTable table,
                                                               Object value, boolean isSelected, boolean hasFocus,
                                                               int row, int column) {
                    value = dateFormat.format(value);
                    return super.getTableCellRendererComponent(table, value,
                            isSelected, hasFocus, row, column);
                }
            };

            @Override
            public TableCellRenderer getDefaultRenderer(Class<?> columnClass) {
                if (Date.class.isAssignableFrom(columnClass)) {
                    return dateR;
                }
                return super.getDefaultRenderer(columnClass);
            }
        };
        add(new JScrollPane(table), BorderLayout.CENTER);

    }

    protected TableModel createEventModel(List<Event> events) {
        return new EventTableModel(events);
    }

}

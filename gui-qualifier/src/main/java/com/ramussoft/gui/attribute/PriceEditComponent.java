package com.ramussoft.gui.attribute;

import javax.swing.JPanel;

import com.ramussoft.core.attribute.simple.Price;
import com.ramussoft.core.attribute.simple.PricePersistent;
import com.ramussoft.gui.attribute.DoubleAttributePlugin.DoubleCellEditor;
import com.ramussoft.gui.common.GlobalResourcesManager;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class PriceEditComponent extends JPanel {
    /**
     *
     */
    private static final long serialVersionUID = -7358138459907691246L;

    private JTable table;

    private Price price = new Price();

    private DateChooserCellEditor dateChooserCellEditor = new DateChooserCellEditor();

    private DoubleCellEditor doubleCellEditor = new DoubleCellEditor();

    private DefaultTableCellRenderer dateTableCellRenderer = new DefaultTableCellRenderer() {

        /**
         *
         */
        private static final long serialVersionUID = -2741486218125253416L;

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            Component component = super.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);
            if (value != null) {
                if (component instanceof JLabel)
                    ((JLabel) component)
                            .setText(DateAttributePlugin.DATE_INSTANCE
                                    .format(value));
                else {
                    TableCellRenderer renderer = table
                            .getDefaultRenderer(Date.class);
                    return renderer.getTableCellRendererComponent(table, value,
                            isSelected, hasFocus, row, column);
                }
            }
            return component;
        }
    };

    DefaultTableCellRenderer priceTableCellRenderer = new DefaultTableCellRenderer() {
        /**
         *
         */
        private static final long serialVersionUID = -7922152040779823252L;

        {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            Component component = super.getTableCellRendererComponent(table,
                    value, isSelected, hasFocus, row, column);
            if (value != null)
                ((JLabel) component)
                        .setText(CurrencyAttributePlugin.currentcyFormat
                                .format(value));
            return component;
        }
    };

    public PriceEditComponent() {
        price.setData(new PricePersistent[]{});
        setLayout(new BorderLayout(0, 0));

        JToolBar toolBar = new JToolBar();
        add(toolBar, BorderLayout.NORTH);

        JButton btnNewButton = new JButton(
                GlobalResourcesManager.getString("Record.Add"));
        btnNewButton.setIcon(new ImageIcon(PriceEditComponent.class
                .getResource("/com/ramussoft/gui/table/add.png")));
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addRow();
            }
        });
        toolBar.add(btnNewButton);

        JButton btnNewButton_1 = new JButton(
                GlobalResourcesManager.getString("Record.Remove"));
        btnNewButton_1.setIcon(new ImageIcon(PriceEditComponent.class
                .getResource("/com/ramussoft/gui/table/delete.png")));
        btnNewButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeRow();
            }
        });
        toolBar.add(btnNewButton_1);

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, BorderLayout.CENTER);

        table = new JTable(new PriceModel()) {
            /**
             *
             */
            private static final long serialVersionUID = 6863166192090726557L;

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (column == 0)
                    return dateTableCellRenderer;
                return priceTableCellRenderer;
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                if (column == 0)
                    return dateChooserCellEditor;
                return doubleCellEditor;
            }
        };
        scrollPane.setViewportView(table);
    }

    protected void addRow() {
        PricePersistent pp = new PricePersistent();
        PricePersistent[] pps = Arrays.copyOf(price.getData(),
                price.getData().length + 1);
        pps[pps.length - 1] = pp;
        price.setData(pps);
        PriceModel model = (PriceModel) table.getModel();
        model.fireTableRowsInserted(pps.length - 1, pps.length - 1);
    }

    protected void removeRow() {
        int[] rs = table.getSelectedRows();
        PricePersistent[] pps = new PricePersistent[price.getData().length
                - rs.length];
        int k = 0;
        int j = 0;
        for (int i = 0; i < price.getData().length; i++) {
            if (rs.length > j)
                if (rs[j] == i) {
                    j++;
                    continue;
                }
            pps[k] = price.getData()[i];
            k++;

        }
        price.setData(pps);
        PriceModel model = (PriceModel) table.getModel();
        model.fireTableDataChanged();
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        if (price == null) {
            this.price = new Price();
            this.price.setData(new PricePersistent[]{});
        } else
            this.price = price;
        PriceModel model = (PriceModel) table.getModel();
        model.fireTableDataChanged();
    }

    private final class PriceModel extends AbstractTableModel {
        /**
         *
         */
        private static final long serialVersionUID = -7358003536207942399L;

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            PricePersistent record = price.getData()[rowIndex];
            if (columnIndex == 0)
                return record.getStartDate();
            return record.getValue();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            PricePersistent record = price.getData()[rowIndex];
            if (columnIndex == 0) {
                if (aValue != null && !(aValue instanceof Timestamp))
                    aValue = new Timestamp(((Date) aValue).getTime());
                record.setStartDate((Timestamp) aValue);
            } else
                record.setValue((Double) aValue);
        }

        @Override
        public int getRowCount() {
            if (price == null)
                return 0;
            return price.getData().length;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0)
                return GlobalResourcesManager
                        .getString("AttributeType.Core.Date");
            return GlobalResourcesManager.getString("AttributeType.Core.Price");
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
    }

}

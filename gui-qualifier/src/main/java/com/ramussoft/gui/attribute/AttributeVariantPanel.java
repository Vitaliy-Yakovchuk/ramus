package com.ramussoft.gui.attribute;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;

import com.ramussoft.core.attribute.simple.VariantPropertyPersistent;
import com.ramussoft.gui.common.GlobalResourcesManager;

public class AttributeVariantPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 5034231456454357002L;

    private final JButton add = new JButton(new ImageIcon(getClass()
            .getResource("/com/ramussoft/gui/table/add.png")));

    private final JButton remove = new JButton(new ImageIcon(getClass()
            .getResource("/com/ramussoft/gui/table/delete.png")));

    private final JButton moveUp = new JButton(new ImageIcon(getClass()
            .getResource("/com/ramussoft/gui/table/move-up.png")));

    private final JButton moveDown = new JButton(new ImageIcon(getClass()
            .getResource("/com/ramussoft/gui/table/move-down.png")));

    private final JPanel main = new JPanel();

    private final JPanel right = new JPanel();

    private final JPanel buttonPanel = new JPanel();

    private final JScrollPane pane = new JScrollPane();

    private final JTable table = new JTable() {
        /**
         *
         */
        private static final long serialVersionUID = -6534174253515581830L;

        @Override
        public void changeSelection(int rowIndex, int columnIndex,
                                    boolean toggle, boolean extend) {
            super.changeSelection(rowIndex, columnIndex, toggle, extend);
            remove.setEnabled(table.getSelectedRowCount() > 0);
        }
    };

    private List<VariantPropertyPersistent> data = new ArrayList<VariantPropertyPersistent>();

    private final AbstractTableModel model = new AbstractTableModel() {

        /**
         *
         */
        private static final long serialVersionUID = -7023550573046031437L;

        public int getColumnCount() {
            return 1;
        }

        public int getRowCount() {
            return data.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return data.get(rowIndex).getValue();
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            data.get(rowIndex).setValue(aValue.toString());
        }

        @Override
        public String getColumnName(int column) {
            return GlobalResourcesManager.getString("AttributeVariant");
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
    };

    public AttributeVariantPanel() {
        super(new BorderLayout());
        final GridLayout layout = new GridLayout(4, 2, 0, 5);
        buttonPanel.setLayout(layout);
        add.setToolTipText(GlobalResourcesManager
                .getString("Action.AddVariant"));
        remove.setToolTipText(GlobalResourcesManager
                .getString("Action.RemoveVariant"));

        moveUp.setToolTipText(GlobalResourcesManager
                .getString("Action.MoveVariantUp"));
        moveUp.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                long[] attrs = getSelectedIds();
                moveUp(table.getSelectedRows());
                setSelected(attrs);
            }

        });
        moveDown.setToolTipText(GlobalResourcesManager
                .getString("Action.MoveVariantDown"));
        moveDown.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                long[] attrs = getSelectedIds();
                moveDown(table.getSelectedRows());
                setSelected(attrs);
            }

        });

        buttonPanel.add(add);
        buttonPanel.add(remove);
        buttonPanel.add(moveUp);
        buttonPanel.add(moveDown);
        right.setLayout(new FlowLayout());
        right.add(buttonPanel);
        main.setLayout(new BorderLayout());
        main.add(right, BorderLayout.EAST);

        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "startEditing");

        table.setModel(model);

        pane.setViewportView(table);
        main.add(pane, BorderLayout.CENTER);
        this.add(main, BorderLayout.CENTER);
        add.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                add();
            }

        });

        remove.addActionListener(new ActionListener() {

            public void actionPerformed(final ActionEvent e) {
                remove();
            }

        });
        setPreferredSize(new Dimension(200, 200));
    }

    private void moveDown(final int[] selectedRows) {
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            final int r = selectedRows[i];
            if (r < data.size() - 1) {
                VariantPropertyPersistent attr = data.get(r);
                data.remove(r);
                data.add(r + 1, attr);
            }
        }
        model.fireTableDataChanged();
    }

    private void moveUp(final int[] selectedRows) {
        for (final int r : selectedRows) {
            if (r > 0) {
                VariantPropertyPersistent attr = data.get(r);
                data.remove(r);
                data.add(r - 1, attr);
            }
        }
        model.fireTableDataChanged();
    }

    protected void setSelected(final long[] attrs) {
        for (int i = 0; i < data.size(); i++) {
            for (long id : attrs) {
                if (data.get(i).getVariantId() == id) {
                    table.getSelectionModel().addSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    protected long[] getSelectedIds() {
        int[] is = table.getSelectedRows();
        long[] res = new long[is.length];
        for (int i = 0; i < is.length; i++)
            res[i] = data.get(is[i]).getVariantId();
        return res;
    }

    protected void remove() {
        if (JOptionPane.showConfirmDialog(this, GlobalResourcesManager
                        .getString("DeleteActiveElementsDialog.Warning"),
                GlobalResourcesManager.getString("ConfirmMessage.Title"),
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;

        final int[] is = table.getSelectedRows();
        final Vector<VariantPropertyPersistent> rem = new Vector<VariantPropertyPersistent>();
        for (final int i : is)
            rem.add(data.get(i));
        for (int i = 0; i < is.length; i++)
            data.remove(rem.get(i));
        model.fireTableDataChanged();
    }

    protected void add() {
        long max = -1;
        for (int i = 0; i < data.size(); i++)
            if (data.get(i).getVariantId() > max)
                max = data.get(i).getVariantId();
        VariantPropertyPersistent p = new VariantPropertyPersistent();
        p.setVariantId(max + 1);
        p.setValue("");
        data.add(p);
        model.fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    public void load(List<VariantPropertyPersistent> list) {
        remove.setEnabled(table.getSelectedRowCount() > 0);
        data = new ArrayList<VariantPropertyPersistent>(list);
        model.fireTableDataChanged();
        setVisible(true);
    }

    public List<VariantPropertyPersistent> getData() {
        return data;
    }
}

package com.ramussoft.gui.elist;

import java.awt.Component;
import java.util.StringTokenizer;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.core.attribute.simple.ElementListPersistent;
import com.ramussoft.core.attribute.simple.ElementListPropertyPersistent;

public abstract class ElistTable extends JTable {

    /**
     *
     */
    private static final long serialVersionUID = 6803264105996013240L;

    private JComboBox comboBox = new JComboBox();

    private ElistCellEditor cellEditor = new ElistCellEditor();

    private BooleanCellEditor booleanCellEditor = new BooleanCellEditor();

    private BooleanRenderer booleanRenderer = new BooleanRenderer();

    private DefaultTableCellRenderer textRenderer = new DefaultTableCellRenderer();

    private Attribute cached = null;

    private boolean showBoolean = true;

    public ElistTable() {
        textRenderer.setHorizontalAlignment(JLabel.CENTER);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if (cached != getAttribute())
            updateShowBoolean();

        if (showBoolean)
            return booleanCellEditor;

        return cellEditor;
    }

    private void updateShowBoolean() {
        Engine engine = getEngine();
        ElementListPropertyPersistent pp = (ElementListPropertyPersistent) engine
                .getAttribute(null, getAttribute());

        String connectionTypes = pp.getConnectionTypes();
        showBoolean = connectionTypes == null
                || connectionTypes.trim().length() == 0;
        cached = getAttribute();
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        row = convertRowIndexToModel(row);
        column = convertColumnIndexToModel(column);

        ElementListPersistent lp = (ElementListPersistent) getModel()
                .getValueAt(row, column);
        if (lp == null)
            return textRenderer;

        if (cached != getAttribute())
            updateShowBoolean();

        if (showBoolean)
            return booleanRenderer;

        return textRenderer;
    }

    protected abstract Attribute getAttribute();

    protected abstract Engine getEngine();

    private class ElistCellEditor extends DefaultCellEditor {
        /**
         *
         */
        private static final long serialVersionUID = -5272413157435833221L;

        public ElistCellEditor() {
            super(comboBox);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value, boolean isSelected, int row, int column) {
            Engine engine = getEngine();
            ElementListPropertyPersistent pp = (ElementListPropertyPersistent) engine
                    .getAttribute(null, getAttribute());

            String connectionTypes = pp.getConnectionTypes();
            StringTokenizer st = new StringTokenizer(connectionTypes, "\n");
            comboBox.removeAllItems();
            comboBox.addItem("");
            while (st.hasMoreElements()) {
                comboBox.addItem(st.nextToken().trim());
            }
            return super.getTableCellEditorComponent(table, value, isSelected,
                    row, column);
        }
    }

    private class BooleanCellEditor extends DefaultCellEditor {
        /**
         *
         */
        private static final long serialVersionUID = -527241345677833221L;

        public BooleanCellEditor() {
            super(new JCheckBox());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value, boolean isSelected, int row, int column) {
            return super.getTableCellEditorComponent(table, value != null,
                    isSelected, row, column);
        }
    }

    static class BooleanRenderer extends JCheckBox implements
            TableCellRenderer, UIResource {
        /**
         *
         */
        private static final long serialVersionUID = -5703409888709749526L;

        private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        public BooleanRenderer() {
            super();
            setHorizontalAlignment(JLabel.CENTER);
            setBorderPainted(true);
        }

        public Component getTableCellRendererComponent(JTable table,
                                                       Object value, boolean isSelected, boolean hasFocus, int row,
                                                       int column) {
            value = value != null;
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(table.getBackground());
            }
            setSelected((value != null && ((Boolean) value).booleanValue()));

            if (hasFocus) {
                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
            } else {
                setBorder(noFocusBorder);
            }

            return this;
        }
    }

}

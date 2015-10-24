package com.ramussoft.gui.qualifier.select;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.JTableHeader;

import org.jdesktop.swingx.JXTreeTable;

import com.ramussoft.database.common.Row;
import com.ramussoft.gui.qualifier.table.SelectType;

public class TableRowHeader extends JList {
    /**
     *
     */
    private static final long serialVersionUID = -8395654826690979872L;

    private final JXTreeTable table;

    private final QualifierModel model;

    private final int m;

    private ListCellRenderer renderer = new CellRenderer();

    private boolean groupSelect = true;

    private SelectType selectType;

    private class CellRenderer implements ListCellRenderer {

        private final JLabel label = new JLabel();

        Border a = BorderFactory.createEtchedBorder();

        Border b = BorderFactory.createRaisedBevelBorder();

        private final JCheckBox checkBox = new JCheckBox();

        private final JRadioButton radioButton = new JRadioButton();

        {
            radioButton.setHorizontalTextPosition(SwingConstants.LEFT);
            radioButton.setHorizontalAlignment(SwingConstants.TRAILING);

            checkBox.setHorizontalTextPosition(SwingConstants.LEFT);
            checkBox.setHorizontalAlignment(SwingConstants.TRAILING);

        }

        public Component getListCellRendererComponent(final JList list,
                                                      final Object value, final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {

            if (value == null) {
                label.setBorder(isSelected ? b : a);
                label.setText("");
                return label;
            }

            if ((groupSelect) || (((Row) value).getChildCount() == 0)) {

                if (selectType.equals(SelectType.CHECK)) {
                    checkBox.setBorder(isSelected ? b : a);
                    checkBox.setText(((Row) value).getCode());
                    checkBox.setSelected(model.isChecked((Row) value));
                    return checkBox;
                }

                if (selectType.equals(SelectType.RADIO)) {
                    radioButton.setBorder(isSelected ? b : a);
                    radioButton.setText(((Row) value).getCode());
                    radioButton.setSelected(model.isChecked((Row) value));
                    return radioButton;
                }
            }

            label.setBorder(isSelected ? b : a);
            label.setText(((Row) value).getCode());
            return label;
        }
    }

    ;

    TableModelListener listener = new TableModelListener() {

        public void tableChanged(TableModelEvent e) {
            setModel(new TableRowHeaderModel(table));
            updatePrefferedWidth(0, table.getRowCount() - 1);
        }

    };

    public TableRowHeader(final JXTreeTable table, final QualifierModel model) {
        super(new TableRowHeaderModel(table));
        this.table = table;
        this.model = model;
        // ((RowTreeTable) table).setHeader(this);
        m = preferredHeaderWidth();
        setFixedCellHeight(table.getRowHeight());
        setFixedCellWidth(m);
        setCellRenderer(renderer);

        setSelectionModel(table.getSelectionModel());
        table.getModel().addTableModelListener(listener);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                final int index = locationToIndex(e.getPoint());
                if (index >= 0) {
                    final Row row = (Row) getModel().getElementAt(index);
                    if (row != null) {
                        TableRowHeader.this.model.setSelectedRow(row,
                                !TableRowHeader.this.model.isChecked(row));
                        TableRowHeader.this.repaint();
                    }
                }
            }
        });
    }

    /**
     * * Returns the bounds of the specified range of items in JList *
     * coordinates. Returns null if index isn't valid. * *
     *
     * @param index0 the index of the first JList cell in the range *
     * @param index1 the index of the last JList cell in the range *
     * @return the bounds of the indexed cells in pixels
     */
    @Override
    public Rectangle getCellBounds(final int index0, final int index1) {
        final Rectangle rect0 = table.getCellRect(index0, 0, true);
        final Rectangle rect1 = table.getCellRect(index1, 0, true);
        int y, height;
        if (rect0.y < rect1.y) {
            y = rect0.y;
            height = rect1.y + rect1.height - y;
        } else {
            y = rect1.y;
            height = rect0.y + rect0.height - y;
        }
        return new Rectangle(0, y, getFixedCellWidth(), height);
    }

    private int preferredHeaderWidth() {
        final JLabel longestRowLabel = new JLabel("1.1.1");
        final JTableHeader header = table.getTableHeader();
        longestRowLabel.setBorder(header.getBorder());
        longestRowLabel.setHorizontalAlignment(SwingConstants.CENTER);
        longestRowLabel.setFont(header.getFont());
        return longestRowLabel.getPreferredSize().width;
    }

    private void updatePrefferedWidth(int firstRow, int lastRow) {
        final ListModel model = getModel();
        final int l = model.getSize() - 1;
        if (lastRow > l)
            lastRow = l;
        if (firstRow < 0)
            firstRow = 0;
        int m = this.m;
        for (int i = firstRow; i <= lastRow; i++) {
            final Object obj = model.getElementAt(i);
            if (obj == null)
                continue;
            final Component c = renderer.getListCellRendererComponent(this,
                    obj, i, true, true);
            final int t = c.getPreferredSize().width + 2;
            if (t > m)
                m = t;
        }
        if (m != getFixedCellWidth()) {
            setFixedCellWidth(m);
            revalidate();
        }
    }

    @Override
    public void scrollRectToVisible(final Rectangle rect) {
        if (table != null)
            table.scrollRectToVisible(rect);
        else
            super.scrollRectToVisible(rect);
    }

    public void setSelectType(SelectType selectType) {
        this.selectType = selectType;
        this.repaint();
    }

    public void updateWidth() {
        setModel(new TableRowHeaderModel(table));
        updatePrefferedWidth(0, table.getRowCount() - 1);
    }

    public void setGroupSelect(boolean groupSelect) {
        this.groupSelect = groupSelect;
    }
}

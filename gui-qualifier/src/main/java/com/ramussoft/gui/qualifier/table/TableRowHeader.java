package com.ramussoft.gui.qualifier.table;

import java.awt.Component;

import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

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
import javax.swing.table.TableCellRenderer;

import com.ramussoft.common.Attribute;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;

public class TableRowHeader extends JList implements ImportExport {
    /**
     *
     */
    private static final long serialVersionUID = -8395654826690979872L;

    private final RowTreeTable table;

    private final RowTreeTableModel model;

    private final int m;

    private ListCellRenderer renderer = new SimpleCellRenderer();

    private class SimpleCellRenderer implements ListCellRenderer {

        private final JLabel label = new JLabel();

        Border a = BorderFactory.createEtchedBorder();

        Border b = BorderFactory.createRaisedBevelBorder();

        public Component getListCellRendererComponent(final JList list,
                                                      final Object value, final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {
            TreeTableNode treeTableNode = (TreeTableNode) value;
            Row row = treeTableNode.getRow();
            label.setBorder(isSelected ? b : a);
            String code = row == null ? "" : row.getCode();
            if (treeTableNode instanceof Codeable)
                code = ((Codeable) treeTableNode).getCode();
            label.setText(code);
            return label;
        }
    }

    ;

    protected class CheckCellRenderer implements ListCellRenderer {

        private final JCheckBox checkBox = new JCheckBox();

        {
            checkBox.setHorizontalTextPosition(SwingConstants.LEFT);
            checkBox.setHorizontalAlignment(SwingConstants.TRAILING);
        }

        Border a = BorderFactory.createEtchedBorder();

        Border b = BorderFactory.createRaisedBevelBorder();

        public Component getListCellRendererComponent(final JList list,
                                                      final Object value, final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {
            TreeTableNode treeTableNode = (TreeTableNode) value;
            Row row = treeTableNode.getRow();
            String code = row == null ? "" : row.getCode();
            if (treeTableNode instanceof Codeable)
                code = ((Codeable) treeTableNode).getCode();
            if (row == null) {
                checkBox.setBorder(isSelected ? b : a);
                checkBox.setText(code);
                checkBox.setSelected(model.isChecked(treeTableNode));
                return checkBox;
            }
            checkBox.setBorder(isSelected ? b : a);
            checkBox.setText(code);
            checkBox.setSelected(model.isChecked(row));
            return checkBox;
        }
    }

    protected class RadioCellRenderer implements ListCellRenderer {

        private final JRadioButton radioButton = new JRadioButton();

        {
            radioButton.setHorizontalTextPosition(SwingConstants.LEFT);
            radioButton.setHorizontalAlignment(SwingConstants.TRAILING);
        }

        private final JLabel label = new JLabel();

        Border a = BorderFactory.createEtchedBorder();

        Border b = BorderFactory.createRaisedBevelBorder();

        public Component getListCellRendererComponent(final JList list,
                                                      final Object value, final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {
            TreeTableNode treeTableNode = (TreeTableNode) value;
            Row row = treeTableNode.getRow();
            String code = row == null ? "" : row.getCode();
            if (treeTableNode instanceof Codeable)
                code = ((Codeable) treeTableNode).getCode();
            if (row == null) {
                label.setBorder(isSelected ? b : a);
                label.setText(code);
                return label;
            }
            radioButton.setBorder(isSelected ? b : a);
            radioButton.setText(code);
            radioButton.setSelected(model.isChecked(row));
            return radioButton;
        }
    }

    TableModelListener listener = new TableModelListener() {

        public void tableChanged(TableModelEvent e) {
            setModel(new TableRowHeaderModel(table));
            updatePrefferedWidth(0, table.getRowCount() - 1);
        }

    };

    public TableRowHeader(final RowTreeTable table,
                          final RowTreeTableModel model) {
        super(new TableRowHeaderModel(table));
        this.table = table;
        this.model = model;
        // ((RowTreeTable) table).setHeader(this);
        m = preferredHeaderWidth();
        setFixedCellHeight(table.getRowHeight());
        setFixedCellWidth(m);
        // setCellRenderer(renderer);
        setCellRenderer(renderer);
        setSelectionModel(table.getSelectionModel());
        table.getModel().addTableModelListener(listener);
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                final int index = locationToIndex(e.getPoint());
                if (index >= 0) {
                    TreeTableNode treeTableNode = (TreeTableNode) getModel()
                            .getElementAt(index);
                    final Row row = treeTableNode.getRow();
                    if (row != null) {
                        TableRowHeader.this.model.setSelectedRow(row,
                                !TableRowHeader.this.model.isChecked(row));
                        TableRowHeader.this.repaint();
                    } else {
                        TableRowHeader.this.model.setSelectedRow(treeTableNode,
                                !TableRowHeader.this.model
                                        .isChecked(treeTableNode));
                        TableRowHeader.this.repaint();
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if (index >= 0) {
                    TreeTableNode treeTableNode = (TreeTableNode) getModel()
                            .getElementAt(index);
                    final Row row = treeTableNode.getRow();
                    String current = getToolTipText();
                    if (row == null) {
                        if (current != null)
                            setToolTipText(null);
                    } else {
                        String tipText = createToolTipText(row, index);
                        if (!tipText.equals(current))
                            setToolTipText(tipText);
                    }
                }
            }

            private String createToolTipText(Row row, int index) {

                StringBuffer sb = new StringBuffer();
                sb.append("<html><body>");
                sb.append("<table>");
                RowSet rs = row.getRowSet();
                for (int i = 0; i < table.getColumnCount(); i++) {
                    int c = table.convertColumnIndexToModel(i);
                    Attribute attribute = rs.getAttributes()[c];
                    if (attribute.getAttributeType().isLight()) {
                        TableCellRenderer renderer = table.getCellRenderer(
                                index, i);
                        String text;
                        Object object = table.getValueAt(index, i);
                        Component comp = renderer
                                .getTableCellRendererComponent(table, object,
                                        false, false, index, i);
                        if (comp instanceof JLabel) {

                            text = ((JLabel) comp).getText();
                        } else {
                            if (object != null) {
                                text = String.valueOf(object);
                            } else
                                text = "";
                        }
                        sb.append("<tr>");

                        sb.append("<td><u>");

                        String name = ((RowTreeTableModel) table
                                .getTreeTableModel()).getFramework()
                                .getSystemAttributeName(attribute);
                        if (name == null)
                            name = attribute.getName();
                        sb.append(name);
                        sb.append("</u>: ");

                        sb.append("</td>");

                        sb.append("<td>");

                        sb.append(text);

                        sb.append("</td>");

                        sb.append("</tr>");
                    }
                }

                sb.append("<tr>");

                sb.append("<td><u>");

                sb.append("UID");
                sb.append("</u>: ");

                sb.append("</td>");

                sb.append("<td>");

                sb.append(row.getElementId());

                sb.append("</td>");

                sb.append("</tr>");

                sb.append("</table>");
                sb.append("</body></html>");
                return sb.toString();
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

    public Transferable createTransferable() {
        return table.createTransferable();
    }

    public boolean importData(final Transferable t) {
        return table.importData(t, false, getDropLocation().getIndex());
    }

    public boolean canImport(final DataFlavor[] transferFlavors) {
        return table.canImport(transferFlavors);
    }

    public void exportDone(final Transferable data, final int action) {
        table.exportDone(data, action);
    }

    @Override
    public void scrollRectToVisible(final Rectangle rect) {
        if (table != null)
            table.scrollRectToVisible(rect);
        else
            super.scrollRectToVisible(rect);
    }

    public void setSelectType(SelectType selectType) {

        if (selectType.equals(SelectType.CHECK))
            renderer = new CheckCellRenderer();
        else if (selectType.equals(SelectType.RADIO))
            renderer = new RadioCellRenderer();
        else
            renderer = new SimpleCellRenderer();

        setCellRenderer(renderer);
        this.repaint();
    }

    public void updateWidth() {
        setModel(new TableRowHeaderModel(table));
        updatePrefferedWidth(0, table.getRowCount() - 1);
    }

}

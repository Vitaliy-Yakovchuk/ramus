package com.ramussoft.gui.qualifier.table;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellRenderer;

public class FirstRowCellEditor implements TableCellEditor {

    private final TableCellEditor deligate;

    private final JTree tree;

    private final RowTreeTable treeTable;

    public FirstRowCellEditor(final TableCellEditor deligate, final JTree tree,
                              final RowTreeTable treeTable) {
        this.deligate = deligate;
        this.tree = tree;
        this.treeTable = treeTable;
    }

    public Component getTableCellEditorComponent(final JTable table,
                                                 final Object value, final boolean isSelected, final int row,
                                                 final int column) {
        return deligate.getTableCellEditorComponent(table, value, isSelected,
                row, column);
    }

    public void addCellEditorListener(final CellEditorListener l) {
        deligate.addCellEditorListener(l);
    }

    public void cancelCellEditing() {
        deligate.cancelCellEditing();
    }

    public Object getCellEditorValue() {
        return deligate.getCellEditorValue();
    }

    public boolean isCellEditable(final EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            final MouseEvent event = (MouseEvent) anEvent;
            final int row = treeTable.rowAtPoint(event.getPoint());
            final Rectangle bounds = tree.getRowBounds(row);
            int offset = bounds.x;
            final Object node = tree.getPathForRow(row).getLastPathComponent();
            final boolean leaf = tree.getModel().isLeaf(node);
            final boolean expanded = tree.isExpanded(row);
            final TreeCellRenderer tcr = tree.getCellRenderer();
            final Component treeComponent = tcr.getTreeCellRendererComponent(
                    tree, node, true, expanded, leaf, row, false);
            if (treeComponent instanceof JLabel) {
                final JLabel label = (JLabel) treeComponent;

                final Icon icon = label.getIcon();
                if (icon != null) {
                    offset += icon.getIconWidth() + label.getIconTextGap();
                }

            }
            if (event.getPoint().x < offset)
                return false;
        }
        return deligate.isCellEditable(anEvent);
    }

    public void removeCellEditorListener(final CellEditorListener l) {
        deligate.removeCellEditorListener(l);
    }

    public boolean shouldSelectCell(final EventObject anEvent) {
        return deligate.shouldSelectCell(anEvent);
    }

    public boolean stopCellEditing() {
        return deligate.stopCellEditing();
    }

}

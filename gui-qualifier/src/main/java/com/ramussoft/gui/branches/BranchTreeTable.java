package com.ramussoft.gui.branches;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.JXTree.DelegatingRenderer;
import org.jdesktop.swingx.treetable.TreeTableModel;

public class BranchTreeTable extends JXTreeTable {
    /**
     *
     */
    private static final long serialVersionUID = -6784013907857444376L;

    private ImageIcon branchActual;

    private ImageIcon branchTree;

    private ImageIcon branch;

    private BranchView branchView;

    public BranchTreeTable(TreeTableModel treeModel, BranchView branchView) {
        super(treeModel);
        this.branchView = branchView;
        branch = new ImageIcon(getClass().getResource(
                "/com/ramussoft/gui/branch-down.png"));
        branchActual = new ImageIcon(getClass().getResource(
                "/com/ramussoft/gui/branch-actual.png"));
        branchTree = new ImageIcon(getClass().getResource(
                "/com/ramussoft/gui/branch-down-right.png"));
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if (column == 0) {
            JTree tree = (JTree) super.getCellRenderer(row, column);
            DelegatingRenderer rend = ((DelegatingRenderer) tree
                    .getCellRenderer());
            TreePath pathForRow = tree
                    .getPathForRow(convertRowIndexToModel(row));
            if (pathForRow == null)
                return super.getCellRenderer(row, column);
            BranchView.Node n = (BranchView.Node) pathForRow
                    .getLastPathComponent();
            if (n.branch.getBranchId() == branchView.getActualBranch()) {
                rend.setLeafIcon(branchActual);
                rend.setOpenIcon(branchActual);
                rend.setClosedIcon(branchActual);
            } else {
                if (n.branch.getChildren().size() < 2) {
                    rend.setLeafIcon(branch);
                    rend.setOpenIcon(branch);
                    rend.setClosedIcon(branch);
                } else {
                    rend.setLeafIcon(branchTree);
                    rend.setOpenIcon(branchTree);
                    rend.setClosedIcon(branchTree);
                }
            }
        }
        return super.getCellRenderer(row, column);
    }
}
package com.ramussoft.gui.qualifier.table;

import java.util.Vector;

import com.ramussoft.common.Engine;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.database.common.RowSet.RowCreater;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.event.Closeable;

public class RowRootCreater implements RootCreater {

    protected RowNode root;

    @Override
    public TreeTableNode createRoot(RowSet rowSet) {
        root = new RowNode(createRowChildren(rowSet.getRoot()), rowSet
                .getRoot());
        root.setParent();
        return root;
    }

    public static Vector<TreeTableNode> createRowChildren(Row root) {

        Vector<TreeTableNode> res = new Vector<TreeTableNode>();

        for (Row row : root.getChildren()) {
            RowNode node = new RowNode(createRowChildren(row), row);
            node.setParent();
            res.add(node);
        }
        return res;
    }

    public TreeTableNode findNode(Row row) {
        TreeTableNode node = findNodeA(row, root.getChildren());
        if (node != null)
            return node;
        return root;
    }

    public TreeTableNode findNodeA(Row row, Vector<TreeTableNode> vector) {
        for (TreeTableNode node : vector) {
            if (row.equals(node.getRow()))
                return node;
            TreeTableNode res = findNodeA(row, node.getChildren());
            if (res != null)
                return res;
        }
        return null;
    }

    @Override
    public RowCreater getRowCreater() {
        return null;
    }

    @Override
    public void init(Engine engine, GUIFramework framework, Closeable model) {
    }

    @Override
    public TreeTableNode findNode(TreeTableNode parent, Row row) {
        return findNodeA(row, parent.getChildren());
    }

    @Override
    public TreeTableNode[] findParentNodes(Row parentRow, Row row) {
        return new TreeTableNode[]{findNode(parentRow)};
    }

    @Override
    public TreeTableNode getRoot() {
        return root;
    }

}

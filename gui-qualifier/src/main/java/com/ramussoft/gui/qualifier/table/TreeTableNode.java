package com.ramussoft.gui.qualifier.table;

import java.util.Vector;

import com.ramussoft.database.common.Row;

public abstract class TreeTableNode implements TableNode {

    protected Row row;

    protected Vector<TreeTableNode> children;

    private TreeTableNode parent;

    public TreeTableNode(Vector<TreeTableNode> childrens) {
        this.children = childrens;
    }

    public Row getRow() {
        return row;
    }

    public Object getChild(int index) {
        try {
            return children.get(index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getChildCount() {
        return children.size();
    }

    public int getIndexOfChild(TreeTableNode child) {
        return children.indexOf(child);
    }

    public TreeTableNode getParent() {
        return parent;
    }

    public void setParent(TreeTableNode parent) {
        this.parent = parent;
    }

    public void setParent() {
        for (TreeTableNode node : children)
            node.setParent(this);
    }

    public Vector<TreeTableNode> getChildren() {
        return children;
    }
}

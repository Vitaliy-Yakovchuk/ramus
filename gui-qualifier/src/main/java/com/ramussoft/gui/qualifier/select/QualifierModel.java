package com.ramussoft.gui.qualifier.select;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.tree.TreeNode;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import com.ramussoft.common.Element;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.qualifier.table.SelectType;

public class QualifierModel extends AbstractTreeTableModel {

    private SelectType selectType;

    private Hashtable<Row, Boolean> selected = new Hashtable<Row, Boolean>();

    public void setSelectType(SelectType selectType) {
        this.selectType = selectType;
    }

    public QualifierModel(Object root) {
        super(root);
    }

    @Override
    public int getColumnCount() {
        return 1;
    }

    @Override
    public String getColumnName(int column) {
        return GlobalResourcesManager.getString("AttributeName");
    }

    @Override
    public Object getValueAt(Object node, int column) {
        return node;
    }

    @Override
    public Object getChild(Object parent, int index) {
        Row row = (Row) parent;
        return row.getChildAt(index);
    }

    @Override
    public int getChildCount(Object parent) {
        Row row = (Row) parent;
        return row.getChildCount();
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        Row row = (Row) parent;
        return row.getIndex((TreeNode) child);
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return false;
    }

    public boolean isChecked(Row value) {
        return selected.get(value) != null;
    }

    public void setSelectedRow(Row row, boolean b) {
        if (b) {
            if (selectType.equals(SelectType.RADIO))
                selected.clear();
            selected.put(row, Boolean.TRUE);
        } else {
            selected.remove(row);
        }
    }

    public List<Row> getSelected() {
        List<Row> result = new ArrayList<Row>();

        addResults((Row) getRoot(), result);

        return result;
    }

    private void addResults(Row parent, List<Row> result) {
        for (Row row : parent.getChildren()) {
            if (isChecked(row))
                result.add(row);
            addResults(row, result);
        }
    }

    public void setSelectedElement(Element element, boolean b) {
        select((Row) getRoot(), element);
    }

    private void select(Row row, Element element) {
        if ((row.getElement() != null) && (row.getElement().equals(element))) {
            setSelectedRow(row, true);
            return;
        }
        for (Row row2 : row.getChildren())
            select(row2, element);
    }

}

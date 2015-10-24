package com.ramussoft.gui.qualifier.table;

import java.util.Vector;

import com.ramussoft.database.common.Row;

public class RowNode extends TreeTableNode {

    public RowNode(Vector<TreeTableNode> childs, Row row) {
        super(childs);
        this.row = row;
    }

    @Override
    public Object getValueAt(int index) {
        return row.getAttribute(index);
    }

}

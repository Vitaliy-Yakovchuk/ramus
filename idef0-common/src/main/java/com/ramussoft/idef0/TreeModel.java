package com.ramussoft.idef0;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.ramussoft.common.journal.Journaled;
import com.ramussoft.database.common.Row;

public class TreeModel extends DefaultTreeModel {

    public TreeModel() {
        super(null);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        Object o = path.getLastPathComponent();
        Row row = (Row) o;
        ((Journaled) row.getEngine()).startUserTransaction();
        row.setName(String.valueOf(newValue));
        ((Journaled) row.getEngine()).commitUserTransaction();
    }

}
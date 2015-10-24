package com.ramussoft.gui.qualifier.table;

import com.ramussoft.common.Engine;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.database.common.RowSet.RowCreater;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.event.Closeable;

public interface RootCreater {
    TreeTableNode createRoot(RowSet rowSet);

    TreeTableNode findNode(TreeTableNode parent, Row row);

    RowCreater getRowCreater();

    void init(Engine engine, GUIFramework framework, Closeable model);

    TreeTableNode[] findParentNodes(Row parentRow, Row row);

    TreeTableNode getRoot();
}

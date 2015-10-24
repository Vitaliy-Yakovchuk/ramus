package com.ramussoft.gui.attribute.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

// Steve Webb 16/09/04 swebb99_uk@hotmail.com

/**
 * Class which extends the functionality of DefaultColumnTableModel to
 * also provide capabilities to group columns. This can be used for
 * instance to aid in the layout of groupable table headers.
 */
public class GroupableTableColumnModel extends DefaultTableColumnModel {

    /**
     *
     */
    private static final long serialVersionUID = -5174204582670343602L;
    /**
     * Hold the list of ColumnGroups which define what group each normal
     * column is within, if any.
     */
    @SuppressWarnings("unchecked")
    protected ArrayList columnGroups = new ArrayList();


    /**
     * Add a new columngroup.
     *
     * @param columnGroup new ColumnGroup
     */
    @SuppressWarnings("unchecked")
    public void addColumnGroup(ColumnGroup columnGroup) {
        columnGroups.add(columnGroup);
    }

    /**
     * Provides an Iterator to iterate over the
     * ColumnGroup list.
     *
     * @return Iterator over ColumnGroups
     */
    @SuppressWarnings("unchecked")
    public Iterator columnGroupIterator() {
        return columnGroups.iterator();
    }

    /**
     * Returns a ColumnGroup specified by an index.
     *
     * @param index index of ColumnGroup
     * @return ColumnGroup
     */
    public ColumnGroup getColumnGroup(int index) {
        if (index >= 0 && index < columnGroups.size()) {
            return (ColumnGroup) columnGroups.get(index);
        }
        return null;
    }

    /**
     * Provides and iterator for accessing the ColumnGroups
     * associated with a column.
     *
     * @param col Column
     * @return ColumnGroup iterator
     */
    @SuppressWarnings("unchecked")
    public Iterator getColumnGroups(TableColumn col) {
        if (columnGroups.isEmpty()) return null;
        Iterator iter = columnGroups.iterator();
        while (iter.hasNext()) {
            ColumnGroup cGroup = (ColumnGroup) iter.next();
            Vector v_ret = cGroup.getColumnGroups(col, new Vector());
            if (v_ret != null) {
                return v_ret.iterator();
            }
        }
        return null;
    }
}



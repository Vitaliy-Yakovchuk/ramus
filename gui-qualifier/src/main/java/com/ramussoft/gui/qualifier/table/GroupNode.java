package com.ramussoft.gui.qualifier.table;

import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import com.ramussoft.common.Attribute;
import com.ramussoft.database.StringCollator;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.attribute.DateAttributePlugin;
import com.ramussoft.gui.common.GlobalResourcesManager;

public class GroupNode extends TreeTableNode implements Comparable<GroupNode> {

    private Object value;

    private ArrayList<Row> rows = null;

    private Attribute attribute;

    private GroupRootCreater groupRootCreater;

    public GroupNode(GroupRootCreater groupRootCreater, Attribute attribute,
                     Object value) {
        super(null);
        if (value instanceof Date) {
            value = DateAttributePlugin.DATE_INSTANCE.format(value);
        }
        this.value = value;
        this.attribute = attribute;
        this.groupRootCreater = groupRootCreater;
    }

    public void setChildren(Vector<TreeTableNode> children) {
        this.children = children;
    }

    @Override
    public Object getValueAt(int index) {
        return value;
    }

    public Object getValue() {
        if (value == null)
            return GlobalResourcesManager.getString("EmptyGroupNodeValue");
        return groupRootCreater.convertModelValueToViewValue(this);
    }

    public ArrayList<Row> getRows() {
        return rows;
    }

    @Override
    public int hashCode() {
        if (value == null)
            return 0;
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof GroupNode) {
            GroupNode node = (GroupNode) obj;
            if (node.value == null)
                return value == null;
            return node.value.equals(value);
        }
        return super.equals(obj);
    }

    public void createRowsList() {
        rows = new ArrayList<Row>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(GroupNode o) {
        if (value == null) {
            if (o.value == null)
                return 0;
            return 1;
        }
        if (o.value == null)
            return -1;
        if ((value instanceof String) || (!(value instanceof Comparable))) {
            return StringCollator.compare(value.toString(), o.value.toString());
        }
        return ((Comparable) value).compareTo(o.value);
    }

    @Override
    public String toString() {
        if (value == null)
            return "EmptyGroupNodeValue";
        return value.toString();
    }

    public Attribute getAttribute() {
        return attribute;
    }
}

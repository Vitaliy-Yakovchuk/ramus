package com.ramussoft.pb.data;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.MutableTreeNode;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.types.GlobalId;

public abstract class AbstractRow extends com.ramussoft.database.common.Row
        implements Row {

    public GlobalId globalId = null;

    public AbstractDataPlugin dataPlugin;

    public int rowType = TYPE_ROW;

    public boolean element = true;

    public GlobalId parentGlobalId;

    private String status;

    public List<com.ramussoft.database.common.Row> getChilds() {
        return children;
    }

    public AbstractRow(final AbstractDataPlugin dataPlugin, Element element,
                       com.ramussoft.database.common.RowSet rowSet,
                       Attribute[] attributes, Object[] objects) {
        super(element, rowSet, attributes, objects);
        if (element != null)
            this.globalId = GlobalId.create(element.getId());
        else
            this.globalId = GlobalId.create(-1);
        this.dataPlugin = dataPlugin;
    }

    public String getToolTipText(final boolean big) {
        String tmp = "";
        if (tmp.equals("")) {
            if (getName().equals(""))
                tmp = null;
            if (big)
                tmp = getName();
            else
                tmp = null;
        } else {
            if (getName().equals("") && tmp.equals(""))
                tmp = null;
            else
                tmp = "<h3><b><center>" + getName() + "</b></center></h3>"
                        + tmp;
        }
        if (tmp == null)
            return null;
        return "<html>\n<body width=100%>\n" + tmp + "\n</body></html>";
    }

    public boolean isMoveable() {
        if (dataPlugin.isStatic(globalId))
            return false;
        return true;
    }

    public boolean isRemoveable() {
        return dataPlugin.getAccessRules().canDeleteElements(
                new long[]{getElementId()});
    }

    public boolean isCanHaveChilds() {
        return true;
    }

    public String getKod() {
        return super.getCode();
    }

    public String getFullKod() {
        final Row parent = getParentRow();
        if (parent == null)
            return Integer.toString(getId());
        return parent.getKod() + "." + getId();
    }

    public boolean isEditable() {
        return !dataPlugin.isStatic(globalId);
    }

    public void setUserObject(final Object object) {
        setName(object.toString());
    }

    public boolean getAllowsChildren() {
        return isCanHaveChilds();
    }

    public int compareTo(final Object o) {
        final Row row = (Row) o;
        int level1 = dataPlugin.getLevel(this);
        int level2 = dataPlugin.getLevel(row);
        final int maxLevel = level2 > level1 ? level2 : level1;
        for (int i = 0; i <= maxLevel; i++) {
            level1 = getId(i);
            level2 = ((AbstractRow) row).getId(i);
            if (level1 > level2)
                return 1;
            if (level1 < level2)
                return -1;
        }
        return level1 - level2;
    }

    public int getId(final int level) {
        final int myLevel = dataPlugin.getLevel(this);
        if (level > myLevel)
            return 0;
        else if (level == myLevel)
            return getId();
        else
            return ((AbstractRow) getParentRow()).getId(level);
    }

    public Row getRecParent() {
        final Row p = getParentRow();
        if (p == null || p.isElement() != isElement())
            return p;
        return p.getParentRow();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Row) {
            final Row row = (Row) obj;
            return getGlobalId().equals(row.getGlobalId());
        }
        return false;
    }

    public int getChildCount(final boolean element) {
        int n = 0;
        for (int i = 0; i < children.size(); i++) {
            final Row r = (Row) children.get(i);
            if (r.isElement() == element)
                n++;
        }
        return n;
    }

    public boolean isHaveChilds() {
        return getChilds().size() != 0;
    }

    public void setParent(final MutableTreeNode newParent) {
        setParentRow((Row) newParent);
    }

    @Deprecated
    public void sortByName(final boolean element) {
        final Vector<Row> v = dataPlugin.getChilds(this, element);
        final Object o[] = v.toArray();
        Arrays.sort(o, new RowNameComparator<Object>());
        /*
         * for (int i = 0; i < o.length; i++) { childs.remove(o[i]); }
		 * 
		 * for (int i = 0; i < o.length; i++) { ((Row)
		 * o[i]).sortByName(element); ((Row) o[i]).setParent(this, i); //
		 * childs.add((Row) o[i]); }
		 */
    }

    protected boolean isSameNameOnLevel(final String name) {
        List v;
        if (getParentRow() == null) {
            throw new RuntimeException();
            // v = dataPlugin.getNullChilds();
        } else
            v = ((AbstractRow) getParentRow()).children;
        for (int i = 0; i < v.size(); i++)
            if (!v.get(i).equals(this))
                if (isSameName((Row) v.get(i), name))
                    return true;
        return false;
    }

    protected boolean isSameName(final Row row, final String name) {
        return row.getName().equals(name);
    }

    public void insert(final MutableTreeNode child, final int index) {
    }

    public void remove(final int index) {
    }

    public void remove(final MutableTreeNode node) {
    }

    public void removeFromParent() {
    }

    public boolean remove() {
        rowSet.deleteRow(this);
        return engine.getElement(getElementId()) != null;
    }

	/*
	 * public void setParent(final Row par, final int pos) {
	 * super.setParent((com.ramussoft.database.common.Row) par, pos);
	 * dataPlugin.rowPropertyChanged(this, ChangeListener.RowProperty.PARENT); }
	 */

    public void setParentRow(final Row row) {
        super.setParent((com.ramussoft.database.common.Row) row);
    }

    public Row getParentRow() {
        return (Row) getParent();
    }

    public GlobalId getGlobalId() {
        return globalId;
    }

    public void setGlobalId(final GlobalId id) {
        globalId = id;
    }

    public void setRowType(final int rowType) {
        this.rowType = rowType;
    }

    public int getRowType() {
        return rowType;
    }

    public void setElement(final boolean element) {
        this.element = element;
    }

    public void setIdProperties() {
        setParentRow(dataPlugin.findRowByGlobalId(parentGlobalId));
    }

    public boolean isElement() {
        return element;
    }

    public GlobalId getParentGlobalId() {
        return parentGlobalId;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return globalId.hashCode();
    }

    public void setNameExtractor(final int version, final String extractAlgoritm) {
        System.err.println("WARNING: Setting of name extractor is ignored");
    }

    public AbstractDataPlugin getDataPlugin() {
        return dataPlugin;
    }

    @Override
    public String getAttachedStatus() {
        return status;
    }

    @Override
    public void setAttachedStatus(String status) {
        this.status = status;
    }
}

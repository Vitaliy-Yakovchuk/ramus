package com.ramussoft.gui.qualifier.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.core.attribute.simple.ElementListPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.database.common.RowSet.RowCreater;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.event.Closeable;

import static com.ramussoft.gui.qualifier.table.RowRootCreater.createRowChildren;

public class GroupRootCreater implements RootCreater {

    private Hierarchy hierarchy;

    private GroupNode root;

    private Attribute[] attrs;

    private Hashtable<Attribute, ValueGetter> hash = new Hashtable<Attribute, ValueGetter>();

    public GroupRootCreater(Hierarchy hierarchy) {
        this.hierarchy = hierarchy;
    }

    @Override
    public TreeTableNode createRoot(RowSet rowSet) {
        attrs = hierarchy.getAttributes(rowSet.getQualifier().getAttributes());
        root = new GroupNode(this, null, null);
        root.createRowsList();
        root.getRows().addAll(rowSet.getAllRows());
        root.setChildren(createChildren(0, root));
        root.setParent();
        return root;
    }

    private Vector<TreeTableNode> createChildren(int i, GroupNode parent) {
        List<Row> rows = parent.getRows();
        Vector<TreeTableNode> res = new Vector<TreeTableNode>();
        if (i >= attrs.length) {
            for (Row row : rows) {
                if (hierarchy.isShowBaseHierarchy()) {
                    RowNode rowNode = new RowNode(createRowChildren(row), row);
                    rowNode.setParent();
                    res.add(rowNode);
                } else {
                    RowNode rowNode = new RowNode(new Vector<TreeTableNode>(),
                            row);
                    rowNode.setParent();
                    res.add(rowNode);
                }
            }

        } else {
            if (attrs[i].getAttributeType().toString().equals(
                    "Core.ElementList")) {
                Hashtable<Long, Element> hash = new Hashtable<Long, Element>();
                for (Row row : rows) {
                    Engine engine = row.getEngine();
                    long[] ids = ElementListPlugin.getElements(engine,
                            attrs[i], row.getElement());
                    if (ids.length == 0) {
                        Object value = null;
                        GroupNode groupNode = new GroupNode(this, attrs[i],
                                value);
                        int index;
                        if ((index = res.indexOf(groupNode)) >= 0) {
                            ((GroupNode) res.get(index)).getRows().add(row);
                        } else {
                            groupNode.createRowsList();
                            groupNode.getRows().add(row);
                            res.add(groupNode);
                        }
                    }
                    for (long id : ids) {
                        Object value = getElement(hash, id, engine);
                        GroupNode groupNode = new GroupNode(this, attrs[i],
                                value);
                        int index;
                        if ((index = res.indexOf(groupNode)) >= 0) {
                            ((GroupNode) res.get(index)).getRows().add(row);
                        } else {
                            groupNode.createRowsList();
                            groupNode.getRows().add(row);
                            res.add(groupNode);
                        }
                    }
                }
            } else
                for (Row row : rows) {
                    Object value = row.getAttribute(attrs[i]);
                    GroupNode groupNode = new GroupNode(this, attrs[i], value);
                    int index;
                    if ((index = res.indexOf(groupNode)) >= 0) {
                        ((GroupNode) res.get(index)).getRows().add(row);
                    } else {
                        groupNode.createRowsList();
                        groupNode.getRows().add(row);
                        res.add(groupNode);
                    }
                }

            Object[] objects = res.toArray();
            Arrays.sort(objects);
            for (int j = 0; j < objects.length; j++) {
                res.set(j, (TreeTableNode) objects[j]);
            }

            for (TreeTableNode node : res) {
                GroupNode groupNode = (GroupNode) node;
                Vector<TreeTableNode> children = createChildren(i + 1,
                        groupNode);
                groupNode.setChildren(children);
                groupNode.setParent();
            }
        }
        return res;
    }

    private Element getElement(Hashtable<Long, Element> hash, Long id,
                               Engine engine) {
        Element element = hash.get(id);
        if (element == null) {
            element = engine.getElement(id);
            hash.put(id, element);
        }
        return element;
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
        for (Attribute a : attrs) {
            AttributePlugin plugin = framework.findAttributePlugin(a);
            ValueGetter getter = null;
            if (plugin instanceof TabledAttributePlugin) {
                getter = ((TabledAttributePlugin) plugin).getValueGetter(a,
                        engine, framework, model);
            }
            if (getter == null)
                getter = new RowTreeTableModel.DefaultValueGetter();
            hash.put(a, getter);
        }
    }

    public Object convertModelValueToViewValue(GroupNode node) {
        ValueGetter getter = hash.get(node.getAttribute());
        return getter.getValue(node, 0);
    }

    @Override
    public TreeTableNode findNode(TreeTableNode parent, Row row) {
        return findNodeA(row, parent.getChildren());
    }

    @Override
    public TreeTableNode[] findParentNodes(Row parentRow, Row row) {
        ArrayList<TreeTableNode> list = new ArrayList<TreeTableNode>(1);
        findParentNodes(row, root, list);
        return list.toArray(new TreeTableNode[list.size()]);
    }

    private void findParentNodes(Row row, TreeTableNode base,
                                 ArrayList<TreeTableNode> list) {
        Vector<TreeTableNode> children = base.getChildren();
        for (TreeTableNode node : children) {
            if (row.equals(node.getRow()))
                list.add(base);
            findParentNodes(row, node, list);
        }

    }

    @Override
    public TreeTableNode getRoot() {
        return root;
    }

}

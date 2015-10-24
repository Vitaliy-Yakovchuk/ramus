package com.ramussoft.gui.qualifier.table;

import java.awt.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.tree.TreeModelSupport;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.qualifier.table.event.CloseEvent;
import com.ramussoft.gui.qualifier.table.event.CloseListener;
import com.ramussoft.gui.qualifier.table.event.Closeable;
import com.ramussoft.gui.qualifier.table.event.SelectionEvent;
import com.ramussoft.gui.qualifier.table.event.SelectionListener;

public class RowTreeTableModel extends AbstractTreeTableModel implements
        Closeable {

    protected RowSet rowSet;

    private AccessRules accessor;

    private RowTreeTable table;

    private RootCreater rootCreater;

    private Hashtable<Row, Boolean> selectedRows = new Hashtable<Row, Boolean>();

    private Localizer[] localizers;

    private ValueGetter[] valueGetters;

    private boolean[] editableColumn;

    private boolean uniqueSelect = false;

    private EventListenerList closeListeners = new EventListenerList();

    private EventListenerList selectRowListeners = new EventListenerList();

    private Class<?>[] classes;

    private List<Element> elementsToHide;

    private boolean[] saveValues;

    private boolean editable = true;

    private GUIFramework framework;

    public static interface Localizer {
        Object getValue(Object key);
    }

    ;

    public static interface ColumnLocalizer {
        String getString(Attribute attribute);
    }

    ;

    private class EmptyLocalizer implements Localizer {

        @Override
        public Object getValue(Object key) {
            return key;
        }
    }

    ;

    public static class DefaultValueGetter implements ValueGetter {
        @Override
        public Object getValue(TableNode node, int index) {
            return node.getValueAt(index);
        }
    }

    ;

    public void setAttributeLocalizer(Localizer localizer, Attribute attribute) {
        Attribute[] attributes = rowSet.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            if (attribute.equals(attributes[i])) {
                localizers[i] = localizer;
                return;
            }
        }
        throw new RuntimeException("Attribute " + attribute + " not found");
    }

    public void setValueGetter(ValueGetter getter, Attribute attribute) {
        Attribute[] attributes = rowSet.getAttributes();
        for (int i = 0; i < attributes.length; i++) {
            if (attribute.equals(attributes[i])) {
                valueGetters[i] = getter;
                return;
            }
        }
        throw new RuntimeException("Attribute " + attribute + " not found");
    }

    public RowTreeTableModel(Engine engine, Qualifier qualifier,
                             Attribute[] attributes, AccessRules accessor,
                             final RootCreater rootCreater, GUIFramework framework) {
        this.framework = framework;
        localizers = new Localizer[attributes.length];
        valueGetters = new ValueGetter[attributes.length];

        saveValues = new boolean[attributes.length];
        Arrays.fill(saveValues, true);

        editableColumn = new boolean[attributes.length];
        Arrays.fill(editableColumn, true);

        for (int i = 0; i < localizers.length; i++) {
            localizers[i] = new EmptyLocalizer();
            valueGetters[i] = new DefaultValueGetter();
        }

        this.rootCreater = rootCreater;

        this.rowSet = new RowSet(engine, qualifier, attributes,
                rootCreater.getRowCreater()) {

            @Override
            protected void removedFromChildren(Row parentRow, Row row, int i) {
                TreeTableNode[] nodes = rootCreater.findParentNodes(parentRow,
                        row);
                for (TreeTableNode parent : nodes) {
                    TreeTableNode node = rootCreater.findNode(parent, row);
                    int index = parent.getIndexOfChild(node);
                    if (index >= 0)
                        modelSupport.fireChildRemoved(nodeToTreePath(parent),
                                index, node);
                    parent.getChildren().remove(node);
                }
            }

            @Override
            protected void added(Row parent, final Row row, int index) {
                super.added(parent, row, index);
                TreeTableNode[] parentNodes = rootCreater.findParentNodes(
                        parent, row);
                TreeTableNode par;
                if (parentNodes.length > 0)
                    par = parentNodes[0];
                else
                    par = rootCreater.getRoot();

                TreePath parentPath = nodeToTreePath(par);

                final RowNode rowNode = createNode(row, par);

                if (par.getChildCount() < index) {
                    index = par.getChildCount();
                }

                par.getChildren().add(index, rowNode);

                modelSupport.fireChildAdded(parentPath, index, rowNode);
                table.expandPath(parentPath);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        TreePath treePath = nodeToTreePath(rowNode);
                        table.scrollPathToVisible(treePath);
                        try {
                            table.getTreeSelectionModel().setSelectionPath(
                                    treePath);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            protected void attributeChanged(Row row, Attribute attribute,
                                            Object newValue, Object oldValue, boolean journaled) {
                if (row.getParent() == null) {
                } else {
                    TreeTableNode[] nodes = rootCreater.findParentNodes(
                            row.getParent(), row);
                    for (TreeTableNode parent : nodes) {
                        TreeTableNode node = rootCreater.findNode(parent, row);
                        int index = parent.getIndexOfChild(node);
                        if (index >= 0) {
                            TreePath treePath = nodeToTreePath(parent);
                            modelSupport
                                    .fireChildChanged(treePath, index, node);
                            if (journaled) {

                                final TreePath treePath2 = nodeToTreePath(node);

                                SwingUtilities.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        table.scrollPathToVisible(treePath2);
                                        table.getTreeSelectionModel()
                                                .setSelectionPath(treePath2);
                                    }
                                });
                            }
                        }

                    }

                }
            }

            private RowNode createNode(Row row, TreeTableNode par) {
                RowNode res = newRowNode(row);
                fillChildren(res, row);
                res.setParent(par);
                return res;
            }

            private void fillChildren(RowNode node, Row row) {
                Vector<TreeTableNode> children = node.getChildren();
                for (Row r : row.getChildren()) {
                    RowNode rowNode = createNode(r, node);
                    children.add(rowNode);
                }
            }

            private TreePath nodeToTreePath(TreeTableNode node) {
                List<TreeTableNode> list = new ArrayList<TreeTableNode>();
                while (node != null) {
                    list.add(0, node);
                    node = node.getParent();
                }

                TreePath parentPath = new TreePath(list.toArray());
                return parentPath;
            }
        };
        this.accessor = accessor;
        this.root = rootCreater.createRoot(rowSet);
        rootCreater.init(engine, framework, this);

        classes = new Class<?>[attributes.length];

        for (int i = 0; i < attributes.length; i++) {
            classes[i] = framework.findAttributePlugin(attributes[i])
                    .getClass();
        }

        for (Attribute attribute : attributes) {
            AttributePlugin plugin = framework.findAttributePlugin(attribute);
            if (plugin instanceof TabledAttributePlugin) {
                ValueGetter getter = ((TabledAttributePlugin) plugin)
                        .getValueGetter(attribute, engine, framework, this);
                if (getter != null)
                    setValueGetter(getter, attribute);
            }
        }
    }

    protected RowNode newRowNode(Row row) {
        return new RowNode(new Vector<TreeTableNode>(row.getChildCount()), row);
    }

    protected void expandPath(TreePath treePath) {
        if (treePath == null)
            return;
        expandPath(treePath.getParentPath());
        table.expandPath(treePath);
    }

    public void setTable(RowTreeTable table) {
        this.table = table;
    }

    @Override
    public int getColumnCount() {
        return rowSet.getAttributes().length;
    }

    @Override
    public Object getValueAt(Object node, int index) {
        TreeTableNode tableRow = (TreeTableNode) node;
        if (tableRow instanceof GroupNode) {
            if (index == 0) {
                GroupNode groupNode = (GroupNode) tableRow;
                if (table != null) {
                    Attribute attribute = groupNode.getAttribute();
                    if (attribute != null) {
                        AttributePlugin plugin = framework
                                .findAttributePlugin(attribute);
                        TableCellRenderer renderer = plugin
                                .getTableCellRenderer(rowSet.getEngine(),
                                        framework.getAccessRules(), attribute);
                        if (renderer != null) {
                            try {
                                Component c = renderer
                                        .getTableCellRendererComponent(table,
                                                groupNode.getValue(), false,
                                                false, -1, -1);
                                if (c instanceof JLabel)
                                    return ((JLabel) c).getText();
                            } catch (Exception e) {
                                // e.printStackTrace();
                            }
                        }
                    }
                }
                return groupNode.getValue();
            }
            return null;
        }
        return localizers[index].getValue(valueGetters[index].getValue(
                tableRow, index));
    }

    @Override
    public Object getChild(Object parent, int index) {
        TreeTableNode tableRow = (TreeTableNode) parent;
        if (elementsToHide != null) {
            for (int i = 0; i < tableRow.getChildCount(); i++) {
                if (i > index)
                    break;
                TreeTableNode child = (TreeTableNode) tableRow.getChild(i);
                if ((child.getRow() != null)
                        && (elementsToHide.indexOf(child.getRow().getElement()) >= 0))
                    index++;
            }
        }
        return tableRow.getChild(index);
    }

    @Override
    public int getChildCount(Object parent) {
        TreeTableNode tableRow = (TreeTableNode) parent;
        int cc = tableRow.getChildCount();
        if (elementsToHide != null) {
            for (int i = 0; i < tableRow.getChildCount(); i++) {
                TreeTableNode child = (TreeTableNode) tableRow.getChild(i);
                if ((child.getRow() != null)
                        && (elementsToHide.indexOf(child.getRow().getElement()) >= 0))
                    cc--;
            }
        }
        return cc;
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        TreeTableNode tableRow = (TreeTableNode) parent;
        if (elementsToHide != null) {
            int index = 0;
            for (int i = 0; i < tableRow.getChildCount(); i++) {
                TreeTableNode c = (TreeTableNode) tableRow.getChild(i);
                if ((c.getRow() != null)
                        && (elementsToHide.indexOf(c.getRow().getElement()) >= 0)) {
                } else
                    index++;
            }
            return index;
        }
        return tableRow.getIndexOfChild((TreeTableNode) child);
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        if (!editable)
            return false;
        if (!editableColumn[column])
            return false;
        TreeTableNode row = (TreeTableNode) node;
        if (row.getRow() == null)
            return false;
        long elementId = row.getRow().getElementId();
        long attributeId = rowSet.getAttributes()[column].getId();
        if (accessor.canUpdateElement(elementId, attributeId))
            return rowSet.getEngine().getCalculateInfo(elementId, attributeId) == null;
        return false;
    }

    @Override
    public String getColumnName(int column) {
        Attribute attribute = rowSet.getAttributes()[column];
        String name = framework.getSystemAttributeName(attribute);
        if (name != null)
            return name;
        return "<html><body><center>" + attribute.getName()
                + "</center></body></html>";
    }

    public RowSet getRowSet() {
        return rowSet;
    }

    public boolean isChecked(Row value) {
        Boolean res = selectedRows.get(value);
        if (res == null)
            return false;
        return res;
    }

    public void setSelectedRow(Row row, boolean b) {
        setSelectedRows(new Row[]{row}, b);
    }

    public void setSelectedRows(Row[] rows, boolean b) {
        if (uniqueSelect)
            selectedRows.clear();
        for (Row row : rows)
            selectedRows.put(row, b);
        SelectionEvent event = new SelectionEvent(rows, b);
        for (SelectionListener l : getSelectionListeners()) {
            l.changeSelection(event);
        }
    }

    public void selectRows(List<Long> list) {
        if (uniqueSelect)
            selectedRows.clear();
        for (Long l : list) {
            Row row = rowSet.findRow(l);
            if (row != null)
                selectedRows.put(row, Boolean.TRUE);
        }
    }

    public void refresh() {
        rowSet.refresh();
        root = rootCreater.createRoot(rowSet);
        modelSupport.fireNewRoot();
    }

    @Override
    public void setValueAt(Object value, Object node, int column) {
        if (!saveValues[column])
            return;
        TreeTableNode row = (TreeTableNode) node;
        if (row.getRow() == null)
            return;

        Attribute a = row.getRow().getRowAttributes()[column + 1];
        Engine engine = row.getRow().getRowSet().getEngine();
        Qualifier q = engine.getQualifier(row.getRow().getElement()
                .getQualifierId());

        if ((q.getAttributeForName() == a.getId()) && (value instanceof String)) {
            List<Element> list = engine.findElements(q.getId(), a, value);
            for (Element element : list) {
                if (element.getId() != row.getRow().getElementId()) {
                    if (JOptionPane.showConfirmDialog(table,
                            GlobalResourcesManager
                                    .getString("Warning.ElementsExists"),
                            GlobalResourcesManager
                                    .getString("ConfirmMessage.Title"),
                            JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                        return;
                    break;
                }
            }
        }

        row.getRow().startUserTransaction();
        row.getRow().setAttribute(column, value);
        row.getRow().endUserTransaction();
    }

    public List<Row> getSelectedRows() {
        List<Row> res = new ArrayList<Row>();
        for (Entry<Row, Boolean> e : selectedRows.entrySet()) {
            if (e.getValue()) {
                res.add(e.getKey());
            }
        }
        return res;
    }

    public void setEditable(int column, boolean b) {
        editableColumn[column] = b;
    }

    void setUniqueSelect(boolean b) {
        uniqueSelect = b;
    }

    public void close() {
        try {
            rowSet.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        CloseEvent event = new CloseEvent(this);
        for (CloseListener listener : getCloseListeners()) {
            listener.closed(event);
        }
    }

    public void addCloseListener(CloseListener listener) {
        closeListeners.add(CloseListener.class, listener);
    }

    public void removeCloseListener(CloseListener listener) {
        closeListeners.remove(CloseListener.class, listener);
    }

    public CloseListener[] getCloseListeners() {
        return closeListeners.getListeners(CloseListener.class);
    }

    public void setHideElements(List<Element> elementsToHide) {
        this.elementsToHide = elementsToHide;
        modelSupport.fireNewRoot();
    }

    public void setSaveValue(int column, boolean save) {
        saveValues[column] = save;
    }

    public SelectionListener[] getSelectionListeners() {
        return selectRowListeners.getListeners(SelectionListener.class);
    }

    public void addSelectionListener(SelectionListener listener) {
        selectRowListeners.add(SelectionListener.class, listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        selectRowListeners.remove(SelectionListener.class, listener);
    }

    public void setEditable(boolean b) {
        this.editable = b;
    }

    public boolean isEditable() {
        return editable;
    }

    public int getSelectedRowCount() {
        return getSelectedRows().size();
    }

    public TreeTableNode findNode(Row row) {
        return findNode(row, (TreeTableNode) root);
    }

    private TreeTableNode findNode(Row row, TreeTableNode parent) {
        TreeTableNode node = rootCreater.findNode(parent, row);
        if (node == null) {
            for (TreeTableNode node2 : parent.getChildren()) {
                node = findNode(row, node2);
                if (node != null)
                    return node;
            }
        }
        return node;
    }

    public void clearSelection() {
        Set<Row> rows = selectedRows.keySet();
        SelectionEvent event = new SelectionEvent(rows.toArray(new Row[rows
                .size()]), false);
        selectedRows.clear();
        for (SelectionListener l : getSelectionListeners())
            l.changeSelection(event);
    }

    public void checkAll() {
        selectedRows.clear();
        for (Row row : getRowSet().getAllRows())
            selectedRows.put(row, Boolean.TRUE);
        Set<Row> rows = selectedRows.keySet();
        SelectionEvent event = new SelectionEvent(rows.toArray(new Row[rows
                .size()]), true);
        for (SelectionListener l : getSelectionListeners()) {
            l.changeSelection(event);
        }
    }

    public void uncheckAll() {
        clearSelection();
    }

    public GUIFramework getFramework() {
        return framework;
    }

    public boolean isChecked(TreeTableNode treeTableNode) {
        if (treeTableNode.getRow() != null) {
            if (!isChecked(treeTableNode.getRow()))
                return false;
        }
        for (TreeTableNode node : treeTableNode.getChildren()) {
            if (!isChecked(node))
                return false;
        }
        return true;
    }

    public void setSelectedRow(TreeTableNode treeTableNode, boolean b) {
        List<Row> rows = new ArrayList<Row>();
        addRows(treeTableNode, rows);
        setSelectedRows(rows.toArray(new Row[rows.size()]), b);
    }

    private void addRows(TreeTableNode treeTableNode, List<Row> rows) {
        if (treeTableNode.getRow() != null)
            rows.add(treeTableNode.getRow());
        for (TreeTableNode node : treeTableNode.getChildren())
            addRows(node, rows);
    }

    public void setModelSupport(TreeModelSupport modelSupport) {
        this.modelSupport = modelSupport;
    }

    public TreeModelSupport getModelSupport() {
        return modelSupport;
    }
}

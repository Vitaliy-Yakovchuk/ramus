package com.ramussoft.gui.qualifier.table;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.CellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.JXTree.DelegatingRenderer;
import org.jdesktop.swingx.treetable.TreeTableModel;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowMover;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.attribute.icon.IconFactory;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.qualifier.table.event.SelectionEvent;
import com.ramussoft.gui.qualifier.table.event.SelectionListener;

public class RowTreeTable extends JXTreeTable implements ImportExport,
        ElementsTable {

    /**
     *
     */
    private static final long serialVersionUID = 5114610550724905038L;

    private static final String ROW_INDEXES = DataFlavor.javaJVMLocalObjectMimeType
            + ";class=java.util.ArrayList";

    private static final String ROWS = DataFlavor.javaJVMLocalObjectMimeType
            + ";class=com.ramussoft.gui.qualifier.table.Rows";

    public static DataFlavor localListFlavor;

    public static DataFlavor rowsListFlavor;

    private boolean exportRows;

    static {
        try {
            localListFlavor = new DataFlavor(ROW_INDEXES);
            rowsListFlavor = new DataFlavor(ROWS);
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected boolean exporting = false;

    private AccessRules accessRules;

    private RowSet rowSet;

    private boolean editIfNullEvent = true;

    private EventListenerList selectionListeners = new EventListenerList();

    private AttributePlugin[] plugins;

    private TreeTableNode lastFindIndex = null;

    private TableCellEditor[] cellEditors = null;

    private TableCellRenderer[] cellRenderers = null;

    private int editingRow;

    private int editingColumn;

    private ImageIcon leafIcon;

    private ImageIcon folderIcon;

    private ImageIcon folderSheetIcon;

    public final class TextTransfer implements ClipboardOwner {

        /**
         * Empty implementation of the ClipboardOwner interface.
         */
        public void lostOwnership(Clipboard aClipboard, Transferable aContents) {
            // do nothing
        }

        /**
         * Place a String on the clipboard, and make this class the owner of the
         * Clipboard's contents.
         */
        public void setClipboardContents(String aString) {
            StringSelection stringSelection = new StringSelection(aString);
            Clipboard clipboard = Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
            clipboard.setContents(stringSelection, this);
        }

    }

    public RowTreeTable(AccessRules accessRules, RowSet rowSet,
                        AttributePlugin[] plugins, GUIFramework framework,
                        RowTreeTableModel model) {
        setLeafIcon(new ImageIcon(getClass().getResource(
                "/com/ramussoft/gui/table/sheet.png")));
        setFolderIcon(new ImageIcon(getClass().getResource(
                "/com/ramussoft/gui/table/folder.png")));
        setFolderSheetIcon(new ImageIcon(getClass().getResource(
                "/com/ramussoft/gui/table/folder-sheet.png")));
        this.accessRules = accessRules;
        this.rowSet = rowSet;
        this.plugins = plugins;

        getInputMap(JInternalFrame.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "RamusENTER_Action");
        getInputMap(JInternalFrame.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "RamusENTER_Action");
        AbstractAction ramusEnterAction = new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 4745861738845278043L;

            public void actionPerformed(ActionEvent ae) {
                CellEditor editor = getCellEditor();
                if (editor != null)
                    editor.stopCellEditing();
            }
        };

        getActionMap().put("RamusENTER_Action", ramusEnterAction);

        this.getColumnModel().addColumnModelListener(
                new TableColumnModelListener() {

                    @Override
                    public void columnAdded(TableColumnModelEvent e) {
                    }

                    @Override
                    public void columnMarginChanged(ChangeEvent e) {
                    }

                    @Override
                    public void columnMoved(TableColumnModelEvent e) {
                    }

                    @Override
                    public void columnRemoved(TableColumnModelEvent e) {
                    }

                    @Override
                    public void columnSelectionChanged(ListSelectionEvent e) {
                        SelectionEvent event = new SelectionEvent(null, false);
                        for (SelectionListener listener : getSelectionListeners()) {
                            listener.changeSelection(event);
                        }
                    }

                });
        // setColumnSelectionAllowed(true);
        Attribute[] attributes = rowSet.getAttributes();
        cellEditors = new TableCellEditor[attributes.length];
        cellRenderers = new TableCellRenderer[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            cellEditors[i] = plugins[i].getTableCellEditor(rowSet.getEngine(),
                    accessRules, attributes[i]);
            cellRenderers[i] = plugins[i].getTableCellRenderer(
                    rowSet.getEngine(), accessRules, attributes[i]);
            if (cellEditors[i] == null) {
                cellEditors[i] = new DialogedTableCellEditor(
                        rowSet.getEngine(), accessRules, attributes[i],
                        plugins[i], framework);
                model.setSaveValue(i, false);
            }
        }

        setTreeTableModel(model);
        setHorizontalScrollEnabled(true);

        AbstractAction copy = new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = -7041684321033663566L;

            @Override
            public void actionPerformed(ActionEvent e) {
                new TextTransfer().setClipboardContents(getClipboardText());
            }

        };

        getActionMap().put("copyTableToClipboard", copy);
        getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK),
                "copyTableToClipboard");
        getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, KeyEvent.CTRL_MASK),
                "copyTableToClipboard");

        setShowGrid(false, true);
        setRowSelectionAllowed(true);
    }

    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle,
                                boolean extend) {
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
        SelectionEvent event = new SelectionEvent(null, false);
        for (SelectionListener listener : getSelectionListeners()) {
            listener.changeSelection(event);
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        int aColumn = convertColumnIndexToModel(column);
        if (!plugins[aColumn].isCellEditable())
            return false;
        return super.isCellEditable(row, column);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        editingRow = row;
        editingColumn = column;
        int aColumn = convertColumnIndexToModel(column);
        TableCellEditor e = cellEditors[aColumn];
        if ((e != null) && (aColumn == 0)) {
            return new FirstRowCellEditor(e, (JTree) getCellRenderer(row,
                    column), this);
        }
        return (e == null) ? super.getCellEditor(row, column) : e;
    }

    TreeCellRenderer x;

    private boolean findSelectionChanged = false;

    @Override
    public TableCellRenderer getCellRenderer(final int row, final int column) {
        int aColumn = convertColumnIndexToModel(column);
        final TableCellRenderer r = cellRenderers[aColumn];

        if (aColumn == 0) {
            JTree tree = (JTree) super.getCellRenderer(row, column);
            DelegatingRenderer rend = ((DelegatingRenderer) tree
                    .getCellRenderer());

            TreePath pathForRow = tree
                    .getPathForRow(convertRowIndexToModel(row));
            if (pathForRow == null)
                return super.getCellRenderer(row, column);
            Object object = pathForRow.getLastPathComponent();
            boolean setDefault = true;
            Row row2 = null;
            if (object instanceof TreeTableNode) {
                row2 = ((TreeTableNode) object).getRow();
            }
            if (row2 != null) {
                Hashtable<Long, ImageIcon> icons = IconFactory.getIcons(rowSet
                        .getEngine());
                Icon icon = getDefaultIcon(row2);
                if (icon == null && row2.getHierarchicalPersistent() != null)
                    icon = icons.get(row2.getHierarchicalPersistent()
                            .getIconId());
                setDefault = false;

                if (icon != null) {
                    rend.setLeafIcon(icon);
                    rend.setOpenIcon(icon);
                    rend.setClosedIcon(icon);
                } else {
                    ImageIcon l = IconFactory.getLeafIcons(rowSet.getEngine())
                            .get(row2.getElement().getQualifierId());
                    ImageIcon o = IconFactory.getOpenIcons(rowSet.getEngine())
                            .get(row2.getElement().getQualifierId());
                    ImageIcon c = IconFactory
                            .getClosedIcons(rowSet.getEngine()).get(
                                    row2.getElement().getQualifierId());
                    rend.setLeafIcon((l == null) ? getLeafIcon() : l);
                    rend.setOpenIcon((o == null) ? getFolderIcon() : o);
                    rend.setClosedIcon((c == null) ? getFolderSheetIcon() : c);
                }
            }

            if (setDefault) {
                rend.setLeafIcon(getLeafIcon());
                rend.setOpenIcon(getFolderIcon());
                rend.setClosedIcon(getFolderSheetIcon());
            }
        }

        if (aColumn == 0)
            return super.getCellRenderer(row, column);

        if ((r != null) && (aColumn == 0)) {
            // DefaultTreeCellRenderer
            JTree tree = (JTree) super.getCellRenderer(row, column);
            if (x == null) {
                x = new TreeCellRenderer() {

                    @Override
                    public Component getTreeCellRendererComponent(JTree tree,
                                                                  Object value, boolean selected, boolean expanded,
                                                                  boolean leaf, int aRow, boolean hasFocus) {
                        return r.getTableCellRendererComponent(
                                RowTreeTable.this, getValueAt(aRow, column),
                                selected, hasFocus, aRow, 0);
                    }

                };
                tree.setCellRenderer(x);
            }
            return (TableCellRenderer) tree;
        }

        return (r == null) ? super.getCellRenderer(row, column) : r;
    }

    protected Icon getDefaultIcon(Row row) {
        return null;
    }

    @Override
    public boolean isHierarchical(int column) {
        return convertColumnIndexToModel(column) == 0;
    }

    public TableCellRenderer[] getCellRenderers() {
        return cellRenderers;
    }

    public Transferable createTransferable() {
        exporting = true;
        final int[] is = getSelectedRows();
        final ArrayList<Integer> al = new ArrayList<Integer>();
        Rows rows = new Rows();
        for (final int i : is) {
            al.add(i);
            TreeTableNode node = (TreeTableNode) getPathForRow(i)
                    .getLastPathComponent();
            if ((node != null) && (node.getRow() != null))
                rows.add(node.getRow());
        }
        return new ArrayTransferable(al, rows);
    }

    public boolean importData(final Transferable t) {
        final DropLocation l = getDropLocation();
        return importData(t, !l.isInsertRow(), l.getRow());
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        TableCellEditor editor = getCellEditor();
        if (editor == null)
            super.processMouseEvent(e);
        else {
            int row = editingRow;
            int column = editingColumn;
            if ((row >= 0) && (column >= 0)) {
                if (!getCellRect(row, column, true).contains(e.getPoint())) {
                    super.processMouseEvent(e);
                } else {
                }
            } else
                super.processMouseEvent(e);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean importData(final Transferable t, final boolean on,
                              final int index) {
        if (index < 0)
            return false;
        if (canImport(t.getTransferDataFlavors())) {
            try {
                final ArrayList<Integer> list = (ArrayList<Integer>) t
                        .getTransferData(localListFlavor);
                final int[] sels = new int[list.size()];
                for (int i = 0; i < sels.length; i++)
                    sels[i] = list.get(i);
                final RowMover mover = getRowMover(sels);
                try {
                    if (on)
                        mover.on(index);
                    else
                        mover.insert(index);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
                return true;
            } catch (final UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void setSelectedRows(int[] rows, final boolean scrollToSel) {
        final ListSelectionModel model = getSelectionModel();
        int i = -1;
        for (int j : rows) {
            i = j;
            model.addSelectionInterval(i, i);
        }
        if (scrollToSel)
            scrollRowToVisible(i);
    }

    public int[] getExpandedRows() {
        final ArrayList<Integer> list = getExpandes();
        int res[] = new int[list.size()];
        for (int i = 0; i < res.length; i++)
            res[i] = list.get(i);
        return res;
    }

    public void setExpandedRows(int[] rows) {
        for (int row : rows) {
            expandRow(row);
        }
    }

    private ArrayList<Integer> getExpandes() {
        final int l = getRowCount();
        final ArrayList<Integer> res = new ArrayList<Integer>();
        for (int i = 0; i < l; i++) {
            if (isExpanded(i))
                res.add(i);
        }
        return res;
    }

    private RowMover getRowMover(final int[] sels) {
        final Row[] group = getShownRows();
        final RowMover mover = new RowMover(rowSet.getRoot(), group, sels,
                accessRules, rowSet);
        return mover;
    }

    private Row[] getShownRows() {
        final Row[] group = new Row[getRowCount()];
        for (int i = 0; i < group.length; i++)
            group[i] = ((RowNode) getPathForRow(i).getLastPathComponent())
                    .getRow();
        return group;
    }

    public TreeTableNode getSelectedNode() {
        int i = getSelectedRow();
        if (i < 0)
            return null;
        TreePath row = getPathForRow(i);
        if (row == null)
            return null;
        return (TreeTableNode) row.getLastPathComponent();
    }

    public boolean canImport(final DataFlavor[] flavors) {
        if (exporting)
            for (final DataFlavor element : flavors) {
                if (localListFlavor.equals(element)) {
                    return true;
                }
            }
        return false;
    }

    public void exportDone(Transferable data, int action) {
        exporting = false;
    }

    public class ArrayTransferable implements Transferable {
        ArrayList<Integer> data;

        private Rows rows;

        public ArrayTransferable(final ArrayList<Integer> alist, Rows rows) {
            this.data = alist;
            this.rows = rows;
        }

        public Object getTransferData(final DataFlavor flavor)
                throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            if (localListFlavor.equals(flavor))
                return data;
            if (rowsListFlavor.equals(flavor))
                return rows;
            return rows;
        }

        public DataFlavor[] getTransferDataFlavors() {
            if (exportRows)
                return new DataFlavor[]{localListFlavor, rowsListFlavor};
            return new DataFlavor[]{localListFlavor, rowsListFlavor};
        }

        public boolean isDataFlavorSupported(final DataFlavor flavor) {
            if ((localListFlavor.equals(flavor))
                    || (rowsListFlavor.equals(flavor)))
                return true;

            return false;
        }
    }

    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        if ((e != null) && (!isEditIfNullEvent())) {
            return false;
        }
        boolean res = super.editCellAt(row, column, e);
        if ((res) && (cellEditor instanceof DefaultCellEditor)) {
            ((DefaultCellEditor) cellEditor).getComponent().requestFocus();
        }
        return res;
    }

    /**
     * @param editIfNullEvent the editIfNullEvent to set
     */
    public void setEditIfNullEvent(boolean editIfNullEvent) {
        this.editIfNullEvent = editIfNullEvent;
    }

    /**
     * @return the editIfNullEvent
     */
    public boolean isEditIfNullEvent() {
        return editIfNullEvent;
    }

    public void addSelectionListener(SelectionListener listener) {
        selectionListeners.add(SelectionListener.class, listener);
    }

    public void removeSelectionListener(SelectionListener listener) {
        selectionListeners.remove(SelectionListener.class, listener);
    }

    public SelectionListener[] getSelectionListeners() {
        return selectionListeners.getListeners(SelectionListener.class);
    }

    public boolean find(final String startName, final boolean wordsOrder) {
        lastFindIndex = null;
        return findNext(startName, wordsOrder);
    }

    public boolean findNext(final String startName, final boolean wordsOrder) {
        lastFindIndex = findNextRow(startName, wordsOrder);
        if (lastFindIndex != null) {
            TreeTableNode parent = lastFindIndex.getParent();
            List<TreeTableNode> tree = new ArrayList<TreeTableNode>();
            while (parent != null) {
                tree.add(0, parent);
                parent = parent.getParent();
            }
            int index = 0;
            for (int i = 1; i < tree.size(); i++) {
                index = indexOfNode(tree.get(i));
                if (!isExpanded(index))
                    expandRow(index);
            }
            index = indexOfNode(lastFindIndex);
            try {
                this.findSelectionChanged = true;
                changeSelection(index, convertColumnIndexToView(0), false,
                        false);
            } finally {
                this.findSelectionChanged = false;
            }
            return true;
        } else
            return false;
    }

    private int indexOfNode(final TreeTableNode treeTableNode) {
        for (int i = 0; i < getRowCount(); i++) {
            if (getPathForRow(i).getLastPathComponent() == treeTableNode)
                return i;
        }
        return -1;
    }

    public int indexOfRow(Row row) {
        for (int i = 0; i < getRowCount(); i++) {
            if (row.equals(((TreeTableNode) getPathForRow(i)
                    .getLastPathComponent()).getRow()))
                return i;
        }
        return -1;
    }

    private TreeTableNode findNextRow(final String startName,
                                      final boolean wordsOrder) {
        final TreeTableModel model = getTreeTableModel();
        final int cc = getColumnCount();
        final TreeTableNode node = lastFindIndex;
        nextNode();
        if (node == null && lastFindIndex == null)
            return null;
        do {
            if (findRow(startName, wordsOrder, cc, model, lastFindIndex))
                return lastFindIndex;
            if (node == lastFindIndex)
                return null;
            nextNode();
        } while (true);
    }

    private boolean findRow(final String startName, final boolean wordsOrder,
                            final int cc, final TreeTableModel model, final TreeTableNode node) {
        if (node == null)
            return false;
        if (node.getParent() == null)
            return false;
        for (int j = 0; j < cc; j++) {
            final Object o = model.getValueAt(node, j);
            if (o != null)
                if (isStartSame(o.toString(), startName, wordsOrder))
                    return true;
        }
        return false;
    }

    private TreeTableNode nextNode() {
        if (lastFindIndex == null) {
            lastFindIndex = (TreeTableNode) getTreeTableModel().getRoot();
        } else {
            if (lastFindIndex.getChildCount() > 0)
                lastFindIndex = (TreeTableNode) lastFindIndex.getChild(0);
            else {
                do {
                    final TreeTableNode parent = lastFindIndex.getParent();
                    if (parent == null) {
                        lastFindIndex = null;
                        break;
                    }
                    final int index = parent.getIndexOfChild(lastFindIndex);
                    if (index < parent.getChildCount() - 1) {
                        lastFindIndex = (TreeTableNode) parent
                                .getChild(index + 1);
                        break;
                    } else
                        lastFindIndex = parent;
                } while (true);

            }
        }
        return lastFindIndex;
    }

    public static boolean isNameStartFrom(final Row row, final String text) {
        final String name = row.getName();
        return name.length() >= text.length()
                && name.substring(0, text.length()).equals(text);
    }

    public static boolean isStartSame(final Row row, final String startName,
                                      final boolean wordsOrder) {
        return isStartSame(row.getName(), startName, wordsOrder);
    }

    public static boolean isStartSame(final String name,
                                      final String startName, final boolean wordsOrder) {
        if (name == null)
            return false;
        String lowerCaseName = name.toLowerCase();
        String lowerCaseStartName = startName.toLowerCase();

        if (wordsOrder) {
            return lowerCaseName.indexOf(lowerCaseStartName) >= 0;
        } else {
            final StringTokenizer stm = new StringTokenizer(lowerCaseName,
                    "\"+-*/=\'` \t\n\r\f");
            final StringTokenizer st = new StringTokenizer(lowerCaseStartName,
                    "\"+-*/=\'` \t\n\r\f");
            final ArrayList<String> a = new ArrayList<String>();
            final ArrayList<String> b = new ArrayList<String>();

            while (stm.hasMoreElements())
                b.add(stm.nextToken());

            while (st.hasMoreElements())
                a.add(st.nextToken());

            for (int i = 0; i < a.size(); i++) {
                final String t = a.get(i);
                boolean bo = false;
                for (int j = 0; j < b.size(); j++) {
                    final String t1 = b.get(j);
                    if (t1.length() >= t.length()) {
                        if (t1.substring(0, t.length()).compareTo(t) == 0) {
                            bo = true;
                            break;
                        }
                    }
                }
                if (!bo) {
                    return false;
                }
            }
            return true;
        }
    }

    public Element getElementForRow(int row) {
        return ((TreeTableNode) getPathForRow(row).getLastPathComponent())
                .getRow().getElement();
    }

    public String getClipboardText() {
        final int[] rows = getSelectedRows();
        final StringBuffer sb = new StringBuffer();
        final int cc = getColumnCount();
        for (final int i : rows) {
            for (int c = 0; c < cc; c++) {
                final Object o = getValueAt(i, c);
                if (o != null)
                    sb.append(o.toString());
                if (c < cc - 1)
                    sb.append('\t');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public RowSet getRowSet() {
        return rowSet;
    }

    /**
     * @param leafIcon the leafIcon to set
     */
    public void setLeafIcon(ImageIcon leafIcon) {
        this.leafIcon = leafIcon;
    }

    /**
     * @return the leafIcon
     */
    public ImageIcon getLeafIcon() {
        return leafIcon;
    }

    /**
     * @param folderIcon the folderIcon to set
     */
    public void setFolderIcon(ImageIcon folderIcon) {
        this.folderIcon = folderIcon;
    }

    /**
     * @return the folderIcon
     */
    public ImageIcon getFolderIcon() {
        return folderIcon;
    }

    /**
     * @param folderSheetIcon the folderSheetIcon to set
     */
    public void setFolderSheetIcon(ImageIcon folderSheetIcon) {
        this.folderSheetIcon = folderSheetIcon;
    }

    /**
     * @return the folderSheetIcon
     */
    public ImageIcon getFolderSheetIcon() {
        return folderSheetIcon;
    }

    public String getBodyText(String tableName) {
        StringBuffer sb = new StringBuffer();
        sb.append("<h1>" + tableName + "</h1>\n<hr>\n");
        sb.append("<table>\n");

        RowTreeTableModel model = (RowTreeTableModel) getTreeTableModel();

        sb.append("<tr>");
        sb.append("<td><b><i>");
        sb.append(GlobalResourcesManager.getString("Code"));
        sb.append("</i></b></td>\n");

        for (int j = 0; j < getColumnCount(); j++) {
            int column = convertColumnIndexToModel(j);
            sb.append("<td><b>");
            String name = getModel().getColumnName(column);
            if (name != null) {

                if (name.startsWith("<html><body><center>"))
                    name = name.substring("<html><body><center>".length(),
                            name.length() - "</center></body></html>".length());
                sb.append(name);
            }
            sb.append("</b></td>\n");
        }
        sb.append("</tr>\n");

        for (int i = 0; i < getRowCount(); i++) {
            int row = convertRowIndexToModel(i);
            sb.append("<tr>");
            sb.append("<td>");
            TreeTableNode node = (TreeTableNode) getPathForRow(row)
                    .getLastPathComponent();
            Row row2 = node.getRow();
            if (row2 != null)
                sb.append("<i>" + row2.getCode() + "</i>");
            sb.append("</td>");
            for (int j = 0; j < getColumnCount(); j++) {
                boolean rightAlign = false;
                int column = convertColumnIndexToModel(j);
                TableCellRenderer renderer = getCellRenderer(row, column);
                String value;
                if (renderer instanceof JLabel) {
                    if (((JLabel) renderer).getHorizontalAlignment() == SwingConstants.RIGHT)
                        rightAlign = true;
                }
                Object object = model.getValueAt(node, column);
                if (object == null)
                    value = null;
                else
                    value = object.toString();

                if (rightAlign)
                    sb.append("<td align=\"right\">");
                else
                    sb.append("<td>");
                if (value != null)
                    sb.append(value);
                sb.append("</td>\n");
            }
            sb.append("</tr>\n");
        }
        sb.append("</table>\n");
        return sb.toString();
    }

    public boolean isFindSelectionChanged() {
        return findSelectionChanged;
    }

    /**
     * @param exportRows the exportRows to set
     */
    public void setExportRows(boolean exportRows) {
        this.exportRows = exportRows;
    }

    /**
     * @return the exportRows
     */
    public boolean isExportRows() {
        return exportRows;
    }

    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new HTMLTableHeader(columnModel);
    }

    public TableCellEditor[] getCellEditors() {
        return cellEditors;
    }
}

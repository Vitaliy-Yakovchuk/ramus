package com.ramussoft.gui.elist;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Qualifier;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.attribute.icon.IconFactory;
import com.ramussoft.gui.common.GUIFramework;

public class ElistTablePanel extends JTree {

    /**
     *
     */
    private static final long serialVersionUID = 3740062936548554691L;

    private RowSet rowSet;

    private Attribute attribute;

    private DefaultTreeModel model;

    private ImageIcon leafIcon;

    private ImageIcon folderIcon;

    private ImageIcon folderSheetIcon;

    private DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
        /**
         *
         */
        private static final long serialVersionUID = 2404450396669363028L;

        public java.awt.Component getTreeCellRendererComponent(JTree tree,
                                                               Object value, boolean sel, boolean expanded, boolean leaf,
                                                               int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                    leaf, row, hasFocus);
            Hashtable<Long, ImageIcon> icons = IconFactory.getIcons(rowSet
                    .getEngine());
            ImageIcon icon = null;
            Row row2 = (Row) value;
            if (row2.getHierarchicalPersistent() != null)
                icon = icons.get(row2.getHierarchicalPersistent().getIconId());

            if (icon != null) {
                setLeafIcon(icon);
                setOpenIcon(icon);
                setClosedIcon(icon);
            } else {
                ImageIcon l = IconFactory.getLeafIcons(rowSet.getEngine()).get(
                        row2.getElement().getQualifierId());
                ImageIcon o = IconFactory.getOpenIcons(rowSet.getEngine()).get(
                        row2.getElement().getQualifierId());
                ImageIcon c = IconFactory.getClosedIcons(rowSet.getEngine())
                        .get(row2.getElement().getQualifierId());
                setLeafIcon((l == null) ? ElistTablePanel.this.leafIcon : l);
                setOpenIcon((o == null) ? ElistTablePanel.this.folderIcon : o);
                setClosedIcon((c == null) ? ElistTablePanel.this.folderSheetIcon
                        : c);
            }
            return this;
        }

        ;
    };

    public ElistTablePanel(GUIFramework framework, Qualifier qualifier) {
        leafIcon = new ImageIcon(getClass().getResource(
                "/com/ramussoft/gui/table/sheet.png"));
        folderIcon = new ImageIcon(getClass().getResource(
                "/com/ramussoft/gui/table/folder.png"));
        folderSheetIcon = new ImageIcon(getClass().getResource(
                "/com/ramussoft/gui/table/folder-sheet.png"));
        setRootVisible(false);
        List<Attribute> list = qualifier.getAttributes();
        if (list.size() > 0)
            attribute = list.get(0);
        for (Attribute attr : list) {
            if (attr.getId() == qualifier.getAttributeForName()) {
                attribute = attr;
                break;
            }
        }
        Attribute[] attributes;
        if (attribute != null)
            attributes = new Attribute[]{attribute};
        else
            attributes = new Attribute[]{};
        rowSet = new RowSet(framework.getEngine(), qualifier, attributes) {

            @Override
            protected void removedFromChildren(Row parentRow, Row row, int i) {
                try {
                    model.nodesWereRemoved(parentRow, new int[]{i},
                            new Object[]{row});
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void added(Row parent, final Row row, int index) {
                super.added(parent, row, index);
                if (parent == null)
                    parent = getRoot();
                try {
                    model.nodesWereInserted(parent, new int[]{index});
                    expandPath(new TreePath(model.getPathToRoot(parent)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void attributeChanged(Row row, Attribute attr,
                                            Object newValue, Object oldValue, boolean journaled) {
                if (attribute.getId() == attr.getId()) {
                    try {
                        model.nodeChanged(row);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        model = new DefaultTreeModel(rowSet.getRoot());
        setModel(model);
        for (int i = 0; i < getRowCount(); i++)
            expandRow(i);
        setRowHeight(ElistTableTabView.CELL_BORDER);
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                setToolTipText(getToolTipText(e));
            }
        });

        setCellRenderer(renderer);
    }

    public Element getElement(int row) {
        return ((Row) getPathForRow(row).getLastPathComponent()).getElement();
    }

    public void paintMe(Graphics g) {
        paintComponent(g);
    }

    public String getToolTipText(int x, int y) {
        TreePath pathForLocation = getPathForLocation(x, y);
        if (pathForLocation == null)
            return null;
        return pathForLocation.getLastPathComponent().toString();
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        return getToolTipText(event.getX(), event.getY());
    }

    public void close() {
        rowSet.close();
    }

    public Qualifier getQualifier() {
        return rowSet.getQualifier();
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        // super.processMouseEvent(e);
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        // super.processKeyEvent(e);
    }

}

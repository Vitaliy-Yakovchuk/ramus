package com.ramussoft.gui.qualifier.select;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.JXTree.DelegatingRenderer;

public class QualifierTable extends JXTreeTable {

    /**
     *
     */
    private static final long serialVersionUID = 78357925573817284L;

    private ImageIcon leafIcon = new ImageIcon(getClass().getResource(
            "/com/ramussoft/gui/table/qualifier.png"));

    private ImageIcon folderIcon = new ImageIcon(getClass().getResource(
            "/com/ramussoft/gui/table/folder.png"));

    private ImageIcon folderSheetIcon = new ImageIcon(getClass().getResource(
            "/com/ramussoft/gui/table/folder-sheet.png"));

    @Override
    public TableCellRenderer getCellRenderer(final int row, final int column) {

        JTree tree = (JTree) super.getCellRenderer(row, column);
        DelegatingRenderer rend = ((DelegatingRenderer) tree.getCellRenderer());

        rend.setLeafIcon(leafIcon);
        rend.setOpenIcon(folderIcon);
        rend.setClosedIcon(folderSheetIcon);

        return (TableCellRenderer) tree;
    }

}

package com.ramussoft.report.editor.xml;

import javax.swing.table.TableCellEditor;

import org.jdesktop.swingx.JXTable;

import com.ramussoft.gui.common.GUIFramework;

public class AttributesEditorTable extends JXTable {

    /**
     *
     */
    private static final long serialVersionUID = -2036692426001867041L;

    private TableCellEditorFactory cellFactory;

    public AttributesEditorTable(GUIFramework framework) {
        cellFactory = new TableCellEditorFactory(framework);
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        AttributesEditorModel model = (AttributesEditorModel) getModel();
        Attribute attribute = model.getAttribute(convertRowIndexToModel(row));
        return cellFactory.getCellEditor(attribute.getType());
    }
}

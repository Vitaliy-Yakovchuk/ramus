package com.ramussoft.gui.common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.table.TableCellEditor;

public class AttributeCellEditorToAttributeEditor extends
        AbstractAttributeEditor {

    public AttributeCellEditorToAttributeEditor(TableCellEditor editor) {
        this.editor = editor;
    }

    private TableCellEditor editor;

    private JPanel panel1 = new JPanel(new FlowLayout());

    @Override
    public JComponent getComponent() {
        return panel1;
    }

    @Override
    public Object getValue() {
        editor.stopCellEditing();
        return editor.getCellEditorValue();
    }

    @Override
    public Object setValue(Object value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(300, 50));
        panel1.removeAll();
        panel.add(editor.getTableCellEditorComponent(null, value, false, 0, 0),
                BorderLayout.NORTH);
        panel1.add(panel);
        panel1.revalidate();
        panel1.repaint();
        return value;
    }
}

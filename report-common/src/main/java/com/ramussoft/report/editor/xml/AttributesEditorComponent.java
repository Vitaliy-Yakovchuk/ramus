package com.ramussoft.report.editor.xml;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.report.editor.XMLReportEditorView;
import com.ramussoft.report.editor.xml.components.XMLComponent;

public class AttributesEditorComponent extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 4143382673490503794L;

    private AttributesEditorModel model;

    private AttributesEditorTable table;

    public AttributesEditorComponent(GUIFramework framework) {
        super(new BorderLayout());
        model = new AttributesEditorModel();
        table = new AttributesEditorTable(framework);
        table.setModel(model);
        JScrollPane pane = new JScrollPane(table);
        this.add(pane, BorderLayout.CENTER);
    }

    public void setAttributes(List<XMLComponent> list,
                              XMLReportEditorView reportEditor) {
        if (table.getCellEditor() != null)
            table.getCellEditor().stopCellEditing();
        model.setAttributes(list, reportEditor);
    }

}

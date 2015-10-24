package com.ramussoft.report.editor.xml;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.report.ReportResourceManager;
import com.ramussoft.report.editor.XMLReportEditorView;
import com.ramussoft.report.editor.xml.components.XMLComponent;

public class AttributesEditorModel extends AbstractTableModel {

    /**
     *
     */
    private static final long serialVersionUID = 4113989574122522087L;

    private List<Attribute> attributes = new ArrayList<Attribute>(0);

    private List<XMLComponent> components;

    private ReportEditor reportEditor;

    private XMLReportEditorView editorView;

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0)
            return GlobalResourcesManager.getString("OtherElement.Attribute");
        return GlobalResourcesManager.getString("Attribute.Value");
    }

    @Override
    public int getRowCount() {
        return attributes.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Attribute attribute = attributes.get(rowIndex);
        if (columnIndex == 0)
            return attribute.getTitle();
        Object value = attribute.getValue();
        if ("[ALL MODELS]".equals(value))
            return ReportResourceManager.getString("AllModels");
        return value;
    }

    public void setAttributes(List<XMLComponent> components,
                              XMLReportEditorView editorView) {
        this.components = components;
        this.reportEditor = editorView.getEditorView().getReportEditor();
        this.editorView = editorView;

        attributes = new ArrayList<Attribute>();

        for (XMLComponent component : this.components) {
            for (Attribute attribute : component.getXMLAttributes()) {
                if (attributes.indexOf(attribute) < 0) {
                    attributes.add(attribute);
                }
            }
        }

        fireTableDataChanged();
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        Attribute attribute = attributes.get(rowIndex);
        boolean set = false;
        for (XMLComponent component : components) {
            List<Attribute> list = component.getXMLAttributes();
            int i = list.indexOf(attribute);
            if (i >= 0) {
                component.setXMLAttribute(list.get(i), aValue);
                set = true;
            }
        }
        if (set)
            editorView.save();
        reportEditor.repaint();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex > 0;
    }

    public Attribute getAttribute(int rowIndex) {
        return attributes.get(rowIndex);
    }

}

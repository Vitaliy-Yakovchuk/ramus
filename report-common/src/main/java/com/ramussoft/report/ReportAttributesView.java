package com.ramussoft.report;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;

import com.ramussoft.gui.common.AbstractUniqueView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.report.editor.XMLReportEditorView;
import com.ramussoft.report.editor.xml.AttributesEditorComponent;
import com.ramussoft.report.editor.xml.components.XMLComponent;

public class ReportAttributesView extends AbstractUniqueView implements
        UniqueView {

    public static final String ACTIVE_REPORT_XML_COMPONENT = "ActiveReportXMLComponent";

    private AttributesEditorComponent component;

    public ReportAttributesView(GUIFramework framework) {
        super(framework);
        framework.addActionListener(ACTIVE_REPORT_XML_COMPONENT,
                new ActionListener() {

                    @SuppressWarnings("unchecked")
                    @Override
                    public void onAction(ActionEvent event) {
                        component.setAttributes(
                                (List<XMLComponent>) event.getValue(),
                                (XMLReportEditorView) event.getMetadata());
                    }
                });
    }

    @Override
    public String getDefaultWorkspace() {
        return "workspace.reportEditor";
    }

    @Override
    public String getId() {
        return "AttributesList";
    }

    @Override
    public JComponent createComponent() {
        component = new AttributesEditorComponent(framework);
        return component;
    }

    @Override
    public Action[] getActions() {
        return new Action[]{};
    }

    @Override
    public String getDefaultPosition() {
        return BorderLayout.SOUTH;
    }
}

package com.ramussoft.report.editor;

import java.awt.BorderLayout;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;

import org.xml.sax.SAXException;

import com.ramussoft.common.Element;
import com.ramussoft.common.event.StreamAdapter;
import com.ramussoft.common.event.StreamEvent;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.qualifier.Commands;
import com.ramussoft.report.ReportPlugin;

public class XMLReportEditorView extends ReportEditorView {

    private XMLReportView editorView;

    private Element element;

    private boolean disableUpdates = false;

    private StreamAdapter streamAdapter;

    private ActionListener fullRefreshAction;

    public XMLReportEditorView(GUIFramework framework, Element element) {
        super(framework, element);
        editorView = new XMLReportView(framework, this);
        this.element = element;
        try {
            load();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(framework.getMainFrame(), e
                    .getLocalizedMessage());
        }
        streamAdapter = new StreamAdapter() {

            @Override
            public void streamUpdated(StreamEvent event) {
                if (!disableUpdates)
                    if (event.getPath().equals(getXMLReportPath()))
                        try {
                            load();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (SAXException e) {
                            e.printStackTrace();
                        }
            }

        };
        framework.getEngine().addStreamListener(streamAdapter);
        framework.addActionListener(Commands.FULL_REFRESH, fullRefreshAction = new ActionListener() {

            @Override
            public void onAction(ActionEvent event) {
                try {
                    load();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void load() throws IOException, SAXException {
        byte[] bytes = framework.getEngine().getStream(getXMLReportPath());
        if ((bytes != null) && (bytes.length > 0))
            editorView.loadFromStream(new ByteArrayInputStream(bytes), false);
        else
            editorView.clear();
    }

    @Override
    public JComponent createComponent() {
        JComponent createComponent = super.createComponent();
        content.add(editorView, BorderLayout.CENTER);
        activeView = editorView;
        return createComponent;
    }

    @Override
    protected void createButtons(ButtonGroup group) {
        JToggleButton button1 = createOpenViewButton(group, editorView);
        button1.setSelected(true);
        buttonsPanel.add(button1);
        buttonsPanel.add(createOpenViewButton(group, queryView));
        super.createButtons(group);
    }

    public void save() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            disableUpdates = true;
            editorView.saveReportToStream(stream);
            ((Journaled) framework.getEngine()).startUserTransaction();
            framework.getEngine().setUndoableStream(getXMLReportPath(),
                    stream.toByteArray());
            ((Journaled) framework.getEngine()).commitUserTransaction();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(framework.getMainFrame(), e
                    .getLocalizedMessage());
            e.printStackTrace();
        } finally {
            disableUpdates = false;
        }

    }

    private String getXMLReportPath() {
        return "/elements/"
                + element.getId()
                + "/"
                + ReportPlugin.getReportNameAttribute(framework.getEngine())
                .getId() + "/report.1.xml";
    }

    @Override
    public void close() {
        super.close();
        framework.getEngine().removeStreamListener(streamAdapter);
        framework.removeActionListener(Commands.FULL_REFRESH, fullRefreshAction);
    }

    public XMLReportView getEditorView() {
        return editorView;
    }
}

package com.ramussoft.report.editor;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import org.xml.sax.SAXException;

import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.report.ReportAttributesView;
import com.ramussoft.report.ReportResourceManager;
import com.ramussoft.report.editor.xml.ComponentLoader;
import com.ramussoft.report.editor.xml.ReportEditor;
import com.ramussoft.report.editor.xml.XMLDiagram;
import com.ramussoft.report.editor.xml.components.Report;
import com.ramussoft.reportgef.Component;

public class XMLReportView extends SubView {

    /**
     *
     */
    private static final long serialVersionUID = -8082224575689573294L;

    private ReportEditor reportEditor;

    private Engine engine;

    public XMLReportView(final GUIFramework framework,
                         final XMLReportEditorView xmlReportEditorView) {
        this.engine = framework.getEngine();

        XMLDiagram diagram = new XMLDiagram();
        reportEditor = new ReportEditor(diagram) {
            /**
             *
             */
            private static final long serialVersionUID = 3512843810158790233L;

            private FileFilter fileFilter = new FileFilter() {

                @Override
                public boolean accept(File f) {
                    if (f.isFile()) {
                        if (f.getName().toLowerCase().endsWith(".xml"))
                            return true;
                        else
                            return false;
                    }
                    return true;
                }

                @Override
                public String getDescription() {
                    return "*.xml";
                }

            };

            @SuppressWarnings("unchecked")
            @Override
            protected void selectionChanged() {
                super.selectionChanged();
                Component[] components = getSelectedComponents();
                List list = Arrays.asList(components);
                if (list.size() == 0) {
                    list = new ArrayList(1);
                    list.add(reportEditor.getReport());
                }
                framework.propertyChanged(
                        ReportAttributesView.ACTIVE_REPORT_XML_COMPONENT, list,
                        xmlReportEditorView);
            }

            @Override
            public void postChanged() {
                super.postChanged();
                xmlReportEditorView.save();
            }

            @Override
            public void exportToXML() {
                JFileChooser fileChooser = new JFileChooser() {

                    /**
                     *
                     */
                    private static final long serialVersionUID = -1905243119079555911L;

                    @Override
                    public void approveSelection() {
                        if (getSelectedFile().exists()) {
                            if (JOptionPane
                                    .showConfirmDialog(
                                            framework.getMainFrame(),
                                            GlobalResourcesManager
                                                    .getString("File.Exists"),
                                            UIManager
                                                    .getString("OptionPane.messageDialogTitle"),
                                            JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                                return;
                        }
                        super.approveSelection();
                    }
                };
                fileChooser.setFileFilter(fileFilter);
                String file = Options.getString("XML_EXPORT_FILE");
                if (file != null)
                    fileChooser.setSelectedFile(new File(file));
                if (fileChooser.showSaveDialog(framework.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
                    FileOutputStream stream;
                    try {
                        File selectedFile = fileChooser.getSelectedFile();
                        if (!selectedFile.getName().toLowerCase().endsWith(
                                ".xml")) {
                            selectedFile = new File(selectedFile
                                    .getParentFile(), selectedFile.getName()
                                    + ".xml");
                        }
                        Options.setString("XML_EXPORT_FILE", selectedFile
                                .getAbsolutePath());
                        stream = new FileOutputStream(selectedFile);

                        saveToStream(stream);
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(framework.getMainFrame(),
                                e.getLocalizedMessage());
                    }
                }
            }

            @Override
            public void importFromXML() {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(fileFilter);
                String file = Options.getString("XML_EXPORT_FILE");
                if (file != null)
                    fileChooser.setSelectedFile(new File(file));
                if (fileChooser.showOpenDialog(framework.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
                    FileInputStream stream;
                    try {
                        File selectedFile = fileChooser.getSelectedFile();
                        Options.setString("XML_EXPORT_FILE", selectedFile
                                .getAbsolutePath());
                        stream = new FileInputStream(selectedFile);

                        loadFromStream(stream, true);
                        stream.close();
                        xmlReportEditorView.save();
                    } catch (Exception e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(framework.getMainFrame(),
                                e.getLocalizedMessage());
                    }
                }
            }

        };
        reportEditor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (reportEditor.getSelection().getBounds().length == 0) {
                    List<Report> list = new ArrayList<Report>(1);
                    list.add(reportEditor.getReport());
                    framework.propertyChanged(
                            ReportAttributesView.ACTIVE_REPORT_XML_COMPONENT,
                            list, xmlReportEditorView);
                }

            }
        });

        JPopupMenu menu = new JPopupMenu();

        for (Action action : getActions()) {
            if (action == null)
                menu.addSeparator();
            else {
                JMenuItem item = menu.add(action);
                item.setText(ReportResourceManager.getString((String) action
                        .getValue(Action.ACTION_COMMAND_KEY)));
            }
        }

        reportEditor.setComponentPopupMenu(menu);

        JScrollPane pane = new JScrollPane(reportEditor);
        pane.getVerticalScrollBar().setUnitIncrement(20);
        this.add(pane, BorderLayout.CENTER);
    }

    @Override
    public Action[] getActions() {
        return reportEditor.getActions();
    }

    @Override
    public String getTitle() {
        return ReportResourceManager.getString("XMLReportEditorView.title");
    }

    public void saveReportToStream(ByteArrayOutputStream stream)
            throws IOException {
        reportEditor.saveToStream(stream);
    }

    public void loadFromStream(InputStream stream, boolean ignoreBaseQualifier)
            throws IOException, SAXException {
        ComponentLoader loader = new ComponentLoader(engine,
                ignoreBaseQualifier);
        loader.parse(reportEditor, stream);
        reportEditor.checkSelection();
        reportEditor.repaint();
    }

    public void clear() {
        reportEditor.setReport(new Report());
        reportEditor.clearSelection();
        reportEditor.clear();
        reportEditor.checkSelection();
        reportEditor.repaint();
    }

    public ReportEditor getReportEditor() {
        return reportEditor;
    }

}

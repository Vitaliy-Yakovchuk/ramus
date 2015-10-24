package com.ramussoft.report.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.ramussoft.common.AdditionalPluginLoader;
import com.ramussoft.common.Element;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.Commands;
import com.ramussoft.report.DocBookExporter;
import com.ramussoft.report.ReportLoadCallback;
import com.ramussoft.report.ReportResourceManager;

public class DocBookScriptReportEditorView extends ReportEditorView {

    private ScriptEditorView editorView;

    private com.ramussoft.gui.common.event.ActionListener fullRefresh;

    private String saved;

    private Timer timer;

    private boolean changed = false;

    private Object lock = new Object();

    private long changeTime;

    public DocBookScriptReportEditorView(final GUIFramework framework,
                                         final Element element) {
        super(framework, element);
        timer = new Timer(500, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean save;
                synchronized (lock) {
                    if (System.currentTimeMillis() - changeTime < 500)
                        return;
                    save = changed;
                }

                if (save) {
                    save();
                    synchronized (lock) {
                        changed = false;
                    }
                }
            }
        });

        timer.start();

        editorView = new ScriptEditorView(framework) {
            /**
             *
             */
            private static final long serialVersionUID = -8884124882782860341L;

            @Override
            protected void changed() {
                synchronized (lock) {
                    changed = true;
                    changeTime = System.currentTimeMillis();
                }
            }
        };

        editorView.load(framework.getEngine(), element);

        saved = editorView.getText();

        fullRefresh = new com.ramussoft.gui.common.event.ActionListener() {

            @Override
            public void onAction(
                    com.ramussoft.gui.common.event.ActionEvent event) {
                editorView.load(framework.getEngine(), element);
            }
        };
        framework.addActionListener(Commands.FULL_REFRESH, fullRefresh);

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
        buttonsPanel.add(createOpenViewButton(group, htmlView));
        Iterator<DocBookExporter> iterator = AdditionalPluginLoader
                .loadProviders(DocBookExporter.class);
        while (iterator.hasNext()) {
            final DocBookExporter exporter = iterator.next();
            JButton tb = new JButton();
            tb.setText(exporter.getActionName());
            tb.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    exporter.createReport(getFramework(),
                            new ReportLoadCallback() {

                                @Override
                                public InputStream getDocBookInputStream() {
                                    // TODO Auto-generated method stub
                                    return null;
                                }
                            });
                }
            });
            buttonsPanel.add(tb);
        }
    }

    @Override
    public void beforeSubviewActivated(SubView view) {
        save();
        super.beforeSubviewActivated(view);
    }

    @Override
    public void close() {
        super.close();
        save();
        framework.removeActionListener(Commands.FULL_REFRESH, fullRefresh);
    }

    @Override
    protected void save() {
        super.save();
        if (!editorView.getText().equals(saved)) {
            saved = editorView.getText();
            editorView.save(framework.getEngine(), element);
        }
    }

    @Override
    protected HTMLView createHTMLView() {
        return new HTMLView(this) {
            /**
             *
             */
            private static final long serialVersionUID = -6917086651693696629L;

            private Action saveAction = new AbstractAction() {

                /**
                 *
                 */
                private static final long serialVersionUID = 6340572538511478889L;

                {
                    putValue(ACTION_COMMAND_KEY, "Action.ExportReportToFile");
                    putValue(
                            SMALL_ICON,
                            new ImageIcon(getClass().getResource(
                                    "/com/ramussoft/gui/export.png")));
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    exportToFile();
                }
            };

            protected void exportToFile() {
                File file = framework.showSaveDialog("DOCBOOK", ".xml");
                if (file != null) {
                    try {

                        TransformerFactory transformerFactory = TransformerFactory
                                .newInstance();
                        Transformer transformer = transformerFactory
                                .newTransformer();

                        FileOutputStream fos = new FileOutputStream(file);
                        StreamResult streamResult = new StreamResult(fos);
                        transformer
                                .setOutputProperty(OutputKeys.INDENT, "true");
                        transformer.transform(new StreamSource(
                                new StringReader(text)), streamResult);
                        fos.close();
                    } catch (Exception exception) {
                        JOptionPane.showMessageDialog(framework.getMainFrame(),
                                exception.getLocalizedMessage());
                    }
                }
            }

            @Override
            public Action[] getActions() {
                Action[] actions = super.getActions();
                actions = Arrays.copyOf(actions, actions.length + 1);
                actions[actions.length - 1] = saveAction;
                return actions;
            }

            @Override
            public String getTitle() {
                return ReportResourceManager.getString("XMLView.title");
            }
        };
    }
}

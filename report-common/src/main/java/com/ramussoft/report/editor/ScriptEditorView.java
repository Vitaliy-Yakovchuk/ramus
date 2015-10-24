package com.ramussoft.report.editor;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.SourceFormatter;

import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.report.ReportPlugin;
import com.ramussoft.report.ReportResourceManager;

public abstract class ScriptEditorView extends SubView {

    /**
     *
     */
    private static final long serialVersionUID = 2259960243681671362L;

    private FormatAction formatAction = new FormatAction();

    private ExportToJSSP exportToJSSP = new ExportToJSSP();

    private ImportFromJSSP importFromJSSP = new ImportFromJSSP();

    private JEditorPane editorPane = new JEditorPane();

    private GUIFramework framework;

    public ScriptEditorView(GUIFramework framework) {
        this.framework = framework;
        JScrollPane pane = new JScrollPane(editorPane);
        editorPane.setContentType("text/xhtml");
        this.add(pane, BorderLayout.CENTER);
        editorPane.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }
        });
    }

    protected abstract void changed();

    @Override
    public String getTitle() {
        return ReportResourceManager.getString("ScriptEditorView.title");
    }

    public String getText() {
        return editorPane.getText();
    }

    @Override
    public Action[] getActions() {
        return new Action[]{formatAction, importFromJSSP, exportToJSSP};
    }

    public void save(Engine engine, Element reportElement) {
        try {
            engine.setStream("/elements/" + reportElement.getId() + "/"
                    + ReportPlugin.getReportNameAttribute(engine).getId()
                    + "/report.jssp", getText().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void load(Engine engine, Element reportElement) {
        try {
            byte[] bs = engine.getStream("/elements/" + reportElement.getId()
                    + "/" + ReportPlugin.getReportNameAttribute(engine).getId()
                    + "/report.jssp");
            if (bs == null) {
                editorPane.setText("");
            } else
                editorPane.setText(new String(bs, "UTF-8"));
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    editorPane.scrollRectToVisible(new Rectangle());
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String getJSSPPath(Engine engine, Element reportElement) {
        return "/elements/" + reportElement.getId() + "/"
                + ReportPlugin.getReportNameAttribute(engine).getId()
                + "/report.jssp";
    }

    private class FormatAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -1920415454476046612L;

        public FormatAction() {
            putValue(ACTION_COMMAND_KEY, "Format.HTML");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/report/format.png")));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F,
                    KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String text = getText();
            Source segment = new Source(text);
            segment.fullSequentialParse();
            SourceFormatter formatter = new SourceFormatter(segment);
            text = formatter.toString();
            editorPane.selectAll();
            editorPane.replaceSelection(text);
        }

    }

    ;

    private class ExportToJSSP extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 1428852811300811953L;

        public ExportToJSSP() {
            putValue(ACTION_COMMAND_KEY, "Action.ExportReportToJSSP");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/export.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            exportToJSSP();
        }

    }

    ;

    private class ImportFromJSSP extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 5690589071398243015L;

        public ImportFromJSSP() {
            putValue(ACTION_COMMAND_KEY, "Action.ImportReportFromJSSP");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/import.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            importFromJSSP();
        }

    }

    private FileFilter fileFilter = new FileFilter() {

        @Override
        public boolean accept(File f) {
            if (f.isFile()) {
                if (f.getName().toLowerCase().endsWith(".jssp"))
                    return true;
                else
                    return false;
            }
            return true;
        }

        @Override
        public String getDescription() {
            return "*.jssp";
        }

    };

    public void exportToJSSP() {
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
        String file = Options.getString("JSSP_EXPORT_FILE");
        if (file != null)
            fileChooser.setSelectedFile(new File(file));
        if (fileChooser.showSaveDialog(framework.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            FileOutputStream stream;
            try {
                File selectedFile = fileChooser.getSelectedFile();
                if (!selectedFile.getName().toLowerCase().endsWith(".jssp")) {
                    selectedFile = new File(selectedFile.getParentFile(),
                            selectedFile.getName() + ".jssp");
                }
                Options.setString("JSSP_EXPORT_FILE", selectedFile
                        .getAbsolutePath());
                stream = new FileOutputStream(selectedFile);

                saveToStream(stream);
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(framework.getMainFrame(), e
                        .getLocalizedMessage());
            }
        }
    }

    private void saveToStream(FileOutputStream stream) throws IOException {
        byte[] bs = editorPane.getText().getBytes("UTF-8");
        stream.write(bs);
    }

    public void importFromJSSP() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(fileFilter);
        String file = Options.getString("JSSP_EXPORT_FILE");
        if (file != null)
            fileChooser.setSelectedFile(new File(file));
        if (fileChooser.showOpenDialog(framework.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            FileInputStream stream;
            try {
                File selectedFile = fileChooser.getSelectedFile();
                Options.setString("JSSP_EXPORT_FILE", selectedFile
                        .getAbsolutePath());
                stream = new FileInputStream(selectedFile);

                loadFromStream(stream);
                stream.close();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(framework.getMainFrame(), e
                        .getLocalizedMessage());
            }
        }
    }

    private void loadFromStream(FileInputStream stream) throws IOException {
        byte[] bs = new byte[stream.available()];
        stream.read(bs);
        editorPane.setText(new String(bs, "UTF-8"));
    }
}

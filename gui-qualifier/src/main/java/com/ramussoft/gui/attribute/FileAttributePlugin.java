package com.ramussoft.gui.attribute;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.core.attribute.simple.FilePersistent;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.qualifier.table.TableNode;
import com.ramussoft.gui.qualifier.table.TabledAttributePlugin;
import com.ramussoft.gui.qualifier.table.ValueGetter;
import com.ramussoft.gui.qualifier.table.event.Closeable;

public class FileAttributePlugin extends AbstractAttributePlugin implements
        TabledAttributePlugin {

    private static final DateFormat DATE_INSTANCE = DateFormat
            .getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

    private class FileEditor extends JPanel {

        /**
         *
         */
        private static final long serialVersionUID = -2106558038861963519L;

        private FilePersistent value;

        private JLabel lastModified = new JLabel();

        private JLabel upload = new JLabel();

        private JTextField name = new JTextField();

        private JTextField path = new JTextField();

        private AbstractAction openAction = new AbstractAction(
                GlobalResourcesManager.getString("FileEditor.Open")) {
            /**
             *
             */
            private static final long serialVersionUID = -538438968105132508L;

            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        };

        private AbstractAction saveAsAction = new AbstractAction(
                GlobalResourcesManager.getString("FileEditor.SaveAs")) {
            /**
             *
             */
            private static final long serialVersionUID = -538438968105132508L;

            @Override
            public void actionPerformed(ActionEvent e) {
                saveAs();
            }
        };

        public FileEditor() {
            super(new BorderLayout());
            path.setEditable(false);
            name.setEditable(false);
            double[][] size = {
                    {5, TableLayout.MINIMUM, 5, TableLayout.FILL, 5},
                    {5, TableLayout.FILL, 5, TableLayout.FILL, 5,
                            TableLayout.FILL, 5, TableLayout.FILL,
                            TableLayout.FULL}};
            JPanel info = new JPanel(new TableLayout(size));

            info.add(new JLabel(GlobalResourcesManager
                    .getString("FileEditor.FileName")), "1, 1");
            info.add(new JLabel(GlobalResourcesManager
                    .getString("FileEditor.FilePath")), "1, 3");
            info.add(new JLabel(GlobalResourcesManager
                    .getString("FileEditor.LastModifiedTime")), "1, 5");
            info.add(new JLabel(GlobalResourcesManager
                    .getString("FileEditor.UploadTime")), "1, 7");

            info.add(name, "3, 1");
            info.add(path, "3, 3");
            info.add(lastModified, "3, 5");
            info.add(upload, "3, 7");
            info.add(new JPanel(new BorderLayout()), "3, 8");
            this.add(info, BorderLayout.SOUTH);
            GridLayout grid = new GridLayout(2, 2, 5, 5);
            JPanel loadClear = new JPanel(grid);
            JButton clear = new JButton(new AbstractAction(
                    GlobalResourcesManager.getString("FileEditor.Clear")) {
                /**
                 *
                 */
                private static final long serialVersionUID = -538438968105132508L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    value = null;
                    updateEditor();
                }
            });

            JButton open = new JButton(openAction);

            JButton saveAs = new JButton(saveAsAction);

            JButton load = new JButton(new AbstractAction(
                    GlobalResourcesManager.getString("FileEditor.Load")) {
                /**
                 *
                 */
                private static final long serialVersionUID = -538438968105132508L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser chooser = new JFileChooser();
                    if (value != null)
                        chooser.setSelectedFile(new File(value.getPath()));
                    else {
                        String f = Options.getString("FILE_PLUGIN_LAST_FILE");
                        if (f != null)
                            chooser.setSelectedFile(new File(f));
                    }
                    int r = chooser.showOpenDialog(null);
                    if (r == JFileChooser.APPROVE_OPTION) {
                        File file = chooser.getSelectedFile();
                        FilePersistent old = value;
                        value = new FilePersistent();
                        value.setLastModifiedTime(new Timestamp(file
                                .lastModified()));
                        value.setName(file.getName());
                        value.setPath(file.getAbsolutePath());
                        value.setUploadTime(new Timestamp(System
                                .currentTimeMillis()));
                        try {
                            value.setData(loadData(file));
                        } catch (IOException e1) {
                            JOptionPane.showMessageDialog(FileEditor.this, e1
                                    .getLocalizedMessage());
                            value = old;
                        }
                        Options.setString("FILE_PLUGIN_LAST_FILE", file
                                .getAbsolutePath());
                    }
                    updateEditor();
                }
            });
            loadClear.add(load);
            loadClear.add(clear);
            loadClear.add(open);
            loadClear.add(saveAs);
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panel.add(loadClear);
            this.add(panel, BorderLayout.NORTH);
        }

        protected void saveAs() {
            JFileChooser chooser = new JFileChooser() {
                /**
                 *
                 */
                private static final long serialVersionUID = -5803938951067332185L;

                @Override
                public void approveSelection() {
                    if (getSelectedFile().exists()) {
                        if (JOptionPane.showConfirmDialog(framework
                                        .getMainFrame(), GlobalResourcesManager
                                        .getString("File.Exists"), UIManager
                                        .getString("OptionPane.messageDialogTitle"),
                                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                            return;
                    }
                    super.approveSelection();
                }
            };
            chooser.setSelectedFile(new File(value.getPath()));
            if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    saveData(value.getData(), file);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(FileEditor.this, e
                            .getLocalizedMessage());
                }
            }
        }

        protected void open() {
            String s = File.separator;
            String tmp = System.getProperty("java.io.tmpdir");
            if (!tmp.endsWith(s))
                tmp += s;
            String name = value.getName();
            File file = new File(tmp + name);
            int n = 0;
            while (file.exists()) {
                n++;
                int sp = name.lastIndexOf(".");
                String tName;
                if (sp >= 0) {
                    tName = name.substring(0, sp) + "(" + n + ")"
                            + name.substring(sp + 1, name.length());
                } else {
                    tName = name + "(" + n + ")";
                }
                file = new File(tmp + tName);
            }
            try {
                saveData(value.getData(), file);
                file.deleteOnExit();
                Desktop d = Desktop.getDesktop();
                if (d != null) {
                    d.open(file);
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
            }
        }

        public FilePersistent getValue() {
            return value;
        }

        public void setValue(FilePersistent value) {
            this.value = value;
            updateEditor();
        }

        private String format(Date date) {
            if (date == null)
                return "";
            return DATE_INSTANCE.format(date);
        }

        private void updateEditor() {
            if (value != null) {
                lastModified.setText(format(value.getLastModifiedTime()));
                upload.setText(format(value.getUploadTime()));
                name.setText(value.getName());
                path.setText(value.getPath());
                openAction.setEnabled(true);
                saveAsAction.setEnabled(true);
            } else {
                lastModified.setText("");
                upload.setText("");
                name.setText("");
                path.setText("");
                openAction.setEnabled(false);
                saveAsAction.setEnabled(false);
            }
        }
    }

    ;

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Core", "File", false, false, true);
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        return null;
    }

    @Override
    public AttributeEditor getAttributeEditor(Engine engine, AccessRules rules,
                                              Element element, Attribute attribute, AttributeEditor old) {
        if (old != null)
            old.close();
        return new AbstractAttributeEditor() {

            private FileEditor editor = new FileEditor();

            @Override
            public JComponent getComponent() {
                return editor;
            }

            @Override
            public Object getValue() {
                return editor.getValue();
            }

            @Override
            public Object setValue(Object value) {
                editor.setValue((FilePersistent) value);
                return value;
            }

        };
    }

    @Override
    public ValueGetter getValueGetter(Attribute attribute, Engine engine,
                                      GUIFramework framework, Closeable model) {
        return new ValueGetter() {
            @Override
            public Object getValue(TableNode node, int index) {
                return GlobalResourcesManager
                        .getString("AttributeType.Core.File");
            }
        };
    }

    @Override
    public boolean isCellEditable() {
        return true;
    }

    private byte[] loadData(File file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream fis = new FileInputStream(file);
        byte[] buff = new byte[1024 * 128];
        int r;
        while ((r = fis.read(buff)) > 0) {
            out.write(buff, 0, r);
        }
        fis.close();
        return out.toByteArray();
    }

    private void saveData(byte[] bs, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(bs);
        fos.close();
    }
}

package com.ramussoft.gui.common.print;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;

public abstract class ExportAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -7020231372330610733L;

    private JFileChooser fileChooser;

    private GUIFramework framework;

    public ExportAction(GUIFramework framework) {
        this.framework = framework;
    }

    public ExportAction(String text, GUIFramework framework) {
        super(text);
        this.framework = framework;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = getFileChooser();
        String fn = Options.getString(getClass().getName() + "FILE");
        if (fn != null)
            fileChooser.setSelectedFile(new File(fn));
        if (fileChooser.showSaveDialog(framework.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(getExtension())) {
                    file = new File(file.getParent(), file.getName()
                            + getExtension());
                }
                Options.setString(getClass().getName() + "FILE", file
                        .getAbsolutePath());
                exportToFile(file);
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(framework.getMainFrame(), e1
                        .getLocalizedMessage());
                e1.printStackTrace();
            }
        }
    }

    protected abstract void exportToFile(File file) throws Exception;

    /**
     * @return the fileChooser
     */
    protected JFileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new JFileChooser() {
                /**
                 *
                 */
                private static final long serialVersionUID = 3955173152569317884L;

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

            FileFilter fileFilter = getFileFilter();
            if (fileFilter != null)
                fileChooser.setFileFilter(fileFilter);
        }
        return fileChooser;
    }

    protected FileFilter getFileFilter() {
        return new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isFile()) {
                    if (f.getName().toLowerCase().endsWith(getExtension()))
                        return true;
                    else
                        return false;
                }
                return true;
            }

            @Override
            public String getDescription() {
                return "*" + getExtension();
            }

        };
    }

    protected abstract String getExtension();

}

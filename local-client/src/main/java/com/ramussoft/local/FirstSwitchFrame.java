package com.ramussoft.local;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileFilter;

import com.ramussoft.common.Metadata;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;

public class FirstSwitchFrame extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 7430488093195480409L;

    private static final String RSF = ".rsf";

    private static final int OPEN_FILE = 0;

    private static final int CREATE_NEW_FILE = 1;

    private File file;

    private boolean ok = false;

    private Action okAction = new AbstractAction(GlobalResourcesManager
            .getString("ok")) {

        /**
         *
         */
        private static final long serialVersionUID = 1925904409697149750L;

        @Override
        public void actionPerformed(ActionEvent e) {
            ok();
        }
    };

    private Action cancelAction = new AbstractAction(GlobalResourcesManager
            .getString("cancel")) {

        /**
         *
         */
        private static final long serialVersionUID = -4610490703060597980L;

        @Override
        public void actionPerformed(ActionEvent e) {
            cancel();
        }
    };

    private JComboBox fileLocation = new JComboBox();

    private JRadioButton openFile = new JRadioButton(GlobalResourcesManager
            .getString("Action.OpenExistsFile"));

    private JRadioButton newFile = new JRadioButton(GlobalResourcesManager
            .getString("Action.CreateNewFile"));

    private JCheckBox doNotAsk = new JCheckBox(GlobalResourcesManager
            .getString("UseThisAsDefault"));

    private Action browse = new AbstractAction(GlobalResourcesManager
            .getString("Action.Browse")) {

        /**
         *
         */
        private static final long serialVersionUID = 671870994848790673L;

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(fileFilter);
            String lastFile = Options.getString("LAST_FILE");
            if (lastFile != null) {
                chooser.setSelectedFile(new File(lastFile));
            }
            int r = chooser.showOpenDialog(FirstSwitchFrame.this);
            if (r == JFileChooser.APPROVE_OPTION) {
                fileLocation.setSelectedItem(chooser.getSelectedFile()
                        .getAbsolutePath());
            }
        }
    };

    public FirstSwitchFrame() {
        setTitle(MessageFormat.format(GlobalResourcesManager
                .getString("File.Launcher"), Metadata.getApplicationName()));
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/com/ramussoft/gui/application.png")));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JPanel buttons = new JPanel(new GridLayout(1, 2, 5, 0));
        fileLocation.setPreferredSize(new Dimension(500, fileLocation
                .getPreferredSize().width));
        fileLocation.setEditable(true);
        String[] files = Runner.getLastOpenedFiles();
        for (String file : files) {
            File file2 = new File(file);
            if ((file2.exists()) && (file2.canRead())) {
                fileLocation.addItem(file);
            }
        }

        String lastFile = Options.getString("LAST_FILE_FIRST");
        if (lastFile != null) {
            fileLocation.setSelectedItem(lastFile);
        }
        buttons.add(new JButton(okAction));
        buttons.add(new JButton(cancelAction));
        bottom.add(buttons);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(bottom, BorderLayout.SOUTH);
        getContentPane().add(createCenterPanel(), BorderLayout.CENTER);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        if (doNotAsk.isSelected()) {
            ok();
        }
    }

    private Component createCenterPanel() {
        double[][] size = {
                {5, TableLayout.MINIMUM, 5, TableLayout.FILL, 5,
                        TableLayout.MINIMUM, 5},
                {5, TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                        TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5}};
        JPanel panel = new JPanel(new TableLayout(size));
        panel.add(newFile, "1,1,5,1");
        panel.add(openFile, "1,3,5,3");
        ButtonGroup group = new ButtonGroup();
        group.add(newFile);
        group.add(openFile);

        int type = Options.getInteger("FIRST_LOAD_TYPE", CREATE_NEW_FILE);
        if (CREATE_NEW_FILE == type) {
            newFile.setSelected(true);
            setOpen(false);
        } else {
            openFile.setSelected(true);
            setOpen(true);
        }

        newFile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setOpen(false);
            }

        });

        openFile.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setOpen(true);
            }
        });

        panel.add(
                new JLabel(GlobalResourcesManager.getString("File.Location")),
                "1,5");
        panel.add(fileLocation, "3,5");
        panel.add(new JButton(browse), "5,5");
        panel.add(doNotAsk, "1,7,5,3");
        doNotAsk.setSelected(Options.getBoolean("DO_NOT_ASK_AGAIN_FIRST_LOAD",
                false));
        return panel;
    }

    private void setOpen(boolean b) {
        fileLocation.setEnabled(b);
        browse.setEnabled(b);
    }

    protected void cancel() {
        setVisible(false);
    }

    protected void ok() {
        if (openFile.isSelected()) {
            Options.setInteger("FIRST_LOAD_TYPE", OPEN_FILE);
            Object selectedItem = fileLocation.getSelectedItem();
            if (selectedItem == null)
                selectedItem = "";
            File f = new File(selectedItem.toString());
            if (!f.exists()) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(
                        GlobalResourcesManager.getString("FileNotExists"),
                        selectedItem.toString()));
                if (!isVisible())
                    setVisible(true);
                return;
            } else {
                file = f;
                Options.setString("LAST_FILE_FIRST", file.getAbsolutePath());
            }
        } else {
            Options.setInteger("FIRST_LOAD_TYPE", CREATE_NEW_FILE);
        }
        Options
                .setBoolean("DO_NOT_ASK_AGAIN_FIRST_LOAD", doNotAsk
                        .isSelected());
        ok = true;
        setVisible(false);
    }

    public File getFile() {
        return file;
    }

    public static void main(String[] args) {
        new FirstSwitchFrame().setVisible(true);
    }

    public boolean isOk() {
        return ok;
    }

    private FileFilter fileFilter = new FileFilter() {

        @Override
        public boolean accept(File f) {
            if (f.isFile()) {
                if (f.getName().toLowerCase().endsWith(getRSF()))
                    return true;
                else
                    return false;
            }
            return true;
        }

        @Override
        public String getDescription() {
            return "*" + getRSF();
        }

    };

    /**
     * @return the rSF
     */
    public static String getRSF() {
        return System.getProperty("user.ramus.application.extension", RSF);
    }

    public boolean isDoNotShow() {
        return doNotAsk.isSelected();
    }

}

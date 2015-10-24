package com.ramussoft.gui.server;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import info.clearthought.layout.TableLayout;

import com.ramussoft.gui.common.BaseDialog;

import static com.ramussoft.gui.server.Manager.getString;

public class Preferences extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = -8702606443465730780L;

    private JTextField location;

    private JTextField webPort = new JTextField();

    private JCheckBox canUndoRedo = new JCheckBox(getString("CanUndoRedo"));

    private JCheckBox autostart = new JCheckBox(getString("Autostart"));

    public Preferences() {
        setModal(true);
        init();
    }

    private void init() {
        double[][] size = {
                {5, TableLayout.MINIMUM, 5, TableLayout.FILL},
                {5, TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5,
                        TableLayout.MINIMUM, 5, TableLayout.MINIMUM, 5}};
        this.setTitle(getString("Preferences.Title"));

        JPanel panel = new JPanel(new TableLayout(size));

        JLabel path = new JLabel(getString("Base.Location"));

        panel.add(path, "1, 1");
        panel.add(createLocationSector(), "3, 1");

        panel.add(new JLabel(getString("WebPort")), "1, 3");
        panel.add(webPort, "3, 3");

        //panel.add(new JLabel(), "1, 5");
        panel.add(canUndoRedo, "3, 5");

        //panel.add(new JLabel(), "1, 7");
        panel.add(autostart, "3, 7");

        setMainPane(panel);
        pack();
        centerDialog();
        setResizable(false);
    }

    private Component createLocationSector() {
        JPanel panel = new JPanel(new BorderLayout());
        location = new JTextField();
        location.setPreferredSize(new Dimension(180, location
                .getPreferredSize().height));
        JButton button = new JButton("...");
        button.setToolTipText("Base.Location.ToolTip");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(location.getText());
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                if (chooser.showOpenDialog(Preferences.this) == JFileChooser.APPROVE_OPTION) {
                    location.setText(chooser.getSelectedFile()
                            .getAbsolutePath());
                }

            }
        });
        panel.add(location, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }

    public void load() {
        String conf = System.getProperty("ramus.server.base") + File.separator
                + "conf" + File.separator + "ramus-database.conf";
        Properties ps = new Properties();
        File file = new File(conf);
        if (file.exists()) {
            try {
                FileInputStream stream = new FileInputStream(file);
                ps.load(stream);
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        location.setText(getBaseLocation(ps));
        webPort.setText(getStringValue(ps, "WebPort", "8080"));
        canUndoRedo.setSelected(getBooleanValue(ps, "CanUndoRedo", false));
        autostart.setSelected(getBooleanValue(ps, "Autostart", false));
    }

    private boolean getBooleanValue(Properties ps, String key, boolean def) {
        String value = ps.getProperty(key);
        if (value == null)
            return def;
        return "true".equals(value);
    }

    private String getStringValue(Properties ps, String key, String def) {
        String value = ps.getProperty(key);
        if (value == null)
            return def;
        return value;
    }

    private String getBaseLocation(Properties ps) {
        String url = ps.getProperty("url");
        if (url == null) {
            return System.getProperty("ramus.server.base") + File.separator
                    + "base";
        }
        return url.substring("jdbc:h2:".length(), url.length()
                - "/ramus".length());
    }

    public void save() {
        String confDir = System.getProperty("ramus.server.base")
                + File.separator + "conf";
        new File(confDir).mkdirs();
        String conf = confDir + File.separator + "ramus-database.conf";
        Properties ps = new Properties();
        File file = new File(conf);
        ps.setProperty("url", "jdbc:h2:" + location.getText() + File.separator
                + "ramus");

        ps.setProperty("WebPort", webPort.getText());
        ps.setProperty("CanUndoRedo", toString(canUndoRedo.isSelected()));
        ps.setProperty("Autostart", toString(autostart.isSelected()));
        ps.setProperty("driver", "org.h2.Driver");
        ps.setProperty("user", "sa");
        ps.setProperty("password", "");
        try {
            FileOutputStream out = new FileOutputStream(file);
            ps.store(out, "Ramus light server configuration file");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String toString(boolean selected) {
        if (selected)
            return "true";
        return "false";
    }

    @Override
    protected void onOk() {
        save();
        super.onOk();
    }
}

package com.ramussoft.pb.frames.setup;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dsoft.utils.Options;

public class RunServerOptionsPanel extends JPanel {

    private JCheckBox jCheckBox2 = null;

    private JTextField jTextField2 = null;

    private JCheckBox jCheckBox3 = null;

    private JPanel jPanel = null;

    private JLabel jLabel = null;

    private JPanel jPanel1 = null;

    private JPanel jPanel11 = null;

    /**
     * This is the default constructor
     */
    public RunServerOptionsPanel() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setLayout(new BorderLayout());
        this.setSize(450, 260);
        this.add(getJPanel(), BorderLayout.NORTH);
    }

    /**
     * This method initializes jCheckBox2
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getJCheckBox2() {
        if (jCheckBox2 == null) {
            jCheckBox2 = new JCheckBox();
            jCheckBox2.setText("run_webServer_on_start");
        }
        return jCheckBox2;
    }

    /**
     * This method initializes jTextField2
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextField2() {
        if (jTextField2 == null) {
            jTextField2 = new JTextField();
            jTextField2.setPreferredSize(new java.awt.Dimension(120, 20));
            jTextField2.setText(Options.getString("WEB_SERVER_PORT", "80"));
        }
        return jTextField2;
    }

    public void loadOptions() {
        jTextField2.setText(Options.getString("WEB_SERVER_PORT", "80"));
        jCheckBox2
                .setSelected(Options.getBoolean("runWebServerOnStart", false));
        jCheckBox3.setSelected(Options
                .getBoolean("loadLastLounchedFile", false));
    }

    public void saveOptions() {
        Options.setString("WEB_SERVER_PORT", jTextField2.getText());
        Options.setBoolean("runWebServerOnStart", jCheckBox2.isSelected());
        Options.setBoolean("loadLastLounchedFile", jCheckBox3.isSelected());
    }

    /**
     * This method initializes jCheckBox3
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getJCheckBox3() {
        if (jCheckBox3 == null) {
            jCheckBox3 = new JCheckBox();
            jCheckBox3.setText("load_last_lounched_file");
        }
        return jCheckBox3;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            final GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 4;
            gridBagConstraints4.gridy = 3;
            final GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 2;
            gridBagConstraints3.gridy = 3;
            final GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
            gridBagConstraints21.gridx = 1;
            gridBagConstraints21.anchor = GridBagConstraints.WEST;
            gridBagConstraints21.gridy = 0;
            final GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.gridx = 1;
            gridBagConstraints11.anchor = GridBagConstraints.WEST;
            gridBagConstraints11.gridy = 3;
            jLabel = new JLabel();
            jLabel.setText("web_server_port:");
            final GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.anchor = GridBagConstraints.WEST;
            gridBagConstraints.gridy = 1;
            final GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridx = 0;
            gridBagConstraints2.gridy = 2;
            final GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.gridy = 3;
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.gridx = 3;
            jPanel = new JPanel();
            jPanel.setLayout(new GridBagLayout());
            jPanel.add(getJTextField2(), gridBagConstraints1);
            jPanel.add(getJCheckBox3(), gridBagConstraints);
            jPanel.add(jLabel, gridBagConstraints11);
            jPanel.add(getJCheckBox2(), gridBagConstraints21);
            jPanel.add(getJPanel1(), gridBagConstraints3);
            jPanel.add(getJPanel11(), gridBagConstraints4);
        }
        return jPanel;
    }

    /**
     * This method initializes jPanel1
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            jPanel1 = new JPanel();
            jPanel1.setLayout(new FlowLayout());
        }
        return jPanel1;
    }

    /**
     * This method initializes jPanel11
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel11() {
        if (jPanel11 == null) {
            jPanel11 = new JPanel();
            jPanel11.setLayout(new FlowLayout());
        }
        return jPanel11;
    }
} // @jve:decl-index=0:visual-constraint="10,10"

package com.ramussoft.pb.frames;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dsoft.utils.Options;

public class RunWebServerPanel extends JPanel {

    private JPanel jPanel = null;

    private JLabel jLabel = null;

    private JTextField jTextField = null;

    private JPanel jPanel1 = null;

    private JCheckBox jCheckBox = null;

    private JPanel jPanel2 = null;

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jLabel = new JLabel();
            jLabel.setText("web_server_port:");
            jPanel = new JPanel();
            jPanel.add(jLabel, null);
        }
        return jPanel;
    }

    /**
     * This method initializes jTextField
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextField() {
        if (jTextField == null) {
            jTextField = new JTextField();
            jTextField.setText(Options.getString("WEB_SERVER_PORT", "80"));
            jTextField.setPreferredSize(new java.awt.Dimension(120, 20));
        }
        return jTextField;
    }

    /**
     * This is the default constructor
     */
    public RunWebServerPanel() {
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
        this.setSize(427, 67);
        this.add(getJPanel1(), java.awt.BorderLayout.NORTH);
        this.add(getJPanel2(), java.awt.BorderLayout.SOUTH);
    }

    public String getPort() {
        return jTextField.getText();
    }

    /**
     * This method initializes jPanel1
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel1() {
        if (jPanel1 == null) {
            jPanel1 = new JPanel();
            jPanel1.setLayout(new BorderLayout());
            jPanel1.add(getJTextField(), java.awt.BorderLayout.CENTER);
            jPanel1.add(getJPanel(), java.awt.BorderLayout.WEST);
        }
        return jPanel1;
    }

    /**
     * This method initializes jCheckBox
     *
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getJCheckBox() {
        if (jCheckBox == null) {
            jCheckBox = new JCheckBox();
            jCheckBox.setText("run_webServer_on_start");
            jCheckBox.setSelected(Options.getBoolean("runWebServerOnStart",
                    false));
        }
        return jCheckBox;
    }

    /**
     * This method initializes jPanel2
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (jPanel2 == null) {
            jPanel2 = new JPanel();
            jPanel2.setLayout(new BorderLayout());
            jPanel2.add(getJCheckBox(), java.awt.BorderLayout.EAST);
        }
        return jPanel2;
    }

    public boolean isRunWebServerOnStart() {
        return jCheckBox.isSelected();
    }
} // @jve:decl-index=0:visual-constraint="10,10"

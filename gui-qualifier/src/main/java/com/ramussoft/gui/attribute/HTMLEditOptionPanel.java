package com.ramussoft.gui.attribute;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;

public class HTMLEditOptionPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private JLabel jLabel = null;

    private JPanel jPanel = null;

    private JTextField jTextField = null;

    private JButton jButton = null;

    private JFileChooser chooser = null;

    /**
     * This is the default constructor
     */
    public HTMLEditOptionPanel() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        final GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
        gridBagConstraints3.gridx = 3;
        gridBagConstraints3.gridy = 0;
        final GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints2.gridy = 0;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.gridx = 2;
        final GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
        gridBagConstraints1.gridx = 1;
        gridBagConstraints1.gridy = 0;
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jLabel = new JLabel();
        jLabel.setText(GlobalResourcesManager.getString("HTMLEditor.Programm"));
        this.setSize(300, 200);
        setLayout(new GridBagLayout());
        this.add(jLabel, gridBagConstraints);
        this.add(getJPanel(), gridBagConstraints1);
        this.add(getJTextField(), gridBagConstraints2);
        this.add(getJButton(), gridBagConstraints3);
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jPanel = new JPanel();
            jPanel.setLayout(new FlowLayout());
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
        }
        return jTextField;
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setText("...");
            jButton.setMnemonic(KeyEvent.VK_UNDEFINED);
            jButton.setToolTipText(GlobalResourcesManager.getString("HTMLEditor.SelectCommand"));
            jButton.setPreferredSize(new Dimension(20, 20));
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    brows();
                }
            });
        }
        return jButton;
    }

    protected void brows() {
        final JFileChooser chooser = getChooser();
        chooser.setSelectedFile(new File(jTextField.getText()));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            jTextField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private JFileChooser getChooser() {
        if (chooser == null) {
            chooser = new JFileChooser();
        }
        return chooser;
    }

    public void ok() {
        Options.setString("HTML_EDITOR", jTextField.getText());
    }

    public void modal() {
        jTextField.setText(Options.getString("HTML_EDITOR"));
    }

}

/*
 * Created on 15/10/2004
 */
package com.ramussoft.pb.frames.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.dsoft.utils.Options;

/**
 * @author ZDD
 */
public class RowFindPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JButton jButton = null;

    private JPanel jPanel = null;

    private JLabel jLabel = null;

    private JPanel jPanel1 = null;

    private JTextField jTextField = null;

    private JButton jButton1 = null;

    private JCheckBox jCheckBox = null;

    /**
     * This is the default constructor
     */
    public RowFindPanel() {
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
        this.setSize(581, 39);
        this.add(getJPanel(), java.awt.BorderLayout.WEST);
        setFocusable(true);
        final AbstractAction aa = new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e) {
                findNext(jTextField.getText(), jCheckBox.isSelected());
            }

        };
        this.getInputMap().put(KeyStroke.getKeyStroke("F3"), "findNext");
        this.getActionMap().put("findNext", aa);
        getJTextField().getInputMap().put(KeyStroke.getKeyStroke("F3"),
                "findNext");
        getJTextField().getActionMap().put("findNext", aa);
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setIcon(new ImageIcon(getClass().getResource(
                    "/images/x.png")));
            jButton.setPreferredSize(new java.awt.Dimension(14, 14));
            jButton.setToolTipText("close_find_panel");
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    close();
                }
            });
        }
        return jButton;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jLabel = new JLabel();
            jPanel = new JPanel();
            jLabel.setText("find:");
            jPanel.add(getJButton(), null);
            jPanel.add(getJPanel1(), null);
            jPanel.add(jLabel, null);
            jPanel.add(getJTextField(), null);
            jPanel.add(getJButton1(), null);
            jPanel.add(getJCheckBox(), null);
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
        }
        return jPanel1;
    }

    /**
     * This method initializes jTextField
     *
     * @return javax.swing.JTextField
     */
    public JTextField getJTextField() {
        if (jTextField == null) {
            jTextField = new JTextField();
            jTextField.getDocument().addDocumentListener(
                    new DocumentListener() {

                        public void insertUpdate(final DocumentEvent e) {
                            ch();
                        }

                        public void removeUpdate(final DocumentEvent e) {
                            ch();
                        }

                        public void changedUpdate(final DocumentEvent e) {
                            ch();
                        }

                        private void ch() {
                            findp(jTextField.getText());
                        }

                    });
            jTextField.setPreferredSize(new java.awt.Dimension(160, 21));
        }
        return jTextField;
    }

    /**
     * @param text
     */
    protected void findp(final String text) {
        final boolean res = find(text, jCheckBox.isSelected());
        if (res)
            jTextField
                    .setBackground(UIManager.getColor("TextField.background"));
        else
            jTextField.setBackground(Color.red);
    }

    /**
     * This method initializes jButton1
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton1() {
        if (jButton1 == null) {
            jButton1 = new JButton();
            // jButton1.setText("find_next");
            jButton1.setPreferredSize(new java.awt.Dimension(14, 14));
            jButton1.setIcon(new ImageIcon(getClass().getResource(
                    "/images/findnext_1.gif")));
            jButton1.setToolTipText("find_next_word");
            jButton1.addActionListener(new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    findNext(jTextField.getText(), jCheckBox.isSelected());
                }

            });
        }
        return jButton1;
    }

    public boolean find(final String text, final boolean wordsOrder) {
        return false;
    }

    public boolean findNext(final String text, final boolean wordsOrder) {
        return false;
    }

    public void close() {
        setVisible(false);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.swing.JComponent#setVisible(boolean)
     */
    @Override
    public void setVisible(final boolean aFlag) {
        super.setVisible(aFlag);
        /*
         * if (aFlag) { jTextField.requestFocus(); }
		 */
    }

    /**
     * This method initializes jCheckBox
     *
     * @return javax.swing.JCheckBox
     */
    public JCheckBox getJCheckBox() {
        if (jCheckBox == null) {
            jCheckBox = new JCheckBox();
            jCheckBox.setSelected(true);
            jCheckBox.setToolTipText("consider_words_order");
            jCheckBox.setText("consider_words_order");
            jCheckBox.addActionListener(new ActionListener() {

                public void actionPerformed(final ActionEvent e) {
                    Options.setBoolean("consider_words_order", jCheckBox
                            .isSelected());
                }

            });
            jCheckBox.setSelected(Options.getBoolean("consider_words_order",
                    jCheckBox.isSelected()));
        }
        return jCheckBox;
    }
} // @jve:decl-index=0:visual-constraint="10,10"

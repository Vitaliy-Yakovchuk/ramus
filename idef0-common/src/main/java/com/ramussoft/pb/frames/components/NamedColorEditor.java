/*
 * Created on 13/8/2005
 */
package com.ramussoft.pb.frames.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dsoft.utils.Options;

/**
 * @author ZDD
 */
public class NamedColorEditor extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JButton jButton = null;

    private JPanel jPanel = null;

    private JLabel jLabel = null;

    private String name = null;

    private JPanel jPanel1 = null;

    private JPanel jPanel2 = null;

    public void apply() {
        Options.setColor(name, jPanel2.getBackground());
    }

    public void setColorName(final String name, final Color def) {
        this.name = name;
        jLabel.setText(name);
        jPanel2.setBackground(Options.getColor(name, def));
    }

    /**
     * This is the default constructor
     */
    public NamedColorEditor() {
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
        this.setSize(300, 23);
        this.add(getJPanel(), java.awt.BorderLayout.CENTER);
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setPreferredSize(new java.awt.Dimension(20, 20));
            jButton.setText("...");
            jButton.setToolTipText("select_color");
            jButton.setFocusable(false);
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    selectColor();
                }
            });
        }
        return jButton;
    }

    /**
     *
     */
    protected void selectColor() {
        final Color c = JColorChooser.showDialog(this, null, Options.getColor(name,
                jPanel2.getBackground()));
        if (c != null)
            jPanel2.setBackground(c);
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
            jPanel.setLayout(new BorderLayout());
            jPanel.add(jLabel, java.awt.BorderLayout.CENTER);
            jPanel.add(getJPanel1(), java.awt.BorderLayout.EAST);
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
            final GridLayout gridLayout1 = new GridLayout();
            jPanel1 = new JPanel();
            jPanel1.setLayout(gridLayout1);
            gridLayout1.setRows(1);
            gridLayout1.setColumns(2);
            jPanel1.add(getJPanel2(), null);
            jPanel1.add(getJButton(), null);
        }
        return jPanel1;
    }

    /**
     * This method initializes jPanel2
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel2() {
        if (jPanel2 == null) {
            jPanel2 = new JPanel();
        }
        return jPanel2;
    }

    /**
     * @return
     */
    public Color getSelColor() {
        return jPanel2.getBackground();
    }
} // @jve:decl-index=0:visual-constraint="10,10"

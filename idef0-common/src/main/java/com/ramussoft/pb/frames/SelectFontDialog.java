/*
 * Created on 29/10/2004
 */
package com.ramussoft.pb.frames;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.pb.frames.components.JFontChooser;

/**
 * @author ZDD
 */
public class SelectFontDialog extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private javax.swing.JPanel jContentPane = null;

    private JPanel jPanel = null;

    private JPanel jPanel1 = null;

    private JButton jButton = null;

    private JButton jButton1 = null;

    private JFontChooser jFontChooser = null;

    private Font res = null;

    private static SelectFontDialog fontDialog = null;

    /**
     * @return Returns the fontDialog.
     */
    public static SelectFontDialog getFontDialog() {
        if (fontDialog == null) {
            fontDialog = new SelectFontDialog();
        }
        return fontDialog;
    }

    public static SelectFontDialog getFontDialog(final JFrame frame) {
        if (fontDialog == null) {
            fontDialog = new SelectFontDialog(frame);
        }
        return fontDialog;
    }

    public Font showModal(final Font font) {
        Options.loadOptions("select_font_dialog", this);
        res = font;
        jFontChooser.setSelFont(font);
        setVisible(true);
        Options.saveOptions("select_font_dialog", this);
        return res;
    }

    /**
     * This is the default constructor
     */
    public SelectFontDialog() {
        super();
        initialize();
        setLocationRelativeTo(null);
    }

    public SelectFontDialog(final JFrame frame) {
        super();
        initialize();
        setLocationRelativeTo(frame);
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setModal(true);
        setResizable(false);
        setTitle("select_font");
        this.setSize(409, 385);
        setContentPane(getJContentPane());
        ResourceLoader.setJComponentsText(this);

    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new javax.swing.JPanel();
            jContentPane.setLayout(new java.awt.BorderLayout());
            jContentPane.add(getJPanel(), java.awt.BorderLayout.SOUTH);
            jContentPane.add(getJFontChooser(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            final FlowLayout flowLayout4 = new FlowLayout();
            jPanel = new JPanel();
            jPanel.setLayout(flowLayout4);
            flowLayout4.setAlignment(java.awt.FlowLayout.RIGHT);
            jPanel.add(getJPanel1(), null);
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
            final GridLayout gridLayout3 = new GridLayout();
            jPanel1 = new JPanel();
            jPanel1.setLayout(gridLayout3);
            gridLayout3.setRows(1);
            jPanel1.add(getJButton(), null);
            jPanel1.add(getJButton1(), null);
        }
        return jPanel1;
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setText("ok");
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    res = jFontChooser.getSelFont();
                    setVisible(false);
                }
            });
        }
        return jButton;
    }

    /**
     * This method initializes jButton1
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton1() {
        if (jButton1 == null) {
            jButton1 = new JButton();
            jButton1.setText("cancel");
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    setVisible(false);
                }
            });
        }
        return jButton1;
    }

    /**
     * This method initializes jFontChooser
     *
     * @return com.jason.clasificators.frames.idf.JFontChooser
     */
    private JFontChooser getJFontChooser() {
        if (jFontChooser == null) {
            jFontChooser = new JFontChooser();
        }
        return jFontChooser;
    }
} // @jve:decl-index=0:visual-constraint="10,10"

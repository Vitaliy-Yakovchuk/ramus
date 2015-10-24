/*
 * Created on 13/8/2005
 */
package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;

import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.pb.frames.BaseDialog;
import com.ramussoft.pb.frames.components.JFontChooser;
import com.ramussoft.pb.idef.visual.MovingText;

/**
 * @author ZDD
 */
public class TextOptionsDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private javax.swing.JPanel jContentPane = null;

    private JPanel jPanel1 = null;

    private JTabbedPane jTabbedPane = null;

    private JPanel jPanel3 = null;

    private JPanel jPanel4 = null;

    private JPanel jPanel5 = null;

    private JColorChooser jColorChooser = null;

    private JFontChooser jFontChooser = null;

    private JScrollPane jScrollPane = null;

    private JTextArea jTextArea = null;

    private boolean isOk;

    public void showModal(final MovingText mt) {
        isOk = false;
        Options.loadOptions("text_options_dialog", this);
        getJTextArea().setText(mt.getText());
        getJFontChooser().setSelFont(mt.getFont());
        getJColorChooser().setColor(mt.getColor());
        setVisible(true);
        if (isOk) {
            mt.getMovingArea().startUserTransaction();
            mt.setText(getJTextArea().getText());
            mt.setFont(getJFontChooser().getSelFont());
            mt.setColor(getJColorChooser().getColor());
            mt.getMovingArea().getRefactor().getTexts()
                    .set(mt.getMovingArea().getActiveMovingTextIndex(), mt);
            mt.getMovingArea().getRefactor().setUndoPoint();
        }
        Options.saveOptions("text_options_dialog", this);
    }

    /**
     * This is the default constructor
     */
    public TextOptionsDialog() {
        super();
        initialize();
        ResourceLoader.setJComponentsText(this);
        setLocationRelativeTo(null);
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setResizable(false);
        setModal(true);
        setTitle("text_options");
        this.setSize(407, 417);
        setMainPane(getJContentPane());
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
            jContentPane.add(getJPanel1(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
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
            jPanel1.add(getJTabbedPane(), java.awt.BorderLayout.CENTER);
        }
        return jPanel1;
    }

    @Override
    protected void onOk() {
        isOk = true;
        setVisible(false);
        super.onOk();
    }

    /**
     * This method initializes jTabbedPane
     *
     * @return javax.swing.JTabbedPane
     */
    private JTabbedPane getJTabbedPane() {
        if (jTabbedPane == null) {
            jTabbedPane = new JTabbedPane();
            jTabbedPane.addTab(ResourceLoader.getString("Text.Name"), null,
                    getJPanel3(), null);
            jTabbedPane.addTab(ResourceLoader.getString("font"), null,
                    getJPanel4(), null);
            jTabbedPane.addTab(ResourceLoader.getString("color"), null,
                    getJPanel5(), null);
        }
        return jTabbedPane;
    }

    /**
     * This method initializes jPanel3
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel3() {
        if (jPanel3 == null) {
            jPanel3 = new JPanel();
            jPanel3.setLayout(new BorderLayout());
            jPanel3.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
        }
        return jPanel3;
    }

    /**
     * This method initializes jPanel4
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel4() {
        if (jPanel4 == null) {
            jPanel4 = new JPanel();
            jPanel4.setLayout(new BorderLayout());
            jPanel4.add(getJFontChooser(), java.awt.BorderLayout.CENTER);
        }
        return jPanel4;
    }

    /**
     * This method initializes jPanel5
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel5() {
        if (jPanel5 == null) {
            jPanel5 = new JPanel();
            jPanel5.setLayout(new BorderLayout());
            jPanel5.add(getJColorChooser(), java.awt.BorderLayout.CENTER);
        }
        return jPanel5;
    }

    /**
     * This method initializes jColorChooser
     *
     * @return javax.swing.JColorChooser
     */
    private JColorChooser getJColorChooser() {
        if (jColorChooser == null) {
            jColorChooser = new JColorChooser();
        }
        return jColorChooser;
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

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTextArea());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTextArea
     *
     * @return javax.swing.JTextArea
     */
    private JTextArea getJTextArea() {
        if (jTextArea == null) {
            jTextArea = new JTextArea();
            jTextArea.setWrapStyleWord(true);
            jTextArea.setLineWrap(true);
        }
        return jTextArea;
    }
} // @jve:decl-index=0:visual-constraint="10,10"

/*
 * Created on 29/10/2004
 */
package com.ramussoft.pb.frames.components;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.pb.Main;
import com.ramussoft.pb.frames.SelectFontDialog;

/**
 * @author ZDD
 */
public class NamedFontEditor extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JLabel jLabel = null;

    private JPanel jPanel = null;

    private JPanel jPanel1 = null;

    private JLabel jLabel1 = null;

    private JButton jButton = null;

    private Font font = null;

    private String vName = null;

    /**
     * This is the default constructor
     */
    public NamedFontEditor() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        jLabel = new JLabel();
        setLayout(new BorderLayout());
        this.setSize(300, 23);
        setPreferredSize(new java.awt.Dimension(40, 20));
        jLabel.setText("JLabel");
        this.add(jLabel, java.awt.BorderLayout.WEST);
        this.add(getJPanel(), java.awt.BorderLayout.CENTER);
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            jLabel1 = new JLabel();
            jPanel = new JPanel();
            jPanel.setLayout(new BorderLayout());
            jLabel1.setText("JLabel");
            jPanel.add(getJPanel1(), java.awt.BorderLayout.WEST);
            jPanel.add(jLabel1, java.awt.BorderLayout.CENTER);
            jPanel.add(getJButton(), java.awt.BorderLayout.EAST);
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
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setText("...");
            jButton.setPreferredSize(new java.awt.Dimension(20, 20));
            jButton.setToolTipText("select_font");
            jButton.setFocusable(false);
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    font = SelectFontDialog.getFontDialog(Main.getMainFrame())
                            .showModal(font);
                    printFont();
                }
            });
        }
        return jButton;
    }

    public void setSelFont(final String vName) {
        setSelFont(vName, new Font("Dialog", 0, 12));
    }

    public void setSelFont(final String vName, final Font def) {
        font = Options.getFont(vName, def);
        jLabel.setText(vName);
        this.vName = vName;
        printFont();
    }

    private void printFont() {
        String text = font.getName();
        text += " " + font.getSize();
        final int style = font.getStyle();
        if ((style & Font.ITALIC) == Font.ITALIC)
            text += " " + ResourceLoader.getString("italic");
        if ((font.getStyle() & Font.BOLD) == Font.BOLD)
            text += " " + ResourceLoader.getString("bold");
        jLabel1.setFont(font);
        jLabel1.setText(text);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.awt.Component#getFont()
     */
    public Font getSelFont() {
        return font;
    }

    public void saveFont() {
        Options.setFont(vName, font);
    }

} // @jve:decl-index=0:visual-constraint="10,10"

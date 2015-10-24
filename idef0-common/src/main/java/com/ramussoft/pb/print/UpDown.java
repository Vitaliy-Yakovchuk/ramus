package com.ramussoft.pb.print;

import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

public class UpDown extends JPanel {

    private static final long serialVersionUID = 1L;

    public JButton buttonUp = null;

    public JButton buttonDown = null;

    /**
     * This is the default constructor
     */
    public UpDown() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(getJButton(), null);
        this.add(getJButton1(), null);
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (buttonUp == null) {
            buttonUp = new JButton();
            buttonUp.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    onUp();
                }
            });
            buttonUp.setMnemonic(KeyEvent.VK_UNDEFINED);
            buttonUp.setIcon(new ImageIcon(getClass().getResource(
                    "/images/smallUp.gif")));
            buttonUp.setFocusable(false);
        }
        return buttonUp;
    }

    /**
     * This method initializes jButton1
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton1() {
        if (buttonDown == null) {
            buttonDown = new JButton();
            buttonDown.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    onDown();
                }
            });
            buttonDown.setIcon(new ImageIcon(getClass().getResource(
                    "/images/smallDown.gif")));
            buttonDown.setFocusable(false);
        }
        return buttonDown;
    }

    protected void onUp() {

    }

    protected void onDown() {

    }

} // @jve:decl-index=0:visual-constraint="10,10"

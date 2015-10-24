package com.ramussoft.gui.common;

import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.jdesktop.swingx.JXBusyLabel;

public class BusyDialog extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = 2544684936591850206L;
    private JXBusyLabel busyLabel;

    public BusyDialog(JFrame frame, String title) {
        super(frame);
        setTitle(title);
        init();
    }

    public BusyDialog(JDialog dialog, String title) {
        super(dialog);
        setTitle(title);
        init();
    }

    public BusyDialog(JFrame dialog, String title, boolean b) {
        super(dialog, b);
        setTitle(title);
        init();
    }

    public BusyDialog(JDialog dialog, String title, boolean b) {
        super(dialog, b);
        setTitle(title);
        init();
    }

    private void init() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        busyLabel = new JXBusyLabel();

        busyLabel.setText(getTitle());
        busyLabel.setBusy(true);

        add(busyLabel);
        pack();
        setSize(new Dimension(300, getBounds().height));
        setLocationRelativeTo(this);
        setResizable(false);
    }

    public void setText(String text) {
        busyLabel.setText(text);
    }

}

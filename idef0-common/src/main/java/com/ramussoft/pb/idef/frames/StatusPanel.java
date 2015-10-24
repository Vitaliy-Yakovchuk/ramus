/*
 * Created on 13/8/2005
 */
package com.ramussoft.pb.idef.frames;

import info.clearthought.layout.TableLayout;

import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.dsoft.pb.idef.elements.Status;

/**
 * @author ZDD
 */
public class StatusPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JRadioButton jRadioButton = null;

    private ButtonGroup buttonGroup = null; // @jve:decl-index=0:visual-constraint="47,181"

    private JRadioButton jRadioButton1 = null;

    private JRadioButton jRadioButton2 = null;

    private JRadioButton jRadioButton3 = null;

    private JRadioButton jRadioButton4 = null;

    private JTextField jTextField = null;

    /**
     * @return Returns the status.
     */
    public Status getStatus() {
        final Status status = new Status();
        for (int i = 0; i < 5; i++)
            if (getButton(i).isSelected())
                status.setType(i);
        if (status.getType() == Status.OTHER) {
            String s = getJTextField().getText();
            if (s.equals(""))
                s = null;
            status.setOtherName(s);
        }
        return status;
    }

    /**
     * @param status The status to set.
     */
    public void setStatus(final Status status) {
        getButton(status.getType()).setSelected(true);
        if (status.getType() == Status.OTHER) {
            String s = status.getAtherName();
            if (s == null)
                s = "";
            getJTextField().setText(s);
        } else
            getJTextField().setText("");
        buttonClicked();
    }

    private JRadioButton getButton(final int type) {
        switch (type) {
            case Status.WORKING:
                return getJRadioButton();
            case Status.DRAFT:
                return getJRadioButton1();
            case Status.RECOMENDED:
                return getJRadioButton2();
            case Status.PUBLICATION:
                return getJRadioButton3();
            case Status.OTHER:
                return getJRadioButton4();
        }
        return null;
    }

    /**
     * This is the default constructor
     */
    public StatusPanel() {
        super();
        initialize();
        getButtonGroup();
        getButton(Status.WORKING).setSelected(true);
        buttonClicked();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        double[][] size = {{5, TableLayout.FILL, 5},
                {5, TableLayout.FILL, 5}};
        this.setLayout(new TableLayout(size));
        final GridLayout gridLayout3 = new GridLayout();
        JPanel child = new JPanel(gridLayout3);
        this.setSize(351, 105);
        gridLayout3.setRows(3);
        gridLayout3.setColumns(2);
        gridLayout3.setHgap(5);
        gridLayout3.setVgap(5);
        child.add(getJRadioButton(), null);
        child.add(getJRadioButton1(), null);
        child.add(getJRadioButton2(), null);
        child.add(getJRadioButton3(), null);
        child.add(getJRadioButton4(), null);
        child.add(getJTextField(), null);
        this.add(child, "1,1");
    }

    /**
     * This method initializes jRadioButton
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getJRadioButton() {
        if (jRadioButton == null) {
            jRadioButton = new JRadioButton();
            jRadioButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    buttonClicked();
                }
            });
        }
        return jRadioButton;
    }

    /**
     *
     */
    protected void buttonClicked() {
        getJTextField().setEditable(getButton(Status.OTHER).isSelected());
    }

    /**
     * This method initializes buttonGroup
     *
     * @return javax.swing.ButtonGroup
     */
    private ButtonGroup getButtonGroup() {
        if (buttonGroup == null) {
            buttonGroup = new ButtonGroup();
            for (int i = 0; i < 5; i++) {
                buttonGroup.add(getButton(i));
                getButton(i).setText(Status.STATUS_NAMES[i]);
            }
        }
        return buttonGroup;
    }

    /**
     * This method initializes jRadioButton1
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getJRadioButton1() {
        if (jRadioButton1 == null) {
            jRadioButton1 = new JRadioButton();
            jRadioButton1
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            buttonClicked();
                        }
                    });
        }
        return jRadioButton1;
    }

    /**
     * This method initializes jRadioButton2
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getJRadioButton2() {
        if (jRadioButton2 == null) {
            jRadioButton2 = new JRadioButton();
            jRadioButton2
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            buttonClicked();
                        }
                    });
        }
        return jRadioButton2;
    }

    /**
     * This method initializes jRadioButton3
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getJRadioButton3() {
        if (jRadioButton3 == null) {
            jRadioButton3 = new JRadioButton();
            jRadioButton3
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            buttonClicked();
                        }
                    });
        }
        return jRadioButton3;
    }

    /**
     * This method initializes jRadioButton4
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getJRadioButton4() {
        if (jRadioButton4 == null) {
            jRadioButton4 = new JRadioButton();
            jRadioButton4
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            buttonClicked();
                        }
                    });
        }
        return jRadioButton4;
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
} // @jve:decl-index=0:visual-constraint="10,10"

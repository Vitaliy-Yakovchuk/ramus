package com.ramussoft.pb.frames.components;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.ramussoft.pb.Function;

public class FunctionType extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JRadioButton jRadioButton = null;

    private JRadioButton jRadioButton1 = null;

    private JRadioButton jRadioButton2 = null;

    private JRadioButton jRadioButton3 = null;

    private JRadioButton jRadioButton4 = null;

    private int type = -1;

    public void setFunction(final Function function) {
        setType(function.getType());
    }

    public void setType(int type) {
        switch (type) {
            case Function.TYPE_PROCESS_KOMPLEX:
                jRadioButton.setSelected(true);
                break;
            case Function.TYPE_PROCESS:
                jRadioButton1.setSelected(true);
                break;
            case Function.TYPE_PROCESS_PART:
                jRadioButton2.setSelected(true);
                break;
            case Function.TYPE_OPERATION:
                jRadioButton3.setSelected(true);
                break;
            case Function.TYPE_ACTION:
                jRadioButton4.setSelected(true);
                break;
        }
        this.type = type;
    }

    /**
     * This is the default constructor
     */
    public FunctionType() {
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
        this.setSize(300, 200);
        this.add(getJRadioButton(), null);
        this.add(getJRadioButton1(), null);
        this.add(getJRadioButton2(), null);
        this.add(getJRadioButton3(), null);
        this.add(getJRadioButton4(), null);
        final ButtonGroup bg = new ButtonGroup();
        bg.add(getJRadioButton());
        bg.add(getJRadioButton1());
        bg.add(getJRadioButton2());
        bg.add(getJRadioButton3());
        bg.add(getJRadioButton4());
    }

    /**
     * This method initializes jRadioButton
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getJRadioButton() {
        if (jRadioButton == null) {
            jRadioButton = new JRadioButton();
            jRadioButton.setSelected(true);
            jRadioButton.setText("process_komplex");
            jRadioButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    fTypeCh();
                }
            });
        }
        return jRadioButton;
    }

    /**
     * This method initializes jRadioButton1
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getJRadioButton1() {
        if (jRadioButton1 == null) {
            jRadioButton1 = new JRadioButton();
            jRadioButton1.setText("process");
            jRadioButton1
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            fTypeCh();
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
            jRadioButton2.setText("process_part");
            jRadioButton2
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            fTypeCh();
                        }
                    });
        }
        return jRadioButton2;
    }

    protected void fTypeCh() {
        if (jRadioButton.isSelected())
            type = Function.TYPE_PROCESS_KOMPLEX;
        else if (jRadioButton1.isSelected())
            type = Function.TYPE_PROCESS;
        else if (jRadioButton2.isSelected())
            type = Function.TYPE_PROCESS_PART;
        else if (jRadioButton3.isSelected())
            type = Function.TYPE_OPERATION;
        else if (jRadioButton4.isSelected())
            type = Function.TYPE_ACTION;
    }

    /**
     * This method initializes jRadioButton3
     *
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getJRadioButton3() {
        if (jRadioButton3 == null) {
            jRadioButton3 = new JRadioButton();
            jRadioButton3.setText("operation");
            jRadioButton3
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            fTypeCh();
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
            jRadioButton4.setText("action");
            jRadioButton4
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            fTypeCh();
                        }
                    });
        }
        return jRadioButton4;
    }

    public int getType() {
        return type;
    }
}

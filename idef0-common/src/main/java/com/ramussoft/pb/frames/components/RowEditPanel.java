/*
 * Created on 27/8/2005
 */
package com.ramussoft.pb.frames.components;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import com.ramussoft.pb.Row;
import com.ramussoft.pb.data.RowEditor;

/**
 * @author ZDD
 */
public class RowEditPanel extends JToolBar implements RowEditor {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JButton jButton2 = null;

    private JButton jButton3 = null;

    private JButton jButton4 = null;

    private JButton jButton5 = null;

    private JButton jButton6 = null;

    private JButton jButton7 = null;

    private RowEditor editor;

    public void setEditor(final RowEditor editor) {
        this.editor = editor;
    }

    /**
     * This is the default constructor
     */
    public RowEditPanel() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.add(getJButton2());
        this.add(getJButton3());
        this.add(getJButton5());
        this.add(getJButton6());
        this.add(getJButton7());
        this.add(getJButton4());
    }

    /**
     * This method initializes jButton2
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton2() {
        if (jButton2 == null) {
            jButton2 = new JButton();
            jButton2.setToolTipText("add_row");
            jButton2.setIcon(new ImageIcon(getClass().getResource(
                    "/images/add.png")));
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    addRow();
                }
            });
        }
        return jButton2;
    }

    /**
     * This method initializes jButton3
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton3() {
        if (jButton3 == null) {
            jButton3 = new JButton();
            jButton3.setToolTipText("insert_rows");
            jButton3.setIcon(new ImageIcon(getClass().getResource(
                    "/images/insert.png")));
            jButton3.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(final ActionEvent arg0) {
                    insertRow();
                }
            });
        }
        return jButton3;
    }

    /**
     * This method initializes jButton4
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton4() {
        if (jButton4 == null) {
            jButton4 = new JButton();
            jButton4.setToolTipText("delete_row");
            jButton4.setIcon(new ImageIcon(getClass().getResource(
                    "/images/remove.png")));
            jButton4.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    removeRows();
                }
            });
        }
        return jButton4;
    }

    /**
     * This method initializes jButton5
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton5() {
        if (jButton5 == null) {
            jButton5 = new JButton();
            jButton5.setToolTipText("add_child");
            jButton5.setIcon(new ImageIcon(getClass().getResource(
                    "/images/add_child.png")));
            jButton5.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    addChild();
                }
            });
        }
        return jButton5;
    }

    /**
     * This method initializes jButton6
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton6() {
        if (jButton6 == null) {
            jButton6 = new JButton();
            jButton6.setToolTipText("level_up");
            jButton6.setIcon(new ImageIcon(getClass().getResource(
                    "/images/left.png")));
            jButton6.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    moveLeft();
                }
            });
        }
        return jButton6;
    }

    /**
     * This method initializes jButton7
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton7() {
        if (jButton7 == null) {
            jButton7 = new JButton();
            jButton7.setToolTipText("level_down");
            jButton7.setIcon(new ImageIcon(getClass().getResource(
                    "/images/right.png")));
            jButton7.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    moveRight();
                }
            });
        }
        return jButton7;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.ListEditor.activeEditor#addChild()
     */
    public void addChild() {
        if (editor != null)
            editor.addChild();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.ListEditor.activeEditor#insertRow()
     */
    public void insertRow() {
        if (editor != null)
            editor.insertRow();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.ListEditor.activeEditor#addRow()
     */
    public Row addRow() {
        if (editor != null) {
            editor.addRow();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.ListEditor.activeEditor#removeRows()
     */
    public void removeRows() {
        if (editor != null) {
            editor.removeRows();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.ListEditor.activeEditor#moveLeft()
     */
    public void moveLeft() {
        if (editor != null)
            editor.moveLeft();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jason.clasificators.elements.data.ListEditor.activeEditor#moveRight()
     */
    public void moveRight() {
        if (editor != null)
            editor.moveRight();
    }
}

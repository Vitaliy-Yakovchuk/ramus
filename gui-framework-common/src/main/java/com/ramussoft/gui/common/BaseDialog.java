package com.ramussoft.gui.common;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

public class BaseDialog extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;

    private JPanel jPanel = null;

    private JPanel jPanel1 = null;

    private JPanel jPanel2 = null;

    private JButton jButton = null;

    private JButton jButton1 = null;

    protected static final double DIV_SPACE = 5;

    private boolean okPressed = false;

    /**
     * This is the default constructor
     */
    public BaseDialog() {
        super();
        initialize();
    }

    /**
     * This is the default constructor
     */
    public BaseDialog(final JDialog dialog) {
        super(dialog);
        initialize();
    }

    /**
     * This is the default constructor
     */
    public BaseDialog(final JFrame frame) {
        super(frame);
        initialize();
    }

    public BaseDialog(final JFrame frame, final boolean modal) {
        super(frame, modal);
        initialize();
    }

    public BaseDialog(final JDialog dialog, final boolean modal) {
        super(dialog, modal);
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setSize(440, 278);
        setModal(true);
        setContentPane(getJContentPane());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJPanel(), java.awt.BorderLayout.SOUTH);
            jContentPane.add(getJPanel1(), java.awt.BorderLayout.CENTER);
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
            final FlowLayout flowLayout1 = new FlowLayout();
            flowLayout1.setAlignment(java.awt.FlowLayout.RIGHT);
            final FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
            jPanel = new JPanel();
            jPanel.setLayout(flowLayout1);
            jPanel.add(getJPanel2(), null);
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
            jPanel1 = new JPanel() {
                /**
                 *
                 */
                private static final long serialVersionUID = -4374988448542530887L;

                @Override
                protected boolean processKeyBinding(final KeyStroke ks,
                                                    final KeyEvent e, final int condition,
                                                    final boolean pressed) {
                    if (!ks.isOnKeyRelease()
                            && ks.getKeyCode() == KeyEvent.VK_ENTER
                            && e.isControlDown()) {
                        onOk();
                        return false;
                    }
                    if (!ks.isOnKeyRelease()
                            && ks.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        onCancel();
                        return false;
                    }
                    return super.processKeyBinding(ks, e, condition, pressed);
                }
            };
            jPanel1.setLayout(new BorderLayout());
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
            final GridLayout gridLayout2 = new GridLayout();
            gridLayout2.setRows(1);
            gridLayout2.setColumns(2);
            gridLayout2.setHgap(5);
            final GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(1);
            gridLayout.setColumns(2);
            gridLayout.setHgap(5);
            jPanel2 = new JPanel();
            jPanel2.setLayout(gridLayout2);
            jPanel2.add(getOKButton(), null);
            jPanel2.add(getCencelButton(), null);
        }
        return jPanel2;
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    public JButton getOKButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setText(GlobalResourcesManager.getString("ok"));
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    onOk();
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
    public JButton getCencelButton() {
        if (jButton1 == null) {
            jButton1 = new JButton();
            jButton1.setText(GlobalResourcesManager.getString("cancel"));
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    onCancel();
                }
            });
        }
        return jButton1;
    }

    protected void onCancel() {
        okPressed = false;
        setVisible(false);
    }

    protected void onOk() {
        okPressed = true;
        setVisible(false);
    }

    public void commitEditors() {
        commitTableEditors(getContentPane());
    }

    private void commitTableEditors(Container con) {
        for (int i = 0; i < con.getComponentCount(); i++) {
            if (con.getComponent(i) instanceof Container)
                commitTableEditors((Container) con.getComponent(i));
            final Component container = con.getComponent(i);
            commitComponent(container);
            if (container instanceof JScrollPane) {
                commitComponent(((JScrollPane) container).getViewport().getView());
            }

        }
    }

    private void commitComponent(final Component container) {
        if (container == null)
            return;
        if (container instanceof JTable) {
            TableCellEditor cellEditor = ((JTable) container)
                    .getCellEditor();
            if (cellEditor != null) {
                try {
                    cellEditor.stopCellEditing();
                } catch (Exception e) {
                    try {
                        cellEditor.cancelCellEditing();
                    } catch (Exception ex) {

                    }
                }
            }
        }
    }

    public JPanel getMainPanel() {
        return getJPanel1();
    }

    private final ActionListener listener = new ActionListener() {

        public void actionPerformed(ActionEvent e) {
            if (getOKButton().isEnabled())
                onOk();
        }

    };

    private void setEditsAction(final Container con) {
        if (con instanceof JSpinner)
            return;

		/*
         * if(con instanceof JSplitPane){
		 * setEditsAction(((JSplitPane)con).getLeftComponent());
		 * setEditsAction(((JSplitPane)con).getRightComponent()); return; }
		 */

        for (int i = 0; i < con.getComponentCount(); i++) {
            if (con.getComponent(i) instanceof Container)
                setEditsAction((Container) con.getComponent(i));
            final Component container = con.getComponent(i);
            if (container instanceof JTextField) {
                processTextField((JTextField) container);
            } else if (container instanceof JPasswordField) {
                ((JPasswordField) container).addActionListener(listener);
            }
            if (container instanceof JTextComponent) {
                addUndoFunctions((JTextComponent) container);
            }
            if (container instanceof JList
                    && !(container instanceof JTableHeader)) {
                ((JList) container).addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(final MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1
                                && e.getClickCount() > 1)
                            onOk();
                    }

                });
            }
        }
    }

    public static UndoManager addUndoFunctions(final JTextComponent field) {
        final UndoManager undo = new UndoManager();
        final Action undoAction = new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = -2907840050588049020L;

            public void actionPerformed(ActionEvent e) {
                if (undo.canUndo())
                    undo.undo();
            }

        };
        final Action redoAction = new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = -1159436411122485685L;

            public void actionPerformed(ActionEvent e) {
                if (undo.canRedo())
                    undo.redo();
            }

        };

        field.getDocument().addUndoableEditListener(new UndoableEditListener() {

            public void undoableEditHappened(final UndoableEditEvent e) {
                undo.addEdit(e.getEdit());
            }

        });
        field.getActionMap().put("undo-action-x", undoAction);
        field.getActionMap().put("redo-action-x", redoAction);
        field.getInputMap()
                .put(
                        KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                                InputEvent.CTRL_DOWN_MASK), "undo-action-x");
        field.getInputMap()
                .put(
                        KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                                InputEvent.CTRL_DOWN_MASK), "redo-action-x");
        return undo;
    }

    private void processTextField(final JTextField field) {
        field.addActionListener(listener);
    }

    public void setMainPane(final JComponent component) {
        getMainPanel().add(component, BorderLayout.CENTER);
        setEditsAction(component);
    }

    protected void centerDialog() {
        setLocationRelativeTo(null);
    }

    protected void setMinSizePack() {
        pack();
        setMinimumSize(getPreferredSize());
    }

    public boolean isOkPressed() {
        return okPressed;
    }

} // @jve:decl-index=0:visual-constraint="10,10"

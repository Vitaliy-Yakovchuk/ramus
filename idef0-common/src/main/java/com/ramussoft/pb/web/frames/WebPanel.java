package com.ramussoft.pb.web.frames;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.HyperlinkEvent;

import com.ramussoft.pb.web.History;

public class WebPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private JToolBar jToolBar = null;
    private JButton jButton = null;
    private JButton jButton1 = null;
    private JButton jButton2 = null;
    private JButton jButton3 = null;
    private JLabel jLabel = null;
    private JTextField jTextField = null;
    private JButton jButton4 = null;
    private JButton jButton5 = null;
    private JScrollPane jScrollPane = null;
    private JEditorPane jEditorPane = null;

    private static final String GO_NEXT = "goNext"; // @jve:decl-index=0:

    private static final String GO_BACK = "goBack"; // @jve:decl-index=0:

    private static final String GO_TO_URL = "goToUrl"; // @jve:decl-index=0:

    private static final String GO_HOME = "goHome"; // @jve:decl-index=0:

    private static final String STOP = "stop"; // @jve:decl-index=0:

    History history = new History();

    private final Vector<AbstractAction> actions = new Vector<AbstractAction>();
    private JLabel jLabel1 = null;

    private Action findAction(final String name) {
        for (int i = 0; i < actions.size(); i++) {
            final AbstractAction action = actions.get(i);
            if (action.getValue(Action.NAME).equals(name))
                return action;
        }
        return null;
    }

    private AbstractAction createAction(final String name, final Icon icon) {
        return createAction(name, name, icon);
    }

    private AbstractAction createAction(final String name, final String text, final Icon icon) {
        final AbstractAction action = new AbstractAction(name, icon) {

            public void actionPerformed(ActionEvent e) {
                WebPanel.this.actionPerformed(e);
            }

        };
        action.putValue(Action.LONG_DESCRIPTION, text);
        action.putValue(Action.SHORT_DESCRIPTION, text);
        action.putValue(Action.DEFAULT, text);
        actions.add(action);
        return action;
    }

    protected void actionPerformed(final ActionEvent e) {
        final String s = e.getActionCommand();
        if (GO_TO_URL.equals(s))
            goToURL(false);
        else if (GO_BACK.equals(s)) {
            jTextField.setText(history.back());
            goToURL(true);
        } else if (GO_NEXT.equals(s)) {
            jTextField.setText(history.next());
            goToURL(true);
        }
    }

    private void goToURL(boolean sys) {
        final String url = jTextField.getText();
        try {
            findAction(STOP).setEnabled(true);
            jEditorPane.setPage(url);
            if (!sys)
                history.go(url);
            findAction(GO_BACK).setEnabled(history.isCanBack());
            findAction(GO_NEXT).setEnabled(history.isCanNext());
        } catch (final IOException e) {
            jEditorPane
                    .setText("<html><body>" + "Помилка завантаження сторінки "
                            + url + "</body></html>");
            findAction(STOP).setEnabled(false);
        }

    }

    /**
     * This is the default constructor
     */
    public WebPanel() {
        super();
        createActions();
        initialize();
    }

    private void createActions() {
        createAction(GO_BACK, null).setEnabled(false);
        createAction(GO_NEXT, null).setEnabled(false);
        createAction(GO_HOME, null);
        createAction(GO_TO_URL, null);
        createAction(STOP, null).setEnabled(false);
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        jLabel1 = new JLabel();
        jLabel1.setText("ok");
        this.setSize(710, 321);
        setLayout(new BorderLayout());
        this.add(getJToolBar(), BorderLayout.NORTH);
        this.add(getJScrollPane(), BorderLayout.CENTER);
        this.add(jLabel1, BorderLayout.SOUTH);
    }

    /**
     * This method initializes jToolBar
     *
     * @return javax.swing.JToolBar
     */
    private JToolBar getJToolBar() {
        if (jToolBar == null) {
            jLabel = new JLabel();
            jLabel.setText(" Address: ");
            jToolBar = new JToolBar();
            jToolBar.add(getJButton());
            jToolBar.add(getJButton1());
            jToolBar.add(getJButton2());
            jToolBar.add(getJButton5());
            jToolBar.add(getJButton3());
            jToolBar.add(jLabel);
            jToolBar.add(getJTextField());
            jToolBar.add(getJButton4());
            jToolBar.addSeparator();
        }
        return jToolBar;
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setFocusable(false);
            jButton.setAction(findAction(GO_BACK));
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
            jButton1.setFocusable(false);
            jButton1.setAction(findAction(GO_NEXT));
        }
        return jButton1;
    }

    /**
     * This method initializes jButton2
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton2() {
        if (jButton2 == null) {
            jButton2 = new JButton();
            jButton2.setFocusable(false);
            jButton2.setText("REF");
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
            jButton3.setFocusable(false);
            jButton3.setAction(findAction(GO_HOME));
        }
        return jButton3;
    }

    /**
     * This method initializes jTextField
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextField() {
        if (jTextField == null) {
            jTextField = new JTextField();
            jTextField.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    WebPanel.this.actionPerformed(new ActionEvent(
                            e.getSource(), e.getID(), GO_TO_URL));
                }
            });

        }
        return jTextField;
    }

    /**
     * This method initializes jButton4
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton4() {
        if (jButton4 == null) {
            jButton4 = new JButton();
            jButton4.setFocusable(false);
            jButton4.setAction(createAction(GO_TO_URL, null));
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
            jButton5.setFocusable(false);
            jButton5.setAction(findAction(STOP));
        }
        return jButton5;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJEditorPane());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jEditorPane
     *
     * @return javax.swing.JEditorPane
     */
    private JEditorPane getJEditorPane() {
        if (jEditorPane == null) {
            jEditorPane = new JEditorPane();
            jEditorPane.setContentType("text/html");
            jEditorPane.setEditable(false);
            jEditorPane
                    .addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
                        public void hyperlinkUpdate(
                                final javax.swing.event.HyperlinkEvent e) {
                            if (e.getEventType() == HyperlinkEvent.EventType.EXITED)
                                jLabel1.setText("ok");
                            else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED)
                                jLabel1.setText(e.getURL().toString());
                            else if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                jTextField.setText(e.getURL().toString());
                                actionPerformed(new ActionEvent(jEditorPane, 0,
                                        GO_TO_URL));
                            }
                        }
                    });
            jEditorPane.addPropertyChangeListener("page",
                    new PropertyChangeListener() {

                        public void propertyChange(final PropertyChangeEvent evt) {
                            findAction(STOP).setEnabled(false);
                        }

                    });
        }
        return jEditorPane;
    }

} // @jve:decl-index=0:visual-constraint="10,10"

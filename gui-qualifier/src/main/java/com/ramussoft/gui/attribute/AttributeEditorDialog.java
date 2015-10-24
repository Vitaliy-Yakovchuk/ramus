package com.ramussoft.gui.attribute;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.table.JTableHeader;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.StringGetter;
import com.ramussoft.gui.common.prefrence.Options;

public class AttributeEditorDialog extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = -8139581256677580838L;

    private AttributeEditorPanel panel;

    private AttributeEditor attributeEditor;

    private ActionListener listener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            panel.ok();
        }
    };

    public AttributeEditorDialog(JDialog d, Engine engine, Attribute attribute,
                                 Element element, GUIFramework framework, AccessRules rules,
                                 Object value) {
        super(d);

        init(engine, attribute, element, framework, rules, value);
    }

    public AttributeEditorDialog(JFrame f, Engine engine, Attribute attribute,
                                 Element element, GUIFramework framework, AccessRules rules,
                                 Object value) {
        super(f);

        init(engine, attribute, element, framework, rules, value);
    }

    protected void init(Engine engine, Attribute attribute, Element element,
                        GUIFramework framework, AccessRules rules, Object value) {
        String t = GlobalResourcesManager
                .getString("AttributeEditorDialog.Title") + " - ";

        String name = framework.getSystemAttributeName(attribute);
        if (name == null)
            name = attribute.getName();
        setTitle(t + element.getName() + " | " + name);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        panel = new AttributeEditorPanel() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            protected void close() {
                super.close();
                AttributeEditorDialog.this.setVisible(false);
                AttributeEditorDialog.this.closed();
            }

            @Override
            protected String getSavePrefix() {
                return "1";
            }

            @Override
            public boolean apply() {
                boolean apply = super.apply();
                if (apply)
                    onApply(this.value);
                return apply;
            }
        };

        AttributePlugin p = framework.findAttributePlugin(attribute);

        attributeEditor = p.getAttributeEditor(engine, rules, element,
                attribute, "attributeEditorDialog", null);
        panel.setAttributeEditor(attributeEditor, engine, attribute, element,
                rules.canUpdateElement(element.getId(), attribute.getId()),
                value);
        JPanel panel = new JPanel(new BorderLayout()) {
            /**
             *
             */
            private static final long serialVersionUID = -8409626746568642304L;

            @Override
            protected boolean processKeyBinding(final KeyStroke ks,
                                                final KeyEvent e, final int condition, final boolean pressed) {
                if (!ks.isOnKeyRelease()
                        && ks.getKeyCode() == KeyEvent.VK_ENTER
                        && e.isControlDown()) {
                    AttributeEditorDialog.this.panel.ok();
                    return false;
                }
                if (!ks.isOnKeyRelease()
                        && ks.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    AttributeEditorDialog.this.panel.cancel();
                    return false;
                }
                return super.processKeyBinding(ks, e, condition, pressed);
            }
        };
        panel.add(this.panel, BorderLayout.CENTER);

        Action[] actions = attributeEditor.getActions();

        if (actions.length > 0) {
            JToolBar bar = new JToolBar();
            for (Action action : actions)
                if (action != null) {
                    String command = (String) action
                            .getValue(Action.ACTION_COMMAND_KEY);
                    JButton button = bar.add(action);
                    button.setFocusable(false);
                    if (action.getValue(Action.SHORT_DESCRIPTION) == null) {
                        String text = null;
                        text = p.getString(command);

                        if (text == null) {
                            StringGetter getter = (StringGetter) action
                                    .getValue(StringGetter.ACTION_STRING_GETTER);
                            if (getter != null)
                                text = getter.getString(command);
                            else
                                text = GlobalResourcesManager
                                        .getString(command);
                        }
                        if (text != null)
                            button.setToolTipText(text);
                    }
                } else
                    bar.addSeparator();
            panel.add(bar, BorderLayout.NORTH);
        }

        this.setContentPane(panel);
        setEditsAction(panel);
        pack();
        setLocationRelativeTo(null);
        Options.loadOptions(this);
        this.panel.loadPreferences();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                AttributeEditorDialog.this.panel.cancel();
                Options.saveOptions(AttributeEditorDialog.this);
            }
        });
    }

    protected void onApply(Object value) {
    }

    protected void closed() {
    }

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
                ((JTextField) container).addActionListener(listener);
            } else if (container instanceof JPasswordField) {
                ((JPasswordField) container).addActionListener(listener);
            }

            if (container instanceof JList
                    && !(container instanceof JTableHeader)) {
                ((JList) container).addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseClicked(final MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON1
                                && e.getClickCount() > 1)
                            panel.ok();
                    }

                });
            }
        }
    }

    @Override
    public void setVisible(boolean b) {
        if (!b) {
            Options.saveOptions(AttributeEditorDialog.this);
        }
        super.setVisible(b);
    }

    public AttributeEditor getAttributeEditor() {
        return attributeEditor;
    }

    public AttributeEditorPanel getPanel() {
        return panel;
    }

    public static void showDialog(GUIFramework framework, Element element,
                                  Attribute attribute) {
        Object value = framework.getEngine().getAttribute(element, attribute);
        AttributeEditorDialog dialog = new AttributeEditorDialog(
                framework.getMainFrame(), framework.getEngine(), attribute,
                element, framework, framework.getAccessRules(), value);
        dialog.setVisible(true);
    }
}

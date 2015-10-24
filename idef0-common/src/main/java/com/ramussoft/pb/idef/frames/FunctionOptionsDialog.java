/*
 * Created on 1/8/2005
 */
package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.data.negine.NFunction;
import com.ramussoft.pb.frames.MainFrame;
import com.ramussoft.pb.frames.components.FunctionType;
import com.ramussoft.pb.frames.components.JFontChooser;
import com.ramussoft.pb.idef.visual.IDEF0Object;

/**
 * @author ZDD
 */
public class FunctionOptionsDialog extends JDialog {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private javax.swing.JPanel jContentPane = null;

    private JPanel jPanel = null;

    private JPanel jPanel1 = null;

    private JPanel jPanel2 = null;

    private JButton jButton = null;

    private JButton jButton1 = null;

    protected JTabbedPane jTabbedPane = null;

    private JPanel jPanel6 = null;

    private JPanel jPanel7 = null;

    private JColorChooser jColorChooser = null;

    private JColorChooser jColorChooser1 = null;

    private Function function = null;

    private JButton jButton2 = null;

    private DataPlugin dataPlugin;

    private int staticTabCount;

    private List<AttributeEditor> attributeEditors = new ArrayList<AttributeEditor>();

    private List<Attribute> attributes;

    private GUIFramework framework;

    private List<Object> values = new ArrayList<Object>();

    private void apptype() {
        Engine e = dataPlugin.getEngine();
        if (e instanceof Journaled)
            ((Journaled) e).startUserTransaction();

        function.setFont(getJFontChooser().getSelFont());
        function.setBackground(getJColorChooser().getColor());
        function.setForeground(getJColorChooser1().getColor());

        final JList jList = selectOwner.getJList();
        if (jList.getSelectedIndex() == 0)
            function.setOwner(null);
        else {
            function.setOwner((Row) jList.getSelectedValue());
        }

        function.setType(functionType.getType());

        AccessRules rules = dataPlugin.getAccessRules();

        Element element = ((NFunction) function).getElement();

        for (int index = attributes.size() - 1; index >= 0; index--) {
            Attribute attribute = attributes.get(index);
            if (rules.canUpdateElement(element.getId(), attribute.getId())) {
                Object value = attributeEditors.get(index).getValue();
                if (!equals(value, values.get(index)))
                    e.setAttribute(element, attribute, value);
            }
        }

        if (e instanceof Journaled)
            ((Journaled) e).commitUserTransaction();
    }

    private boolean equals(Object o1, Object o2) {
        if (o1 == null)
            return o2 == null;
        return o1.equals(o2);
    }

    private void load() {
        Engine e = dataPlugin.getEngine();
        Qualifier qualifier = function.getQualifier();
        attributes = new ArrayList<Attribute>(qualifier.getAttributes());
        for (int i = attributes.size() - 1; i >= 0; i--) {
            Attribute attribute = attributes.get(i);
            AttributePlugin plugin = framework.findAttributePlugin(attribute
                    .getAttributeType());
            Element element = ((NFunction) function).getElement();
            AttributeEditor editor = plugin.getAttributeEditor(e, dataPlugin
                    .getAccessRules(), element, attribute, "function", null);
            if (editor == null)
                continue;
            attributeEditors.add(0, editor);
            JComponent component = editor.getComponent();
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(component, BorderLayout.CENTER);
            Action[] actions = editor.getActions();
            if (actions.length > 0) {
                JToolBar bar = new JToolBar();
                for (Action a : actions) {
                    JButton b = bar.add(a);
                    b.setFocusable(false);
                    bar.add(b);
                }
                panel.add(bar, BorderLayout.NORTH);
            }

            getJTabbedPane().insertTab(attribute.getName(), null, panel, null,
                    0);
            Object value = e.getAttribute(element, attribute);
            editor.setValue(value);
            values.add(0, value);
        }

        getJColorChooser().setColor(function.getBackground());
        getJColorChooser1().setColor(function.getForeground());
        getJFontChooser().setSelFont(function.getFont());
        functionType.setFunction(function);
        selectOwner.setFunction(function);
    }

    public void showModal(final Row row) {
        function = (Function) row;
        load();
        Options.loadOptions("function_options_dialog", this);
        int index = Options.getInteger("DEFAULT_FUNCTION_DIALOG_TAB", 0);
        if (index < getJTabbedPane().getTabCount()) {
            getJTabbedPane().setSelectedIndex(index);
            Component component = getJTabbedPane().getSelectedComponent();
            requestFocusForTextComponent(component);
        }
        setVisible(true);
        Options.saveOptions("function_options_dialog", this);
        Options.setInteger("DEFAULT_FUNCTION_DIALOG_TAB", getJTabbedPane()
                .getSelectedIndex());
        while (getJTabbedPane().getTabCount() > staticTabCount) {
            getJTabbedPane().removeTabAt(0);
        }
        for (AttributeEditor editor : attributeEditors)
            editor.close();

        attributeEditors.clear();
        attributes.clear();
        values.clear();
    }

    private boolean requestFocusForTextComponent(final Component component) {
        if (component instanceof JTextComponent) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    component.requestFocus();
                }
            });
            return true;
        }
        if (component instanceof Container) {
            Container container = (Container) component;
            for (int i = 0; i < container.getComponentCount(); i++) {
                boolean res = requestFocusForTextComponent(container
                        .getComponent(i));
                if (res)
                    return true;
            }
        }
        return false;
    }

    public void showModal(final IDEF0Object function) {
        showModal(function.getFunction());
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel() {
        if (jPanel == null) {
            final FlowLayout flowLayout1 = new FlowLayout();
            jPanel = new JPanel();
            jPanel.setLayout(flowLayout1);
            flowLayout1.setAlignment(java.awt.FlowLayout.RIGHT);
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
            jPanel1 = new JPanel();
            jPanel1.setLayout(new BorderLayout());
            jPanel1.add(getJTabbedPane(), java.awt.BorderLayout.CENTER);
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
            jPanel2 = new JPanel();
            jPanel2.setLayout(gridLayout2);
            gridLayout2.setRows(1);
            gridLayout2.setVgap(0);
            gridLayout2.setHgap(5);
            jPanel2.add(getJButton(), null);
            jPanel2.add(getJButton2(), null);
            jPanel2.add(getJButton1(), null);
        }
        return jPanel2;
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton() {
        if (jButton == null) {
            jButton = new JButton();
            jButton.setText("ok");
            jButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    onOk();
                }
            });
        }
        return jButton;
    }

    protected void onOk() {
        apptype();
        setVisible(false);
    }

    /**
     * This method initializes jButton1
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton1() {
        if (jButton1 == null) {
            jButton1 = new JButton();
            jButton1.setText("cancel");
            jButton1.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    onCancel();
                }
            });
        }
        return jButton1;
    }

    protected void onCancel() {
        setVisible(false);
    }

    /**
     * This method initializes jTabbedPane
     *
     * @return javax.swing.JTabbedPane
     */
    private JTabbedPane getJTabbedPane() {
        if (jTabbedPane == null) {
            jTabbedPane = new JTabbedPane();
            jTabbedPane.addTab(ResourceLoader.getString("font"), null,
                    getJFontChooser(), null);
            jTabbedPane.addTab(ResourceLoader.getString("bk_color"), null,
                    getJPanel6(), null);
            jTabbedPane.addTab(ResourceLoader.getString("fg_color"), null,
                    getJPanel7(), null);
            jTabbedPane.addTab(ResourceLoader.getString("function_type"), null,
                    getFunctionType(), null);
            jTabbedPane.addTab(ResourceLoader.getString("select_owner"), null,
                    getSelectOwner(), null);

            staticTabCount = jTabbedPane.getTabCount();
        }
        return jTabbedPane;
    }

    /**
     * This method initializes jPanel6
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel6() {
        if (jPanel6 == null) {
            jPanel6 = new JPanel();
            jPanel6.setLayout(new BorderLayout());
            jPanel6.add(getJColorChooser(), java.awt.BorderLayout.CENTER);
        }
        return jPanel6;
    }

    /**
     * This method initializes jPanel7
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel7() {
        if (jPanel7 == null) {
            jPanel7 = new JPanel();
            jPanel7.setLayout(new BorderLayout());
            jPanel7.add(getJColorChooser1(), java.awt.BorderLayout.CENTER);
        }
        return jPanel7;
    }

    private JFontChooser jFontChooser = null;

    private FunctionType functionType = null;

    private SelectOwner selectOwner = null;

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
     * This method initializes jColorChooser1
     *
     * @return javax.swing.JColorChooser
     */
    private JColorChooser getJColorChooser1() {
        if (jColorChooser1 == null) {
            jColorChooser1 = new JColorChooser();
        }
        return jColorChooser1;
    }

    /**
     * This method initializes jButton2
     *
     * @return javax.swing.JButton
     */
    private JButton getJButton2() {
        if (jButton2 == null) {
            jButton2 = new JButton();
            jButton2.setText("apptype");
            jButton2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    apptype();
                }
            });
        }
        return jButton2;
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
     * This is the default constructor
     */
    public FunctionOptionsDialog(DataPlugin dataPlugin, GUIFramework framework) {
        super(framework.getMainFrame());
        setModal(true);
        this.framework = framework;
        this.dataPlugin = dataPlugin;
        initialize();
        setLocationRelativeTo(null);
    }

    public FunctionOptionsDialog(final JFrame frame, DataPlugin dataPlugin,
                                 GUIFramework framework) {
        super(framework.getMainFrame(), true);
        this.setIconImage(MainFrame.mainIcon);
        this.dataPlugin = dataPlugin;
        this.framework = framework;
        initialize();
        setLocationRelativeTo(frame);
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setTitle("function_options");
        setModal(true);
        setResizable(true);
        this.setSize(408, 445);
        setContentPane(getJContentPane());
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(final java.awt.event.WindowEvent e) {
                Options.saveOptions("function_options_dialog",
                        FunctionOptionsDialog.this);
            }
        });
        setMinimumSize(new java.awt.Dimension(450, 445));
        ResourceLoader.setJComponentsText(this);
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private javax.swing.JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new javax.swing.JPanel() {
                @Override
                protected boolean processKeyBinding(final KeyStroke ks,
                                                    final KeyEvent e, final int condition,
                                                    final boolean pressed) {
                    if (e.isControlDown()
                            && ks.getKeyCode() == KeyEvent.VK_ENTER) {
                        onOk();
                        return false;
                    }
                    if (ks.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        onCancel();
                        return false;
                    }
                    return super.processKeyBinding(ks, e, condition, pressed);
                }
            };
            jContentPane.setLayout(new java.awt.BorderLayout());
            jContentPane.add(getJPanel(), java.awt.BorderLayout.SOUTH);
            jContentPane.add(getJPanel1(), java.awt.BorderLayout.CENTER);
        }
        return jContentPane;
    }

    /**
     * This method initializes functionType
     *
     * @return com.jason.clasificators.frames.idf.elements.FunctionType
     */
    private FunctionType getFunctionType() {
        if (functionType == null) {
            functionType = new FunctionType();
        }
        return functionType;
    }

    /**
     * This method initializes selectOwner
     *
     * @return com.jason.clasificators.frames.idf.elements.SelectOwner
     */
    private SelectOwner getSelectOwner() {
        if (selectOwner == null) {
            selectOwner = new SelectOwner();
        }
        return selectOwner;
    }

}

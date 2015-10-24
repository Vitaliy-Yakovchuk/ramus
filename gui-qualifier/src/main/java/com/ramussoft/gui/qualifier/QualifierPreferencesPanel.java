package com.ramussoft.gui.qualifier;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.simple.ElementListPersistent;
import com.ramussoft.core.attribute.simple.IconPersistent;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.attribute.AttributePreferenciesDialog;
import com.ramussoft.gui.attribute.ElementListAttributeEditor;
import com.ramussoft.gui.attribute.icon.IconFactory;
import com.ramussoft.gui.attribute.icon.IconPreviewPanel;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.QualifierSetupEditor;
import com.ramussoft.gui.common.QualifierSetupPlugin;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.qualifier.table.RowTreeTable;
import com.ramussoft.gui.qualifier.table.TreeTableNode;

public class QualifierPreferencesPanel extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = -6902560935489096422L;

    private ElementListAttributeEditor attributeEditor;

    private JComboBox box = new JComboBox();

    private Engine engine;

    private Element element;

    private Attribute attribute;

    private GUIFramework framework;

    private IconPreviewPanel open = new IconPreviewPanel(
            IconFactory.getIconsDirectory());

    private IconPreviewPanel closed = new IconPreviewPanel(
            IconFactory.getIconsDirectory());

    private IconPreviewPanel leaf = new IconPreviewPanel(
            IconFactory.getIconsDirectory());

    private AttributeOrderEditPanel attributeOrderEditPanel;

    private QualifierSetupPlugin[] plugins;

    private QualifierSetupEditor[] editors;

    private Qualifier qualifier;

    private AccessRules rules;

    private RowSet rowSet;

    private Attribute attributeId;

    private Action createAttributeAction = new CreateAttributeAction();

    private Action deleteAttributeAction = new DeleteAttributeAction();

    private AttributePreferencesAction attributePreferenciesAction = new AttributePreferencesAction();

    private BaseDialog dialog;

    @SuppressWarnings("unchecked")
    public QualifierPreferencesPanel(final Engine engine, Element element,
                                     GUIFramework framework, AccessRules rules) {
        super(new BorderLayout());
        JPanel panel = new JPanel(new BorderLayout());
        this.engine = engine;
        this.element = element;
        this.rules = rules;
        this.framework = framework;
        this.plugins = framework.getQualifierSetupPlugins();
        this.qualifier = StandardAttributesPlugin.getQualifier(engine, element);
        this.attributeId = (Attribute) engine.getPluginProperty("Core",
                StandardAttributesPlugin.ATTRIBUTE_ID);

        // super(framework.getMainFrame(), GlobalResourcesManager
        // .getString("AttributeEditorDialog.Title"));
        // setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        attribute = (Attribute) engine.getPluginProperty("Core",
                StandardAttributesPlugin.QUALIFIER_ATTRIBUTES);

        AttributePlugin p = framework.findAttributePlugin(attribute);

        attributeEditor = (ElementListAttributeEditor) p.getAttributeEditor(
                engine, rules, element, attribute, "base", null);
        JComponent component = attributeEditor.getComponent();
        clearPopupMenu(component);
        panel.add(component, BorderLayout.CENTER);
        panel.add(createToolBar(), BorderLayout.NORTH);
        attributeEditor.getRowTreeTableComponent().getTable()
                .setComponentPopupMenu(createPopupMenu());

        final RowTreeTable table = attributeEditor.getRowTreeTableComponent()
                .getTable();
        table.setEditIfNullEvent(false);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if ((e.getClickCount() % 2 == 0) && (e.getClickCount() > 0)) {
                        setupAttribute();
                    }
                }
            }

        });
        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        attributePreferenciesAction.setEnabled(table
                                .getSelectedRowCount() > 0);
                    }
                });

        rowSet = attributeEditor.getRowTreeTableComponent().getRowSet();

        List<ElementListPersistent> list = (List<ElementListPersistent>) engine
                .getAttribute(element, attribute);
        attributeEditor.setValue(list);
        double[][] size = {{5, TableLayout.MINIMUM, 5, TableLayout.FILL, 5},
                {5, TableLayout.MINIMUM, 5}};

        JPanel attrForName = new JPanel(new TableLayout(size));

        attrForName.add(
                new JLabel(GlobalResourcesManager
                        .getString("Qualifier.AttributeForName")), "1, 1");
        attrForName.add(box, "3, 1");
        final Attribute attributeId = (Attribute) engine.getPluginProperty(
                "Core", StandardAttributesPlugin.ATTRIBUTE_ID);

        attributeOrderEditPanel = new AttributeOrderEditPanel(
                qualifier.getAttributes());

        for (ElementListPersistent pr : list) {
            Element element2 = engine.getElement(pr.getElement2Id());
            if (element2 != null) {
                Long id2 = (Long) engine.getAttribute(element2, attributeId);
                Attribute attribute = engine.getAttribute(id2);
                if (StandardAttributesPlugin.isNameType(attribute
                        .getAttributeType())) {
                    box.addItem(element2);

                    if (qualifier.getAttributeForName() == id2)
                        box.setSelectedItem(element2);
                }
            }
        }

        box.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                box.removeAllItems();

                List<ElementListPersistent> list = (List<ElementListPersistent>) attributeEditor
                        .getValue();
                for (ElementListPersistent pr : list) {
                    Element element2 = engine.getElement(pr.getElement2Id());
                    Long id2 = (Long) engine
                            .getAttribute(element2, attributeId);
                    Attribute attribute = engine.getAttribute(id2);
                    if (StandardAttributesPlugin.isNameType(attribute
                            .getAttributeType())) {
                        box.addItem(element2);

                        if (qualifier.getAttributeForName() == id2)
                            box.setSelectedItem(element2);
                    }
                }

                JComboBox box = (JComboBox) e.getSource();
                Object comp = box.getUI().getAccessibleChild(box, 0);
                if (!(comp instanceof JPopupMenu))
                    return;
                JComponent scrollPane = (JComponent) ((JPopupMenu) comp)
                        .getComponent(0);
                Dimension size = new Dimension();
                size.width = box.getPreferredSize().width;
                size.height = scrollPane.getPreferredSize().height;
                scrollPane.setPreferredSize(size);
            }

        });

        box.setPreferredSize(new Dimension(200, box.getPreferredSize().height));

        panel.add(attrForName, BorderLayout.SOUTH);

        final JTabbedPane pane = new JTabbedPane();

        pane.addTab(GlobalResourcesManager.getString("attributes"), panel);
        pane.addTab(GlobalResourcesManager.getString("AttributesOrder"),
                attributeOrderEditPanel);
        pane.addTab(GlobalResourcesManager.getString("Open.Icons"), open);
        pane.addTab(GlobalResourcesManager.getString("Closed.Icons"), closed);
        pane.addTab(GlobalResourcesManager.getString("Leaf.Icons"), leaf);

        final int staticTabCount = pane.getTabCount();

        addPluginPreferences(pane);

        pane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (pane.getSelectedIndex() == 1) {
                    updateAttributesInOrder(qualifier.getAttributes());

                    attributeOrderEditPanel.refresh();
                } else if (pane.getSelectedIndex() >= staticTabCount) {
                    updateAttributesInOrder(qualifier.getAttributes());
                    int editorIndex = pane.getSelectedIndex() - staticTabCount;
                    editors[editorIndex].load(engine, qualifier);
                }
            }

        });

        this.add(pane, BorderLayout.CENTER);

        attributeEditor.getRowTreeTableComponent().getTable()
                .getTreeSelectionModel()
                .addTreeSelectionListener(new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        refreshActions();
                    }
                });
        refreshActions();
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        for (Action action : getActions()) {
            if (action == null)
                menu.addSeparator();
            else {
                action.putValue(Action.NAME, GlobalResourcesManager
                        .getString((String) action
                                .getValue(Action.ACTION_COMMAND_KEY)));
                menu.add(action);
            }
        }
        return menu;
    }

    protected void refreshActions() {
        RowTreeTable table = attributeEditor.getRowTreeTableComponent()
                .getTable();

        createAttributeAction.setEnabled(rules.canCreateAttribute());
        if (table.getTreeSelectionModel().getSelectionPath() == null) {
            deleteAttributeAction.setEnabled(false);
        } else {
            boolean e = true;
            TreePath[] paths = table.getTreeSelectionModel()
                    .getSelectionPaths();
            for (TreePath path : paths) {
                Row row = ((TreeTableNode) path.getLastPathComponent())
                        .getRow();
                if (row == null) {
                    e = false;
                    break;
                }
                if (row.getChildCount() > 0) {
                    e = false;
                    break;
                }

                Long long1 = (Long) row.getAttribute(attributeId);
                if (long1 == null)
                    break;
                long attrId = long1;

                if (!rules.canDeleteAttribute(attrId)) {
                    e = false;
                    break;
                }
            }
            deleteAttributeAction.setEnabled(e);
        }
    }

    private Component createToolBar() {
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        for (Action action : getActions())
            if (action == null)
                bar.addSeparator();
            else
                bar.add(action).setFocusable(false);
        return bar;
    }

    private Action[] getActions() {
        return new Action[]{createAttributeAction, new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 3284967628905643862L;

            {
                this.putValue(ACTION_COMMAND_KEY, "Action.SortByName");
                this.putValue(
                        SMALL_ICON,
                        new ImageIcon(getClass().getResource(
                                "/com/ramussoft/gui/table/sort-incr.png")));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                rowSet.startUserTransaction();
                rowSet.sortByName();
                rowSet.commitUserTransaction();

            }
        }, deleteAttributeAction, null, attributePreferenciesAction};
    }

    private class CreateAttributeAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -2190162251129929137L;

        public CreateAttributeAction() {
            this.putValue(ACTION_COMMAND_KEY, "CreateAttribute");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/add.png")));
            this.putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (dialog == null)
                new AttributePreferenciesDialog(framework).setVisible(true);
            else
                new AttributePreferenciesDialog(framework, dialog)
                        .setVisible(true);
        }

    }

    ;

    private class DeleteAttributeAction extends AbstractAction {

        public DeleteAttributeAction() {
            this.putValue(ACTION_COMMAND_KEY, "DeleteAttribute");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/delete.png")));
            this.putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        }

        /**
         *
         */
        private static final long serialVersionUID = 895892525217269346L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (JOptionPane.showConfirmDialog(framework.getMainFrame(),
                    GlobalResourcesManager
                            .getString("DeleteActiveElementsDialog.Warning"),
                    UIManager.getString("OptionPane.titleText"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                TreePath[] paths = attributeEditor.getRowTreeTableComponent()
                        .getTable().getTreeSelectionModel().getSelectionPaths();
                if (paths.length == 0) {
                    System.err
                            .println("Trying to delete element, but no elements are selected");
                    return;
                }
                for (TreePath path : paths) {
                    Row row = ((TreeTableNode) path.getLastPathComponent())
                            .getRow();
                    if (row == null) {
                        System.err
                                .println("Trying to delete node, which conatain no row");
                        return;
                    }
                    long attrId = (Long) row.getAttribute(attributeId);
                    rowSet.startUserTransaction();
                    engine.deleteAttribute(attrId);
                    rowSet.commitUserTransaction();
                }
            }
        }

    }

    private void addPluginPreferences(JTabbedPane pane) {
        editors = new QualifierSetupEditor[plugins.length];
        for (int i = 0; i < editors.length; i++) {
            editors[i] = plugins[i].getSetupEditor();
            editors[i].load(engine, qualifier);
            pane.addTab(editors[i].getTitle(), editors[i].createComponent());
        }
    }

    private void clearPopupMenu(JComponent component) {
        component.setComponentPopupMenu(null);
        for (int i = 0; i < component.getComponentCount(); i++) {
            Component c = component.getComponent(i);
            if (c instanceof JComponent)
                clearPopupMenu((JComponent) c);
        }
    }

    @SuppressWarnings("unchecked")
    private void updateAttributesInOrder(List<Attribute> qAttributes) {

        Attribute attributeId = (Attribute) engine.getPluginProperty("Core",
                StandardAttributesPlugin.ATTRIBUTE_ID);

        List<ElementListPersistent> list = (List<ElementListPersistent>) attributeEditor
                .getValue();
        List<Attribute> attributes = new ArrayList<Attribute>(list.size());
        for (ElementListPersistent pr : list) {
            Element element2 = engine.getElement(pr.getElement2Id());
            Long id2 = (Long) engine.getAttribute(element2, attributeId);
            Attribute attribute = engine.getAttribute(id2);
            attributes.add(attribute);
            if (qAttributes.indexOf(attribute) < 0)
                qAttributes.add(attribute);
        }

        List<Attribute> rem = new ArrayList<Attribute>();

        for (Attribute a : qAttributes)
            if (attributes.indexOf(a) < 0)
                rem.add(a);
        for (Attribute r : rem)
            qAttributes.remove(r);
    }

    public boolean ok() {

        if (box.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, GlobalResourcesManager
                    .getString("Error.SetAttributeForName"));
            return false;
        }

        String[] errors = new String[]{};

        for (QualifierSetupEditor editor : editors) {
            String[] add = editor.getErrors();
            int start = errors.length;
            errors = Arrays.copyOf(errors, errors.length + add.length);
            for (int i = start; i < errors.length; i++) {
                errors[i] = add[i - start];
            }
        }
        if (errors.length > 0) {

            StringBuffer sb = new StringBuffer();
            sb.append("<html><body>");
            for (String error : errors) {
                sb.append(error);
                sb.append("<br>");
            }
            sb.append("</body></html>");
            JOptionPane.showMessageDialog(this, sb.toString());
            return false;
        }

        if (engine instanceof Journaled) {
            ((Journaled) engine).startUserTransaction();
        }

        engine.setAttribute(element, attribute, attributeEditor.getValue());
        long id = (Long) engine.getAttribute(element, (Attribute) engine
                .getPluginProperty("Core",
                        StandardAttributesPlugin.QUALIFIER_ID));
        Qualifier qualifier = engine.getQualifier(id);

        updateAttributesInOrder(attributeOrderEditPanel.getAttributes());

        qualifier.setAttributes(attributeOrderEditPanel.getAttributes());

        if (box.getSelectedItem() == null) {
            qualifier.setAttributeForName(-1l);
        } else {
            Attribute attributeId = (Attribute) engine.getPluginProperty(
                    "Core", StandardAttributesPlugin.ATTRIBUTE_ID);
            Long id2 = (Long) engine.getAttribute(
                    (Element) box.getSelectedItem(), attributeId);
            Attribute a = engine.getAttribute(id2);
            qualifier.setAttributeForName(a.getId());
        }
        engine.updateQualifier(qualifier);

        IconPersistent selectedValue = open.getSelectedValue();
        if (selectedValue != null)
            IconFactory.setOpenIcon(engine, selectedValue, qualifier);
        selectedValue = leaf.getSelectedValue();
        if (selectedValue != null)
            IconFactory.setLeafIcon(engine, selectedValue, qualifier);
        selectedValue = closed.getSelectedValue();
        if (selectedValue != null)
            IconFactory.setClosedIcon(engine, selectedValue, qualifier);

        for (QualifierSetupEditor editor : editors) {
            editor.save(engine, qualifier);
        }

        if (engine instanceof Journaled) {
            ((Journaled) engine).commitUserTransaction();
        }
        return true;
    }

    public void showDialog() {
        dialog = new BaseDialog(framework.getMainFrame(), true) {
            /**
             *
             */
            private static final long serialVersionUID = -8447199076378835917L;

            @Override
            protected void onOk() {
                if (QualifierPreferencesPanel.this.ok())
                    super.onOk();
            }
        };
        dialog.setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass()
                        .getResource("/com/ramussoft/gui/table/qualifier.png")));
        dialog.setTitle(GlobalResourcesManager
                .getString("QialifierPreferenciesDialog.Title"));
        dialog.setMainPane(this);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        Options.loadOptions("QualifierPreferencesDialog", dialog);

        dialog.getOKButton().setEnabled(
                rules.canUpdateQualifier(qualifier.getId()));

        dialog.setVisible(true);
        Options.saveOptions("QualifierPreferencesDialog", dialog);
        attributeEditor.close();
    }

    private class AttributePreferencesAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5171624095557878838L;

        public AttributePreferencesAction() {
            this.putValue(ACTION_COMMAND_KEY, "AttributePreferencies");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/preferencies.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setupAttribute();
        }

    }

    public void setupAttribute() {
        Row row = attributeEditor.getSelectedRow();
        if (row == null)
            return;

        AttributePreferenciesDialog d;
        if (dialog == null)
            d = new AttributePreferenciesDialog(framework);
        else
            d = new AttributePreferenciesDialog(framework, dialog);
        Attribute attribute = engine.getAttribute((Long) row
                .getAttribute(attributeId));
        d.setAttribute(attribute);
        d.getOKButton().setEnabled(rules.canUpdateAttribute(attribute.getId()));
        d.setVisible(true);
    }

    ;

}

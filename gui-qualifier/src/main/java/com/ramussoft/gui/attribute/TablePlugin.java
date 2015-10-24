package com.ramussoft.gui.attribute;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreePath;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.simple.TableGroupablePropertyPersistent;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.attribute.table.TableEditor;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.AttributePreferenciesEditor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.QualifierImporter;
import com.ramussoft.gui.qualifier.AttributeOrderEditPanel;
import com.ramussoft.gui.qualifier.table.SelectableTableView;
import com.ramussoft.gui.qualifier.table.TableNode;
import com.ramussoft.gui.qualifier.table.TabledAttributePlugin;
import com.ramussoft.gui.qualifier.table.TreeTableNode;
import com.ramussoft.gui.qualifier.table.ValueGetter;
import com.ramussoft.gui.qualifier.table.event.Closeable;

public class TablePlugin extends AbstractAttributePlugin implements
        TabledAttributePlugin {

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Core", "Table", false);
    }

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        return null;
    }

    @Override
    public AttributePreferenciesEditor getAttributePreferenciesEditor() {
        return new AttributePreferenciesEditor() {

            private SelectableTableView view;

            private AttributeOrderEditPanel attributeOrderEditPanel = new AttributeOrderEditPanel(
                    new ArrayList<Attribute>(), true);

            private Action createAttributeAction = new AbstractAction() {

                /**
                 *
                 */
                private static final long serialVersionUID = -2190162251129929137L;

                {
                    this.putValue(ACTION_COMMAND_KEY, "CreateAttribute");
                    this.putValue(SMALL_ICON, new ImageIcon(getClass()
                            .getResource("/com/ramussoft/gui/table/add.png")));
                    this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                            KeyEvent.VK_ADD, KeyEvent.CTRL_MASK));
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    new AttributePreferenciesDialog(framework).setVisible(true);
                }

            };

            private Action deleteAttributeAction = new AbstractAction() {

                {
                    this.putValue(ACTION_COMMAND_KEY, "DeleteAttribute");
                    this.putValue(SMALL_ICON,
                            new ImageIcon(getClass().getResource(
                                    "/com/ramussoft/gui/table/delete.png")));
                    this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                            KeyEvent.VK_DELETE, 0));
                }

                /**
                 *
                 */
                private static final long serialVersionUID = 895892525217269346L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (JOptionPane
                            .showConfirmDialog(
                                    framework.getMainFrame(),
                                    GlobalResourcesManager
                                            .getString("DeleteActiveElementsDialog.Warning"),
                                    UIManager.getString("OptionPane.titleText"),
                                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        TreePath[] paths = view.getComponent().getTable()
                                .getTreeSelectionModel().getSelectionPaths();
                        if (paths.length == 0) {
                            System.err
                                    .println("Trying to delete element, but no elements are selected");
                            return;
                        }
                        for (TreePath path : paths) {
                            Row row = ((TreeTableNode) path
                                    .getLastPathComponent()).getRow();
                            if (row == null) {
                                System.err
                                        .println("Trying to delete node, which conatain no row");
                                return;
                            }
                            long attrId = (Long) row
                                    .getAttribute((Attribute) view
                                            .getComponent()
                                            .getRowSet()
                                            .getEngine()
                                            .getPluginProperty(
                                                    "Core",
                                                    StandardAttributesPlugin.ATTRIBUTE_ID));
                            view.getComponent().getRowSet()
                                    .startUserTransaction();
                            view.getComponent().getRowSet().getEngine()
                                    .deleteAttribute(attrId);
                            view.getComponent().getRowSet()
                                    .commitUserTransaction();
                        }
                    }
                }

            };

            @Override
            public void apply(Attribute attribute, Engine engine,
                              AccessRules accessRules) {

                String qualifierName = StandardAttributesPlugin
                        .getTableQualifeirName(attribute);
                Qualifier qualifier = engine.getSystemQualifier(qualifierName);
                if (qualifier == null) {
                    qualifier = engine.createSystemQualifier();
                    qualifier.setName(qualifierName);
                }

                List<Attribute> attributes = attributeOrderEditPanel
                        .getAttributes();
                fillAttributes(engine, attributes);

                qualifier.setAttributes(attributes);

                qualifier.getSystemAttributes().clear();
                qualifier.getSystemAttributes().add(
                        StandardAttributesPlugin
                                .getTableElementIdAttribute(engine));
                engine.updateQualifier(qualifier);

                List<TableGroupablePropertyPersistent> list = new ArrayList<TableGroupablePropertyPersistent>();
                int i = 0;
                for (String group : attributeOrderEditPanel
                        .getAttributeGroups()) {
                    if (group != null) {
                        TableGroupablePropertyPersistent p = new TableGroupablePropertyPersistent();
                        p.setAttribute(attribute.getId());
                        p.setName(group);
                        p.setOtherAttribute(attributes.get(i).getId());
                        list.add(p);
                    }
                    i++;
                }
                engine.setAttribute(null, attribute, list);
            }

            private void fillAttributes(Engine engine,
                                        List<Attribute> attributes) {
                List<Row> rows = view.getSelectedRows();
                ArrayList<Attribute> toRemove = new ArrayList<Attribute>(
                        attributes);
                for (Row row : rows) {
                    Attribute attr = StandardAttributesPlugin.getAttribute(
                            engine, row.getElement());
                    toRemove.remove(attr);
                    if (attributes.indexOf(attr) < 0)
                        attributes.add(attr);
                }
                for (Attribute attribute : toRemove)
                    attributes.remove(attribute);
            }

            @Override
            public boolean canApply() {
                return view.getSelectedRows().size() > 0;
            }

            @SuppressWarnings("unchecked")
            @Override
            public JComponent createComponent(Attribute attribute,
                                              final Engine engine, AccessRules accessRules) {
                view = new SelectableTableView(framework, engine, accessRules,
                        StandardAttributesPlugin.getAttributesQualifier(engine)) {
                    @Override
                    protected Attribute[] getAttributes() {
                        return new Attribute[]{
                                StandardAttributesPlugin
                                        .getAttributeNameAttribute(engine),
                                StandardAttributesPlugin
                                        .getAttributeTypeNameAttribute(engine)};
                    }
                };

                JComponent component = view.createComponent();

                view.getComponent().getModel().setEditable(0, false);
                view.getComponent().getModel().setEditable(1, false);

                JTabbedPane pane = new JTabbedPane();

                pane.addTab(GlobalResourcesManager.getString("attributes"),
                        component);
                pane.addTab(
                        GlobalResourcesManager.getString("AttributesOrder"),
                        attributeOrderEditPanel);

                pane.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        fillAttributes(engine, attributeOrderEditPanel
                                .getAttributes());
                        attributeOrderEditPanel.refresh();
                    }
                });

                if (attribute != null) {
                    Qualifier qualifier = StandardAttributesPlugin
                            .getTableQualifierForAttribute(engine, attribute);

                    attributeOrderEditPanel.setAttributes(qualifier
                            .getAttributes());

                    Hashtable<Attribute, String> groups = new Hashtable<Attribute, String>();
                    List<TableGroupablePropertyPersistent> pList = (List) engine
                            .getAttribute(null, attribute);
                    for (TableGroupablePropertyPersistent p : pList)
                        for (Attribute attribute2 : qualifier.getAttributes()) {
                            if ((p.getOtherAttribute() == attribute2.getId())
                                    && (p.getName() != null))
                                groups.put(attribute2, p.getName());
                        }

                    attributeOrderEditPanel.setGroups(groups);

                    List<Row> list = view.getComponent().getRowSet()
                            .getAllRows();
                    for (Row row : list) {
                        Attribute attr = StandardAttributesPlugin.getAttribute(
                                engine, row.getElement());
                        if (qualifier.getAttributes().indexOf(attr) >= 0) {
                            view.setSelectedRow(row, true);
                        }
                    }
                }

                view.getComponent().getTable().setComponentPopupMenu(
                        createPopupMenu());

                return pane;
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

            private Action[] getActions() {
                return new Action[]{createAttributeAction,
                        new AbstractAction() {

                            /**
                             *
                             */
                            private static final long serialVersionUID = 3284967628905643862L;

                            {
                                this.putValue(ACTION_COMMAND_KEY,
                                        "Action.SortByName");
                                this
                                        .putValue(
                                                SMALL_ICON,
                                                new ImageIcon(
                                                        getClass()
                                                                .getResource(
                                                                        "/com/ramussoft/gui/table/sort-incr.png")));
                            }

                            @Override
                            public void actionPerformed(ActionEvent e) {
                                view.getComponent().getRowSet()
                                        .startUserTransaction();
                                view.getComponent().getRowSet().sortByName();
                                view.getComponent().getRowSet()
                                        .commitUserTransaction();

                            }
                        }, deleteAttributeAction};
            }

        };
    }

    @Override
    public AttributeEditor getAttributeEditor(Engine engine, AccessRules rules,
                                              Element element, Attribute attribute, AttributeEditor old) {
        if (old != null)
            old.close();
        return new TableEditor(engine, rules, element, attribute, framework);
    }

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    public ValueGetter getValueGetter(Attribute attribute, Engine engine,
                                      GUIFramework framework, Closeable model) {
        return new ValueGetter() {
            @Override
            public Object getValue(TableNode node, int index) {
                return GlobalResourcesManager
                        .getString("AttributeType.Core.Table");
            }
        };
    }

    @Override
    public Attribute createSyncAttribute(Engine engine,
                                         QualifierImporter importer, Attribute sourceAttribute) {
        return null;
    }
}

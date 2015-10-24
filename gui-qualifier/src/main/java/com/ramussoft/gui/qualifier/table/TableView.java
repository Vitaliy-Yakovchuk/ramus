package com.ramussoft.gui.qualifier.table;

import java.awt.event.ActionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.attribute.AttributeEditorView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.StringGetter;
import com.ramussoft.gui.common.TabView;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.qualifier.table.event.SelectionEvent;
import com.ramussoft.gui.qualifier.table.event.SelectionListener;

import static com.ramussoft.gui.common.StringGetter.ACTION_STRING_GETTER;

public abstract class TableView extends AbstractTableView implements TabView {

    protected AttributeEditorView.ElementAttribute elementAttribute = new AttributeEditorView.ElementAttribute();

    private List<Element> elementsToHide;

    private Object activeElement;

    private Object activeAttribute;

    private ActionListener beforeSaveListener = new ActionListener() {

        @Override
        public void onAction(com.ramussoft.gui.common.event.ActionEvent event) {
            getProperties().store(TableView.this.engine, TableView.this);

        }
    };

    private AbstractAction tablePreferences = new AbstractAction() {
        /**
         *
         */
        private static final long serialVersionUID = 3105527827224308366L;

        {
            putValue(Action.ACTION_COMMAND_KEY, "TableViewPreferencies");
            putValue(
                    Action.SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/table-preferencies.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            new TableViewPreferenciesDialog(framework.getMainFrame(),
                    TableView.this).setVisible(true);
        }
    };

    public TableView(GUIFramework framework, Engine engine,
                     AccessRules accessor, Qualifier qualifier) {
        super(framework, engine, accessor, qualifier);

        framework.addActionListener("BeforeFileSave", beforeSaveListener);
    }

    public TableView(GUIFramework framework, Qualifier qualifier) {
        this(framework, framework.getEngine(), framework.getAccessRules(),
                qualifier);
    }

    @Override
    public JComponent createComponent() {
        JComponent res = super.createComponent();
        component.getTable().addSelectionListener(new SelectionListener() {
            @Override
            public void changeSelection(SelectionEvent event) {
                TableView.this.tableSelectedValueChanged();
            }
        });
        return res;
    }

    @Override
    protected void createInnerComponent() {
        super.createInnerComponent();
        if (elementsToHide != null)
            component.getModel().setHideElements(elementsToHide);
        component.getTable().setComponentPopupMenu(createPopupMenu());
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                getProperties().setComponentLook(engine, TableView.this);
            }
        });

    }

    protected JPopupMenu createPopupMenu() {
        Action[] actions = getActions();
        return createPopupMenu(actions);
    }

    public static JPopupMenu createPopupMenu(Action[] actions) {
        JPopupMenu menu = new JPopupMenu();
        for (Action a : actions) {
            if (a != null) {
                StringGetter getter = (StringGetter) a
                        .getValue(ACTION_STRING_GETTER);

                if (getter != null)
                    menu.add(a).setText(
                            getter.getString((String) a
                                    .getValue(Action.ACTION_COMMAND_KEY)));
                else
                    menu.add(a).setText(
                            GlobalResourcesManager.getString((String) a
                                    .getValue(Action.ACTION_COMMAND_KEY)));
            } else
                menu.addSeparator();
        }
        return menu;
    }

    protected void tableSelectedValueChanged() {
        int column = table.getSelectedColumn();

        TreeTableNode node = table.getSelectedNode();

        if ((column < 0) && (node != null)) {
            column = table.getHierarchicalColumn();
        }

        if ((column >= 0) && (node != null)) {

            Row row = node.getRow();
            column = table.convertColumnIndexToModel(column);

            if (row != null) {
                long id = row.getElementId();

                Attribute attribute = component.getRowSet().getAttributes()[column];

                elementAttribute.setFindAction(component.getTable()
                        .isFindSelectionChanged());

                if ((elementAttribute.element == null)
                        || (elementAttribute.element.getId() != id)) {
                    framework.propertyChanged(ACTIVATE_ELEMENT,
                            row.getElement());
                    this.activeElement = row.getElement();
                }

                elementAttribute = new AttributeEditorView.ElementAttribute(
                        row.getElement(), attribute);
                elementAttribute.setFindAction(component.getTable()
                        .isFindSelectionChanged());
                framework.propertyChanged(ACTIVATE_ATTRIBUTE, elementAttribute);
                activeAttribute = elementAttribute;
            } else {
                activeAttribute = null;
                elementAttribute = new AttributeEditorView.ElementAttribute();
                this.activeElement = null;
                if ((table.getSelectedRow() >= 0)
                        && (table.getSelectedColumn() >= 0)) {
                    framework.propertyChanged(ACTIVATE_ELEMENT, null);
                    framework.propertyChanged(ACTIVATE_ATTRIBUTE, null);
                }
            }
        }

    }

    @Override
    public void focusGained() {
        super.focusGained();
        if (activeElement != null)
            framework.propertyChanged(ACTIVATE_ELEMENT, activeElement);
        if (elementAttribute != activeAttribute) {
            if (activeAttribute != null)
                framework.propertyChanged(ACTIVATE_ATTRIBUTE, activeAttribute);
        } else {
            AttributeEditorView.ElementAttribute o = new AttributeEditorView.ElementAttribute(
                    elementAttribute.element, elementAttribute.attribute,
                    elementAttribute.value);
            o.setFindAction(true);
            framework.propertyChanged(ACTIVATE_ATTRIBUTE, o);
        }
    }

    @Override
    protected Attribute[] getAttributes() {
        Attribute[] attributes = super.getAttributes();

        List<Attribute> attrs = new ArrayList<Attribute>(attributes.length);
        TableViewProperties properties = getProperties();

        Qualifier q = getQualifier();

        int index = -1;
        for (int i = 0; i < attributes.length; i++) {
            if (attributes[i].getId() == q.getAttributeForName()) {
                index = i;
                break;
            }
        }

        if (index > 0) {
            Attribute a = attributes[index];
            attributes[index] = attributes[0];
            attributes[0] = a;
        }

        for (Attribute attribute : attributes) {
            if ((!properties.isPresent(attribute))
                    || (attribute.getId() == q.getAttributeForName())) {
                attrs.add(attribute);
            }
        }
        return attrs.toArray(new Attribute[attrs.size()]);
    }

    public TableViewProperties getProperties() {
        TableViewProperties properties = new TableViewProperties();
        properties.load(engine, this);
        return properties;
    }

    public void setProperties(TableViewProperties properties) {
        properties.store(engine, this);
        componentRefresh();
    }

    @Override
    public Action[] getActions() {
        Action[] actions = super.getActions();
        actions = Arrays.copyOf(actions, actions.length + 2);

        actions[actions.length - 1] = tablePreferences;
        return actions;
    }

    @Override
    public void close() {
        super.close();
        getProperties().store(engine, this);
        framework.removeActionListener("BeforeFileSave", beforeSaveListener);
    }

    @Override
    public void fullRefresh() {
        getProperties().store(engine, this);
        super.fullRefresh();
    }

    @Override
    protected RootCreater getRootCreater() {
        TableViewProperties ps = getProperties();
        if (ps.getActiveHierarchy() < 0)
            return super.getRootCreater();
        return new GroupRootCreater(
                ps.getHierarchies()[ps.getActiveHierarchy()]);
    }

    public void setHideElements(List<Element> elementsToHide) {
        this.elementsToHide = elementsToHide;
        if (component != null) {
            component.getModel().setHideElements(elementsToHide);
        }
    }

    public String getPropertiesPrefix() {
        return "";
    }

    public AttributeEditorView.ElementAttribute getElementAttribute() {
        return elementAttribute;
    }

    public AbstractAction getTablePreferencesAction() {
        return tablePreferences;
    }
}

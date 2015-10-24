package com.ramussoft.gui.qualifier;

import java.awt.BorderLayout;
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
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.simple.HierarchicalPersistent;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowChildAdapter;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.database.common.RowSet.RootRow;
import com.ramussoft.database.common.RowSet.RowCreater;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.qualifier.table.RootCreater;
import com.ramussoft.gui.qualifier.table.RowRootCreater;
import com.ramussoft.gui.qualifier.table.TableView;
import com.ramussoft.gui.qualifier.table.TreeTableNode;

public class QualifierView extends TableView implements UniqueView {

    private Attribute nameAttribute;

    private Engine engine;

    public final static long EDIT_NAME_CLICK_DELAY = 1000;

    protected int[] lastSelectedRows;

    protected long lastClickTime;

    private Attribute qAttribute;

    private Action openQualifier = new OpenQualifierAction();

    private ConvertQualifierToElementsAction convertQualifierToElementsAction = new ConvertQualifierToElementsAction();

    private QualifierListener listener = new QualifierAdapter() {
        @Override
        public void qualifierUpdated(QualifierEvent event) {
            if (event.getNewQualifier().equals(
                    StandardAttributesPlugin.getQualifiersQualifier(engine))) {
                qualifier = event.getNewQualifier();
                fullRefresh();
                component.repaint();
            }
        }
    };

    public QualifierView(GUIFramework framework) {
        this(framework, framework.getEngine(), framework.getAccessRules());
    }

    public QualifierView(GUIFramework framework, Engine engine,
                         AccessRules accessor) {
        super(framework, engine, accessor, (Qualifier) engine
                .getPluginProperty("Core",
                        StandardAttributesPlugin.QUALIFIERS_QUALIFIER));
        this.nameAttribute = (Attribute) engine.getPluginProperty("Core",
                StandardAttributesPlugin.ATTRIBUTE_NAME);
        this.qAttribute = (Attribute) engine.getPluginProperty("Core",
                StandardAttributesPlugin.QUALIFIER_ID);
        this.engine = engine;
        engine.addQualifierListener(listener);
    }

    private class OpenQualifierAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 1328041697585385438L;

        public OpenQualifierAction() {
            putValue(ACTION_COMMAND_KEY, "Action.OpenQualifier");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/open.png")));
            this.putValue(
                    ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK
                            | KeyEvent.SHIFT_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            openQualifier();
        }

    }

    ;

    @Override
    public JComponent createComponent() {
        JComponent component = super.createComponent();
        addListeners();
        return component;
    }

    @Override
    protected RootCreater getRootCreater() {
        return new RowRootCreater() {
            @Override
            public RowCreater getRowCreater() {
                return new RowSet.RowCreater() {

                    @Override
                    public Row createRow(Element element, RowSet data,
                                         Attribute[] attributes, Object[] objects) {
                        if (element == null) {
                            return new RootRow(qualifier, data, attributes,
                                    objects);
                        }

                        return new Row(element, data, attributes, objects) {
                            @Override
                            public boolean canAddChild() {
                                if (getChildCount() > 0)
                                    return true;
                                long id = StandardAttributesPlugin
                                        .getQualifierId(engine, element);
                                return engine.getElementCountForQualifier(id) == 0;
                            }
                        };
                    }

                };
            }
        };
    }

    protected void addListeners() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if ((e.getClickCount() % 2 == 0) && (e.getClickCount() > 0)) {
                        openQualifier();
                    } else {
                        if ((e.getClickCount() == 1)
                                && (System.currentTimeMillis() - lastClickTime < EDIT_NAME_CLICK_DELAY)
                                && (Arrays.equals(lastSelectedRows,
                                table.getSelectedRows()))) {
                            if (!table.isEditing()) {
                                editTableField();
                            }
                        } else {
                            lastClickTime = System.currentTimeMillis();
                            lastSelectedRows = table.getSelectedRows();
                        }
                    }
                }
            }

        });
        table.setEditIfNullEvent(false);
        table.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0),
                "EditCell");
        table.getActionMap().put("EditCell", new AbstractAction() {
            /**
             *
             */
            private static final long serialVersionUID = 3229634866196074563L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if ((table.getSelectedRow() >= 0)
                        && (table.getSelectedColumn() >= 0))
                    editTableField();
            }
        });
    }

    protected void openQualifier() {
        for (int i : table.getSelectedRows()) {
            if (i >= 0) {
                TreePath path = table.getPathForRow(i);
                if (path != null) {
                    TreeTableNode node = (TreeTableNode) path
                            .getLastPathComponent();
                    if (node != null) {
                        Row row = node.getRow();
                        if (row != null) {
                            if (row.getChildCount() == 0) {
                                table.setEditable(false);
                                openQualifier(row);
                                table.setEditable(true);
                            }
                        }
                    }
                }
            }
        }
    }

    private void openQualifier(Row row) {
        Long l = (Long) row.getAttribute(qAttribute);
        if (l == null)
            return;
        Qualifier qualifier = engine.getQualifier(l);
        framework.propertyChanged(QualifierPlugin.OPEN_QUALIFIER, qualifier);
    }

    public Qualifier getSelectedQualifier() {
        TreeTableNode node = table.getSelectedNode();
        if (node != null) {
            Row row = node.getRow();
            if (row != null) {
                Long l = (Long) row.getAttribute(qAttribute);
                return engine.getQualifier(l);
            }
        }
        return null;
    }

    public void setSelectedQualifier(Qualifier qualifier) {
        int count = table.getRowCount();
        for (int i = 0; i < count; i++) {
            TreeTableNode node = (TreeTableNode) table.getPathForRow(i)
                    .getLastPathComponent();
            if (node.getRow() != null) {
                Long l = (Long) node.getRow().getAttribute(qAttribute);
                if (l == qualifier.getId()) {
                    table.setSelectedRows(new int[]{i}, true);
                    return;
                }
            }
        }

    }

    protected void editTableField() {
        table.editCellAt(table.getSelectedRow(), table.getSelectedColumn());
    }

    @Override
    public String getId() {
        return "QualifierView";
    }

    @Override
    protected Attribute[] getAttributes() {
        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(nameAttribute);
        for (Attribute attribute : qualifier.getAttributes())
            attributes.add(attribute);
        return attributes.toArray(new Attribute[attributes.size()]);
    }

    @Override
    protected void tableSelectedValueChanged() {
    }

    protected Action qualifierPreferenciesAction = new AbstractAction() {

        {
            putValue(ACTION_COMMAND_KEY, "QualifierPreferencies");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/preferencies.png")));
            this.putValue(
                    ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK
                            | KeyEvent.SHIFT_MASK));
        }

        private static final long serialVersionUID = 3203562427255330027L;

        @Override
        public void actionPerformed(ActionEvent e) {

            TreePath[] paths = table.getTreeSelectionModel()
                    .getSelectionPaths();
            if (paths.length == 0) {
                System.err
                        .println("Trying to change element, but no elements are selected");
                return;
            }
            for (TreePath path : paths) {
                Row row = ((TreeTableNode) path.getLastPathComponent())
                        .getRow();
                if (row == null) {
                    System.err
                            .println("Trying to change attributes for node, which conatain no row");
                    return;
                }
                Element element = engine.getElement(row.getElementId());
                QualifierPreferencesPanel dialog = new QualifierPreferencesPanel(
                        engine, element, framework, accessRules);
                dialog.showDialog();
            }

        }

    };

    @Override
    protected void refreshActions() {
        super.refreshActions();
        TreePath selectionPath = table.getTreeSelectionModel()
                .getSelectionPath();
        boolean anySelected = selectionPath != null;
        qualifierPreferenciesAction.setEnabled(anySelected);

        Row sRow = null;

        if (selectionPath != null)
            sRow = ((TreeTableNode) selectionPath.getLastPathComponent())
                    .getRow();

        if (sRow == null)
            convertQualifierToElementsAction.setEnabled(false);
        else {
            if ((sRow.getChildCount() == 0)
                    || (table.getTreeSelectionModel().getSelectionPaths().length > 1))
                convertQualifierToElementsAction.setEnabled(false);
            else
                convertQualifierToElementsAction
                        .setEnabled(canDeleteLastLeveleOfQualifiers(sRow,
                                qAttribute));
        }

        openQualifier.setEnabled(anySelected && sRow != null
                && sRow.getChildCount() == 0);
        if (deleteElementAction.isEnabled()) {
            TreePath[] paths = table.getTreeSelectionModel()
                    .getSelectionPaths();
            boolean e = true;
            for (TreePath path : paths) {
                Row row = ((TreeTableNode) path.getLastPathComponent())
                        .getRow();
                if (engine.getElement(row.getElementId()) == null)
                    row = null;
                if (row == null) {
                    e = false;
                    break;
                }
                if (row.getChildCount() > 0) {
                    e = false;
                    break;
                }
                Long attribute = (Long) row.getAttribute(qAttribute);
                if ((attribute != null)
                        && (!accessRules.canDeleteQualifier(attribute))) {
                    e = false;
                    break;
                }
            }
            deleteElementAction.setEnabled(e);
        }
    }

    private boolean canDeleteLastLeveleOfQualifiers(Row sRow,
                                                    Attribute qAttribute2) {
        boolean result = true;
        for (Row row : toArray(sRow.getChildren())) {
            if (row.getChildCount() == 0) {
                Long qualifierId = (Long) row.getAttribute(qAttribute2);
                if (!accessRules.canUpdateQualifier(qualifierId))
                    return false;
            } else {
                boolean r = canDeleteLastLeveleOfQualifiers(row, qAttribute2);
                if (!r)
                    return false;
            }
        }
        return result;
    }

    @Override
    public Action[] getActions() {
        Action[] actions = super.getActions();
        Action[] res = Arrays.copyOf(actions, actions.length);
        int j = 0;
        boolean add = true;
        for (int i = 0; i < res.length; i++) {
            if ((res[i] instanceof JoinElements)
                    || (res[i] instanceof SetElementQualifierAction)) {
                if (add) {
                    add = false;
                    res[j] = convertQualifierToElementsAction;
                    j++;
                }

            } else {
                res[j] = res[i];
                j++;
            }
        }
        res[res.length - 2] = res[1];
        res[res.length - 1] = qualifierPreferenciesAction;

        res[1] = openQualifier;

        return res;
    }

    @Override
    public String getDefaultWorkspace() {
        return "Workspace.Qualifiers";
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    protected void createInnerComponent() {
        super.createInnerComponent();
        ((AbstractTableModel) component.getTable().getModel())
                .fireTableStructureChanged();
        component.getTable().setLeafIcon(
                new ImageIcon(getClass().getResource(
                        "/com/ramussoft/gui/table/qualifier.png")));
        getComponent().getRowSet().addRowChildListener(new RowChildAdapter() {

            @Override
            public void added(Row parent, Row row, int index) {
                if (parent.getParent() == null)
                    return;
                framework.propertyChanged(
                        "CloseQualifier",
                        StandardAttributesPlugin.getQualifierId(engine,
                                parent.getElementId()));
            }
        });
    }

    private class ConvertQualifierToElementsAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5271883849764905261L;

        public ConvertQualifierToElementsAction() {
            putValue(ACTION_COMMAND_KEY, "Action.ConvertQualifierToElements");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(
                            getClass()
                                    .getResource(
                                            "/com/ramussoft/gui/table/qualifier-to-element.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath selectionPath = table.getTreeSelectionModel()
                    .getSelectionPath();

            Row sRow = null;

            if (selectionPath != null)
                sRow = ((TreeTableNode) selectionPath.getLastPathComponent())
                        .getRow();

            if (sRow == null)
                return;
            else if ((sRow.getChildCount() == 0)
                    || (table.getTreeSelectionModel().getSelectionPaths().length > 1))
                return;

            String message = "<html><body>"
                    + GlobalResourcesManager
                    .getString("Warning.QualifiersWillBeConverted")
                    + "<hr>"
                    + GlobalResourcesManager.getString("QualifierView")
                    + ":<br><table>" + getChildTable(sRow)
                    + "</table></body></html>";

            if (JOptionPane.showConfirmDialog(framework.getMainFrame(),
                    message, UIManager.getString("OptionPane.titleText"),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                return;
            final Row row = sRow;

            framework.showAnimation(GlobalResourcesManager
                    .getString("Wait.DataProcessing"));
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        ((Journaled) engine).startUserTransaction();
                        convertToElements(row);
                        ((Journaled) engine).commitUserTransaction();
                    } catch (final Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                JOptionPane.showMessageDialog(
                                        framework.getMainFrame(),
                                        ex.getLocalizedMessage());
                            }
                        });

                        ((Journaled) engine).rollbackUserTransaction();
                    }
                    framework.hideAnimation();
                }
            });
            thread.start();
        }

    }

    public void convertToElements(Row sRow) {
        Qualifier dest = StandardAttributesPlugin.getQualifier(engine,
                sRow.getElement());
        Attribute nameAttribute = null;

        for (Row row : toArray(sRow.getChildren())) {
            nameAttribute = addArrributes(dest.getAttributes(), row,
                    nameAttribute);
        }

        if (nameAttribute == null) {
            for (Attribute attribute : dest.getAttributes())
                if (attribute.getAttributeType().toString().equals("Core.Text"))
                    nameAttribute = attribute;
        }

        if (nameAttribute != null)
            dest.setAttributeForName(nameAttribute.getId());

        engine.updateQualifier(dest);
        long prevId = -1l;

        for (Row row : sRow.getChildren().toArray(
                new Row[sRow.getChildren().size()])) {
            moveElements(row, -1l, prevId, dest);
            prevId = row.getElementId();
        }
    }

    private Attribute addArrributes(List<Attribute> attributes, Row row,
                                    Attribute nameAttribute) {
        Qualifier qualifier = StandardAttributesPlugin.getQualifier(engine,
                row.getElement());
        for (Attribute attribute : qualifier.getAttributes()) {
            if (attributes.indexOf(attribute) < 0)
                attributes.add(attribute);
            if ((nameAttribute == null)
                    && (attribute.getId() == qualifier.getAttributeForName())) {
                nameAttribute = attribute;
            }
        }
        for (Row row2 : toArray(row.getChildren()))
            addArrributes(attributes, row2, nameAttribute);
        return nameAttribute;
    }

    private void moveElements(Row row, long parentElementId, long prevParentId,
                              Qualifier dest) {
        long qualifierId = StandardAttributesPlugin.getQualifierId(engine,
                row.getElement());
        framework.propertyChanged("CloseQualifier", qualifierId);
        Attribute hAttribute = StandardAttributesPlugin
                .getHierarchicalAttribute(engine);
        List<Element> elements = engine.getElements(qualifierId);
        for (Element element : elements) {
            engine.setElementQualifier(element.getId(), dest.getId());

            element.setQualifierId(dest.getId());

            HierarchicalPersistent hp = (HierarchicalPersistent) engine
                    .getAttribute(element, hAttribute);
            if ((hp != null) && (hp.getParentElementId() == -1l)) {
                hp.setParentElementId(row.getElementId());
                engine.setAttribute(element, hAttribute, hp);
            }
        }
        long prevId = -1l;

        for (Row row2 : toArray(row.getChildren())) {
            moveElements(row2, row.getElementId(), prevId, dest);
            prevId = row2.getId();
        }

        String name = row.getName();
        engine.setElementQualifier(row.getElementId(), dest.getId());
        HierarchicalPersistent hp = new HierarchicalPersistent();
        hp.setPreviousElementId(prevParentId);
        hp.setParentElementId(parentElementId);
        engine.setAttribute(row.getElement(), hAttribute, hp);
        Attribute nameAttribute = engine.getAttribute(dest
                .getAttributeForName());
        if (nameAttribute != null)
            engine.setAttribute(row.getElement(), nameAttribute, name);
    }

    private Row[] toArray(List<Row> children) {
        return children.toArray(new Row[children.size()]);
    }

    private void setChildTable(Row sRow, StringBuffer sb) {
        for (Row row : sRow.getChildren()) {
            sb.append("<tr>");
            sb.append("<td>");
            sb.append(row.getCode());
            sb.append("</td>");
            sb.append("<td>");
            sb.append(row.getName());
            sb.append("</td>");
            sb.append("</tr>");
            setChildTable(row, sb);
        }
    }

    ;

    private String getChildTable(Row sRow) {
        StringBuffer sb = new StringBuffer();
        setChildTable(sRow, sb);
        return sb.toString();
    }

    @Override
    public void close() {
        super.close();
        engine.removeQualifierListener(listener);
    }

    @Override
    public String getDefaultPosition() {
        return BorderLayout.WEST;
    }
}

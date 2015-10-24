package com.ramussoft.gui.qualifier.table;

import java.awt.BorderLayout;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.DeleteStatusList;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.event.AttributeAdapter;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.AttributeListener;
import com.ramussoft.common.event.QualifierAdapter;
import com.ramussoft.common.event.QualifierEvent;
import com.ramussoft.common.event.QualifierListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.simple.IconPersistent;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.StandardFilePlugin;
import com.ramussoft.gui.attribute.icon.IconFactory;
import com.ramussoft.gui.attribute.icon.IconPreviewPanel;
import com.ramussoft.gui.common.AbstractView;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.TabView;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.common.print.HTMLPrintable;
import com.ramussoft.gui.qualifier.Commands;
import com.ramussoft.gui.qualifier.QualifierSelectComponent;

public abstract class AbstractTableView extends AbstractView implements
        Commands {

    protected CreateElementAction createElementAction = new CreateElementAction();

    protected DeleteElementAction deleteElementAction = new DeleteElementAction();

    protected CollapseAction collapseAction = new CollapseAction();

    protected ExpandAction expandAction = new ExpandAction();

    protected CreateChildElementAction createChildElementAction = new CreateChildElementAction();

    protected SetElementIconAction setElementIconAction = new SetElementIconAction();

    protected SortByName sortByName = new SortByName();

    protected SelectUnconnected selectUnconnected = new SelectUnconnected();

    protected JoinElements joinElements = new JoinElements();

    private SetElementQualifierAction setElementQualifierAction = new SetElementQualifierAction();

    protected Engine engine;

    protected Qualifier qualifier;

    protected AccessRules accessRules;

    private RowSet rowSet;

    protected RowTreeTable table;

    protected RowTreeTableComponent component;

    private JPanel panel = new JPanel(new BorderLayout());

    private QualifierListener qualifierListener = new QualifierAdapter() {
        @Override
        public void qualifierUpdated(QualifierEvent event) {
            if (event.getNewQualifier().equals(qualifier)) {
                AbstractTableView.this
                        .qualifierUpdated(event.getNewQualifier());
                IconFactory.clearQualifierIconsBuffer(event.getEngine());
                component.repaint();
            }
        }
    };

    private AttributeListener attributeListener = new AttributeAdapter() {
        @Override
        public void attributeUpdated(AttributeEvent event) {
            for (Attribute attribute : rowSet.getAttributes()) {
                if (attribute.equals(event.getAttribute())) {
                    attribute.setName(event.getNewValue().toString());
                    fullRefresh();
                }
            }
        }
    };

    private ActionListener fullRefreshListener = new ActionListener() {
        @Override
        public void onAction(com.ramussoft.gui.common.event.ActionEvent event) {
            fullRefresh();
        }
    };

    public void fullRefresh() {
        Qualifier q = engine.getQualifier(qualifier.getId());
        if (q == null)
            close();
        else {
            qualifier = q;
            componentRefresh();
        }
    }

    protected void componentRefresh() {
        try {
            component.getRowSet().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        createComponent();
    }

    public AbstractTableView(GUIFramework framework, Engine engine,
                             AccessRules accessor, Qualifier qualifier) {
        super(framework);
        this.engine = engine;
        this.qualifier = qualifier;
        this.accessRules = accessor;
        framework.addActionListener(FULL_REFRESH, fullRefreshListener);
        engine.addQualifierListener(qualifierListener);
        engine.addAttributeListener(attributeListener);
    }

    public void close() {
        framework.removeActionListener(FULL_REFRESH, fullRefreshListener);
        try {
            engine.removeQualifierListener(qualifierListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            engine.removeAttributeListener(attributeListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        component.getModel().close();
    }

    @Override
    public JComponent createComponent() {
        createInnerComponent();
        return panel;
    }

    protected void createInnerComponent() {
        panel.removeAll();
        Attribute[] attributes = getAttributes();
        RootCreater rootCreater = getRootCreater();

        component = new RowTreeTableComponent(engine, qualifier, accessRules,
                rootCreater, attributes, framework);

        rowSet = component.getRowSet();
        table = component.getTable();

        table.getTreeSelectionModel().addTreeSelectionListener(
                new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        refreshActions();
                    }
                });
        refreshActions();
        panel.add(component, BorderLayout.CENTER);
        panel.revalidate();
        panel.repaint();
    }

    protected void refreshActions() {
        boolean canCreateElement = accessRules.canCreateElement(qualifier
                .getId());
        createElementAction.setEnabled(canCreateElement);
        sortByName
                .setEnabled(accessRules.canUpdateQualifier(qualifier.getId()));
        if (table.getTreeSelectionModel().getSelectionPath() == null) {
            deleteElementAction.setEnabled(false);
            createChildElementAction.setEnabled(false);
            joinElements.setEnabled(false);
            setElementIconAction.setEnabled(false);
        } else {
            boolean e = true;
            boolean i = true;
            TreePath[] paths = table.getTreeSelectionModel()
                    .getSelectionPaths();

            long[] ids = new long[paths.length];

            boolean first = true;

            for (int j = 0; j < ids.length; j++) {
                Row row = ((TreeTableNode) paths[j].getLastPathComponent())
                        .getRow();
                if (row == null) {
                    e = false;
                    i = false;
                    break;
                }

                if (row.getChildCount() > 0) {
                    e = false;
                    break;
                }

                ids[j] = row.getElementId();

                if (first) {
                    first = false;
                    if (!accessRules.canUpdateAttribute(row.getElement()
                            .getQualifierId(), row.getRowSet().getHAttribute()
                            .getId())) {
                        i = false;
                    }
                }
            }
            if (e) {
                if (!accessRules.canDeleteElements(ids)) {
                    e = false;
                }
            }
            deleteElementAction.setEnabled(e);
            setElementQualifierAction.setEnabled(e);
            joinElements.setEnabled(e);
            createChildElementAction.setEnabled(canCreateElement);
            setElementIconAction.setEnabled(i);
        }
    }

    protected Attribute[] getAttributes() {
        Attribute[] attributes = qualifier.getAttributes().toArray(
                new Attribute[qualifier.getAttributes().size()]);
        return attributes;
    }

    protected RootCreater getRootCreater() {
        return new RowRootCreater();
    }

    @Override
    public Action[] getActions() {
        return new Action[]{createElementAction, setElementIconAction,
                sortByName, selectUnconnected, joinElements,
                setElementQualifierAction, /*
                                             * createChildElementAction,
											 */
                deleteElementAction, null, expandAction, collapseAction};
    }

    protected class CreateElementAction extends AbstractAction {

        public CreateElementAction() {
            this.putValue(ACTION_COMMAND_KEY, "CreateElement");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/add.png")));
            this.putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_ADD, KeyEvent.CTRL_MASK));
        }

        /**
         *
         */
        private static final long serialVersionUID = -5284012805486357491L;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (beforeRowCreate()) {
                createRow();
            }
        }
    }

    public Row createRow() {
        Row root = rowSet.getRoot();

        TreePath path = table.getTreeSelectionModel().getSelectionPath();
        if (path != null) {
            TreeTableNode node = (TreeTableNode) path.getLastPathComponent();
            if (node.getRow() != null) {
                if (node.getRow().getParent() != null)
                    root = node.getRow().getParent();
            }
        }
        return rowSet.createRow(root);
    }

    protected class CreateChildElementAction extends AbstractAction {

        public CreateChildElementAction() {
            this.putValue(ACTION_COMMAND_KEY, "CreateChildElement");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/add-child.png")));
        }

        /**
         *
         */
        private static final long serialVersionUID = -5284012805486357491L;

        @Override
        public void actionPerformed(ActionEvent e) {
            Row root = rowSet.getRoot();

            TreePath path = table.getTreeSelectionModel().getSelectionPath();
            if (path != null) {
                TreeTableNode node = (TreeTableNode) path
                        .getLastPathComponent();
                if (node.getRow() != null) {
                    root = node.getRow();
                }
            }
            rowSet.createRow(root);
        }
    }

    protected class SetElementIconAction extends AbstractAction {
        /**
         *
         */
        private static final long serialVersionUID = -8380866817844775216L;

        public SetElementIconAction() {
            this.putValue(ACTION_COMMAND_KEY, "SetElementIcon");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/set-icon.png")));
            this.putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath[] paths = table.getTreeSelectionModel()
                    .getSelectionPaths();
            if (paths.length == 0) {
                System.err
                        .println("Trying to set element icon, but no elements are selected");
                return;
            }

            IconPersistent p = null;

            IconPreviewPanel panel = new IconPreviewPanel(
                    IconFactory.getIconsDirectory());
            p = panel.select(framework.getMainFrame());
            if (p != null) {

                component.getRowSet().startUserTransaction();

                for (TreePath path : paths) {
                    Row row = ((TreeTableNode) path.getLastPathComponent())
                            .getRow();
                    if (row != null) {
                        IconFactory.setIcon(engine, p, row);
                    }
                }

                component.getRowSet().commitUserTransaction();
            }

        }
    }

    ;

    /**
     * @return Selected (not chacked row)
     */
    public Row getSelectedRow() {
        TreeTableNode node = table.getSelectedNode();
        if (node == null)
            return null;
        return node.getRow();
    }

    protected class SortByName extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -8699279349736829371L;

        public SortByName() {
            this.putValue(ACTION_COMMAND_KEY, "Action.SortByName");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/sort-incr.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            component.getRowSet().startUserTransaction();
            component.getRowSet().sortByName();
            component.getRowSet().commitUserTransaction();
            fullRefresh();
        }
    }

    ;

    protected class SelectUnconnected extends AbstractAction {
        /**
         *
         */
        private static final long serialVersionUID = -1173048315785554892L;

        public SelectUnconnected() {
            this.putValue(ACTION_COMMAND_KEY, "Action.SelectUnconnected");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/select-unconnected.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            table.expandAll();
            table.clearSelection();
            for (int i = 0; i < table.getRowCount(); i++) {
                TreePath path = table.getPathForRow(i);
                Row row = ((TreeTableNode) path.getLastPathComponent())
                        .getRow();
                long[] elementIds = new long[1];
                if ((row != null) && (row.getChildCount() == 0)) {
                    elementIds[0] = row.getElementId();
                    if ((accessRules.canDeleteElements(elementIds))
                            && (accessRules.getElementsDeleteStatusList(
                            elementIds).size() == 0))
                        table.getTreeSelectionModel().addSelectionPath(path);
                }
            }
            table.requestFocus();
        }
    }

    ;

    protected class JoinElements extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = 7511253862730487588L;

        public JoinElements() {
            putValue(ACTION_COMMAND_KEY, "Action.JoinElements");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/join-elements.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath[] paths = table.getTreeSelectionModel()
                    .getSelectionPaths();
            List<Row> list = SelectableTableView
                    .showRowSelectDialog(component, framework, engine,
                            accessRules, qualifier, SelectType.RADIO);
            if ((list != null) && (list.size() == 1)) {
                Element element = list.get(0).getElement();
                ArrayList<Element> elements = new ArrayList<Element>(
                        paths.length);
                for (TreePath path : paths) {
                    Row row = ((TreeTableNode) path.getLastPathComponent())
                            .getRow();
                    if (row != null)
                        if (!element.equals(row.getElement())) {
                            elements.add(row.getElement());
                        }
                }
                component.getRowSet().startUserTransaction();
                engine.replaceElements(
                        elements.toArray(new Element[elements.size()]), element);
                component.getRowSet().commitUserTransaction();
            }
        }

    }

    ;

    protected class DeleteElementAction extends AbstractAction {

        public DeleteElementAction() {
            this.putValue(ACTION_COMMAND_KEY, "DeleteElement");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/delete.png")));
            // this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
            // KeyEvent.VK_DELETE, 0));
        }

        /**
         *
         */
        private static final long serialVersionUID = -5284012805486357491L;

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath[] paths = table.getTreeSelectionModel()
                    .getSelectionPaths();
            if (paths.length == 0) {
                System.err
                        .println("Trying to delete element, but no elements are selected");
                return;
            }

            long[] elementIds = new long[paths.length];
            for (int i = 0; i < elementIds.length; i++) {
                Row row = ((TreeTableNode) paths[i].getLastPathComponent())
                        .getRow();
                if (row == null) {
                    System.err
                            .println("Trying to delete node, which conatain no row");
                    return;
                }
                elementIds[i] = row.getElementId();
            }

            DeleteStatusList list = accessRules
                    .getElementsDeleteStatusList(elementIds);
            if (list.size() > 0) {
                if (!StatusMessageFormat.deleteElements(list, null, framework))
                    return;
            } else {
                if (JOptionPane
                        .showConfirmDialog(
                                component,
                                GlobalResourcesManager
                                        .getString("DeleteActiveElementsDialog.Warning"),
                                GlobalResourcesManager
                                        .getString("ConfirmMessage.Title"),
                                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                    return;
            }

            component.getRowSet().startUserTransaction();

            for (long elementId : elementIds)
                engine.deleteElement(elementId);

            component.getRowSet().commitUserTransaction();
        }
    }

    protected class CollapseAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -2888510142746145088L;

        public CollapseAction() {
            this.putValue(ACTION_COMMAND_KEY, "CollapseAll");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/collapse.png")));
            this.putValue(
                    ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_MASK
                            | KeyEvent.SHIFT_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            table.collapseAll();
        }

    }

    protected class ExpandAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -1143583852789406865L;

        public ExpandAction() {
            this.putValue(ACTION_COMMAND_KEY, "ExpandAll");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/expand.png")));
            this.putValue(
                    ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_MASK
                            | KeyEvent.SHIFT_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            table.expandAll();
        }

    }

    public Qualifier getQualifier() {
        return qualifier;
    }

    protected boolean beforeRowCreate() {
        return true;
    }

    public RowTreeTableComponent getComponent() {
        return component;
    }

    private Attribute[] getSortedAttributes(List<Attribute> list) {
        Attribute[] res = list.toArray(new Attribute[list.size()]);
        Arrays.sort(res, new Comparator<Attribute>() {
            @Override
            public int compare(Attribute o1, Attribute o2) {
                if (o1.getId() < o2.getId())
                    return -1;
                if (o1.getId() > o2.getId())
                    return 1;
                return 0;
            }
        });
        return res;
    }

    public void qualifierUpdated(Qualifier newQualifier) {
        Attribute[] oldAs = getSortedAttributes(qualifier.getAttributes());
        Attribute[] newAs = getSortedAttributes(newQualifier.getAttributes());
        if ((!Arrays.equals(oldAs, newAs))
                || (qualifier.getAttributeForName() != newQualifier
                .getAttributeForName())) {
            qualifier = newQualifier;
            fullRefresh();
        }
    }

    protected class SetElementQualifierAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5168999627602603222L;

        public SetElementQualifierAction() {
            putValue(ACTION_COMMAND_KEY, "Action.SetElementQualifier");
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(
                            getClass()
                                    .getResource(
                                            "/com/ramussoft/gui/table/set-element-qualifier.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final QualifierSelectComponent component = new QualifierSelectComponent(
                    engine, true, false);

            BaseDialog bd = new BaseDialog(framework.getMainFrame(), true) {

                /**
                 *
                 */
                private static final long serialVersionUID = -8463457256203708106L;

                @Override
                protected void onOk() {
                    List<Qualifier> list = component.getSelected();
                    if (list.size() <= 0) {
                        JOptionPane.showMessageDialog(framework.getMainFrame(), GlobalResourcesManager.getString("CheckQualifierFirst"));
                        return;
                    }
                    if (!list.get(0).equals(getQualifier()))
                        if (list.size() > 0) {
                            TreePath[] paths = table.getTreeSelectionModel()
                                    .getSelectionPaths();
                            if (paths.length == 0) {
                                System.err
                                        .println("Trying to set element's qualifier, but no elements are selected");
                                return;
                            }

                            List<Row> rows = new ArrayList<Row>(paths.length);
                            for (int i = 0; i < paths.length; i++) {
                                Row row = ((TreeTableNode) paths[i]
                                        .getLastPathComponent()).getRow();
                                if (row != null) {
                                    rows.add(row);
                                }

                            }
                            ((Journaled) engine).startUserTransaction();
                            for (Row row : rows) {
                                rowSet.setRowQualifier(row, list.get(0));
                            }
                            ((Journaled) engine).commitUserTransaction();
                        }
                    super.onOk();
                }
            };

            bd.setMainPane(component);
            bd.setTitle(GlobalResourcesManager
                    .getString("Action.SetElementQualifier"));

            bd.pack();
            bd.setLocationRelativeTo(null);

            Options.loadOptions(bd);

            bd.setVisible(true);
            Options.saveOptions(bd);
        }

    }

    ;

    @Override
    public String[] getGlobalActions() {
        return new String[]{StandardFilePlugin.ACTION_PRINT,
                StandardFilePlugin.ACTION_PAGE_SETUP,
                StandardFilePlugin.ACTION_PRINT_PREVIEW};
    }

    @Override
    public void onAction(com.ramussoft.gui.common.event.ActionEvent event) {
        if (event.getKey().equals(StandardFilePlugin.ACTION_PAGE_SETUP))
            new HTMLPrintable().pageSetup(framework);
        else if (event.getKey().equals(StandardFilePlugin.ACTION_PRINT)) {
            HTMLPrintable printable = new HTMLPrintable();
            init(printable);
            try {
                printable.print(framework);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(framework.getMainFrame(),
                        e.getLocalizedMessage());
                e.printStackTrace();
            }
        } else if (event.getKey().equals(
                StandardFilePlugin.ACTION_PRINT_PREVIEW)) {
            HTMLPrintable printable = new HTMLPrintable();
            init(printable);
            framework.printPreview(printable);
        }
    }

    private void init(HTMLPrintable printable) {
        try {
            String title;
            if (this instanceof UniqueView) {
                title = getString(((UniqueView) this).getId());
            } else if (this instanceof TabView) {
                title = ((TabView) this).getTitle();
            } else {
                title = getQualifier().getName();
            }

            OutputStream os = printable.getOutputStream();
            String top = "<html>\n<head>\n<title>" + title
                    + "</title>\n</head>\n\n<body>\n";
            os.write(top.getBytes());

            os.write(table.getBodyText(title).getBytes());
            os.write("</body>\n</html>\n".getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Action getExpandAction() {
        return expandAction;
    }

    public Action getCollapseAction() {
        return collapseAction;
    }

    public Action getSortByNameAction() {
        return sortByName;
    }

    public Action getCreateElementAction() {
        return createElementAction;
    }

    public DeleteElementAction getDeleteElementAction() {
        return deleteElementAction;
    }
}

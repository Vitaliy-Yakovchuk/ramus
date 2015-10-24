package com.ramussoft.gui.attribute.table;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.CalculateInfo;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.simple.TableGroupablePropertyPersistent;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.attribute.TableEditorActionModifier;
import com.ramussoft.gui.attribute.AttributeEditorView.ElementAttribute;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GUIPlugin;
import com.ramussoft.gui.eval.SetFormulaDialog;
import com.ramussoft.gui.qualifier.table.RowTransferHandle;
import com.ramussoft.gui.qualifier.table.SelectType;
import com.ramussoft.gui.qualifier.table.SelectableTableView;

public class TableEditor extends AbstractAttributeEditor {

    private TableEditorModel model;

    private TableEditorTable editorTable;

    private GUIFramework framework;

    private Element element;

    private Attribute attribute;

    private JScrollPane pane;

    private TableHeader header;

    private boolean canEdit;

    @SuppressWarnings("unchecked")
    public TableEditor(Engine engine, AccessRules accessRules, Element element,
                       Attribute attribute, GUIFramework framework) {
        this.framework = framework;
        this.element = element;
        this.attribute = attribute;
        Qualifier qualifier = StandardAttributesPlugin
                .getTableQualifierForAttribute(engine, attribute);
        model = new TableEditorModel(qualifier.getAttributes(),
                StandardAttributesPlugin.getOrderedTableElements(engine, attribute,
                        element), attribute, element, framework);
        canEdit = accessRules.canUpdateElement(element.getId(), attribute.getId());
        editorTable = new TableEditorTable(qualifier.getAttributes(), framework) {
            /**
             *
             */
            private static final long serialVersionUID = 638572466289190725L;

            @Override
            public void changeSelection(int rowIndex, int columnIndex,
                                        boolean toggle, boolean extend) {
                super.changeSelection(rowIndex, columnIndex, toggle, extend);
                setFormulaAction.setEnabled(((rowIndex >= 0)
                        && (columnIndex >= 0) && (model.isEditable())));
                removeRowsAction.setEnabled(setFormulaAction.isEnabled());
                moveToOtherElementAction.setEnabled(setFormulaAction
                        .isEnabled());
                copyToOtherElementAction.setEnabled(moveToOtherElementAction
                        .isEnabled());
                if (!canEdit) {
                    addRowAction.setEnabled(false);
                    removeRowsAction.setEnabled(false);
                    setFormulaAction.setEnabled(false);
                    moveToOtherElementAction.setEnabled(false);
                    copyToOtherElementAction.setEnabled(false);
                }
            }
        };

        List<TableGroupablePropertyPersistent> list = (List) engine
                .getAttribute(null, attribute);
        Hashtable<String, ColumnGroup> columns = new Hashtable<String, ColumnGroup>();
        for (TableGroupablePropertyPersistent p : list) {
            String s = p.getName();
            if ((s != null) && (columns.get(s) == null)) {
                ColumnGroup group = new ColumnGroup(s);
                columns.put(s, group);
            }
        }

        GroupableTableColumnModel columnModel = new GroupableTableColumnModel();

        editorTable.setColumnModel(columnModel);
        editorTable.setTableHeader(new GroupableTableHeader(
                (GroupableTableColumnModel) editorTable.getColumnModel()));

        editorTable.setModel(model);

        int i = 0;

        List<ColumnGroup> groups = new ArrayList<ColumnGroup>();

        for (Attribute attribute2 : qualifier.getAttributes()) {
            for (TableGroupablePropertyPersistent p : list) {
                if ((p.getName() != null)
                        && (p.getOtherAttribute() == attribute2.getId())) {
                    ColumnGroup group = columns.get(p.getName());
                    group.add(columnModel.getColumn(i));
                    groups.add(group);
                }
            }
            i++;
        }
        for (ColumnGroup group : groups) {
            columnModel.addColumnGroup(group);
        }

        removeRowsAction.setEnabled(false);
        setFormulaAction.setEnabled(false);
        moveToOtherElementAction.setEnabled(false);
        copyToOtherElementAction.setEnabled(false);
    }

    @Override
    public JComponent getComponent() {
        pane = new JScrollPane();
        pane.setViewportView(editorTable);

        header = new TableHeader(editorTable, model);

        header.setDragEnabled(true);
        header.setTransferHandler(new RowTransferHandle());
        header.setDropMode(DropMode.INSERT);

        pane.setRowHeaderView(header);


        editorTable.setDropMode(DropMode.INSERT_ROWS);
        editorTable.setTransferHandler(new RowTransferHandle());

        return pane;
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public Object setValue(Object value) {
        return value;
    }

    @Override
    public void close() {
        super.close();
        try {
            model.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Action[] getActions() {
        Action[] actions = new Action[]{addRowAction, removeRowsAction,
                setFormulaAction, moveToOtherElementAction,
                copyToOtherElementAction};
        for (GUIPlugin plugin : framework.getPlugins()) {
            if (plugin instanceof TableEditorActionModifier) {
                try {
                    actions = ((TableEditorActionModifier) plugin).modify(
                            actions, this);
                } catch (Exception e) {
                }
            }
        }
        return actions;
    }

    private Action addRowAction = new AbstractAction() {

        /**
         *
         */
        private static final long serialVersionUID = 2698334968904507843L;

        {
            this.putValue(ACTION_COMMAND_KEY, "CreateElement");
            this.putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/add.png")));
            this.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_ADD, KeyEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            model.addElement();
        }
    };

    private Action removeRowsAction = new AbstractAction() {

        /**
         *
         */
        private static final long serialVersionUID = 2698334968904507843L;

        {
            this.putValue(ACTION_COMMAND_KEY, "DeleteElement");
            this.putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/delete.png")));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if ((editorTable.isEditing())) {
                TableCellEditor editor = editorTable.getCellEditor();
                if (editor != null)
                    editor.cancelCellEditing();
            }
            int[] is = editorTable.getSelectedRows();
            List<Element> list = new ArrayList<Element>(is.length);
            for (int i : is)
                list.add(model
                        .getElement(editorTable.convertRowIndexToModel(i)));
            model.removeElements(list);
        }
    };

    private SetFormulaAction setFormulaAction = new SetFormulaAction();

    private MoveToOtherElementAction moveToOtherElementAction = new MoveToOtherElementAction();

    private CopyToOtherElementAction copyToOtherElementAction = new CopyToOtherElementAction();

    public class SetFormulaAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -5874999065483075657L;

        public SetFormulaAction() {
            putValue(ACTION_COMMAND_KEY, "Action.SetFormula");
            putValue(SMALL_ICON, new ImageIcon(getClass().getResource(
                    "/com/ramussoft/gui/table/formula.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Engine engine = framework.getEngine();
            ElementAttribute elementAttribute = editorTable
                    .getElementAttribute();
            if (elementAttribute == null)
                return;
            CalculateInfo info = engine.getCalculateInfo(
                    elementAttribute.element.getId(),
                    elementAttribute.attribute.getId());
            if (info == null) {
                info = new CalculateInfo(elementAttribute.element.getId(),
                        elementAttribute.attribute.getId(), null);
            }

            SetFormulaDialog dialog = new SetFormulaDialog(framework, info);
            dialog.setVisible(true);
        }

    }

    public class CopyToOtherElementAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -9061908909915032146L;

        public CopyToOtherElementAction() {
            putValue(ACTION_COMMAND_KEY,
                    "Action.CopyTableElementToOtherElement");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(
                            getClass()
                                    .getResource(
                                            "/com/ramussoft/gui/table/copy-table-element-to-other-element.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<Row> list = SelectableTableView.showRowSelectDialog(framework
                    .getMainFrame(), framework, framework.getEngine()
                    .getQualifier(element.getQualifierId()), SelectType.RADIO);
            if ((list != null) && (list.size() > 0)
                    && (list.get(0).getElementId() != element.getId())) {
                Element element = list.get(0).getElement();
                if ((editorTable.isEditing())) {
                    TableCellEditor editor = editorTable.getCellEditor();
                    if (editor != null)
                        editor.cancelCellEditing();
                }
                int[] is = editorTable.getSelectedRows();
                List<Element> listToCopy = new ArrayList<Element>(is.length);

                Journaled journaled = (Journaled) framework.getEngine();
                Engine engine = framework.getEngine();

                for (int i : is)
                    listToCopy.add(model.getElement(editorTable
                            .convertRowIndexToModel(i)));
                try {
                    Qualifier qualifier = StandardAttributesPlugin
                            .getTableQualifierForAttribute(engine, attribute);
                    journaled.startUserTransaction();
                    for (Element record : listToCopy) {
                        Element copy = StandardAttributesPlugin
                                .createTableElement(engine, attribute, element);
                        for (Attribute attribute : qualifier.getAttributes()) {
                            Object object = engine.getAttribute(record,
                                    attribute);
                            if (object != null)
                                engine.setAttribute(copy, attribute, object);
                        }
                    }
                    journaled.commitUserTransaction();
                } catch (Exception ex) {
                    journaled.rollbackUserTransaction();
                }
            }
        }

    }

    public class MoveToOtherElementAction extends AbstractAction {

        /**
         *
         */
        private static final long serialVersionUID = -9061908909915032146L;

        public MoveToOtherElementAction() {
            putValue(ACTION_COMMAND_KEY,
                    "Action.MoveTableElementToOtherElement");
            putValue(
                    SMALL_ICON,
                    new ImageIcon(
                            getClass()
                                    .getResource(
                                            "/com/ramussoft/gui/table/move-table-element-to-other-element.png")));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            List<Row> list = SelectableTableView.showRowSelectDialog(framework
                    .getMainFrame(), framework, framework.getEngine()
                    .getQualifier(element.getQualifierId()), SelectType.RADIO);
            if ((list != null) && (list.size() > 0)
                    && (list.get(0).getElementId() != element.getId())) {
                Element element = list.get(0).getElement();
                if ((editorTable.isEditing())) {
                    TableCellEditor editor = editorTable.getCellEditor();
                    if (editor != null)
                        editor.cancelCellEditing();
                }
                int[] is = editorTable.getSelectedRows();
                List<Element> listToMove = new ArrayList<Element>(is.length);

                Journaled journaled = (Journaled) framework.getEngine();
                Engine engine = framework.getEngine();

                for (int i : is)
                    listToMove.add(model.getElement(editorTable
                            .convertRowIndexToModel(i)));
                try {
                    journaled.startUserTransaction();
                    StandardAttributesPlugin.updateTableElementsElement(engine,
                            listToMove, element);
                    journaled.commitUserTransaction();
                } catch (Exception ex) {
                    journaled.rollbackUserTransaction();
                }
            }
        }

    }

    @Override
    public boolean isAcceptable() {
        return false;
    }

    public GUIFramework getFramework() {
        return framework;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public Element getElement() {
        return element;
    }

    public Action getAddRowAction() {
        return addRowAction;
    }

    public Action getRemoveRowsAction() {
        return removeRowsAction;
    }

    public SetFormulaAction getSetFormulaAction() {
        return setFormulaAction;
    }

    public Element getSelectedElement() {
        int row = editorTable.getSelectedRow();
        if (row < 0)
            return null;
        return model.getElement(editorTable.convertRowIndexToModel(row));
    }

    public Element[] getSelectedElements() {
        int[] is = editorTable.getSelectedRows();
        Element[] elements = new Element[is.length];
        int j = 0;
        for (int i : is) {
            elements[j] = model.getElement(editorTable
                    .convertRowIndexToModel(i));
            j++;
        }
        return elements;
    }
}

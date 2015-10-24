package com.ramussoft.report.editor.xml;

import static com.ramussoft.report.editor.xml.Attribute.BASE_QUALIFIER;
import static com.ramussoft.report.editor.xml.Attribute.BOLD;
import static com.ramussoft.report.editor.xml.Attribute.BOLD_ITALIC;
import static com.ramussoft.report.editor.xml.Attribute.BOOLEAN;
import static com.ramussoft.report.editor.xml.Attribute.CENTER;
import static com.ramussoft.report.editor.xml.Attribute.FONT;
import static com.ramussoft.report.editor.xml.Attribute.FONT_TYPE;
import static com.ramussoft.report.editor.xml.Attribute.ITALIC;
import static com.ramussoft.report.editor.xml.Attribute.JUSTIFY;
import static com.ramussoft.report.editor.xml.Attribute.LEFT;
import static com.ramussoft.report.editor.xml.Attribute.MODEL;
import static com.ramussoft.report.editor.xml.Attribute.NO;
import static com.ramussoft.report.editor.xml.Attribute.PRINT_FOR;
import static com.ramussoft.report.editor.xml.Attribute.PRINT_FOR_ALL;
import static com.ramussoft.report.editor.xml.Attribute.PRINT_FOR_HAVE_CHILDS;
import static com.ramussoft.report.editor.xml.Attribute.PRINT_FOR_HAVE_NO_CHILDS;
import static com.ramussoft.report.editor.xml.Attribute.RIGHT;
import static com.ramussoft.report.editor.xml.Attribute.TEXT_ALIGMENT;
import static com.ramussoft.report.editor.xml.Attribute.YES;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.attribute.OtherElementTableCellEditor;
import com.ramussoft.gui.attribute.OtherElementTableCellEditor.PopupRowWrapper;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.TextField;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.qualifier.table.RowTreeTableComponent;
import com.ramussoft.gui.qualifier.table.SelectType;
import com.ramussoft.gui.qualifier.table.SelectableTableView;
import com.ramussoft.report.ReportResourceManager;

public class TableCellEditorFactory {

    private TableCellEditor defaultEditor;

    private Hashtable<Integer, TableCellEditor> editors = new Hashtable<Integer, TableCellEditor>();

    private Engine engine;

    private TableCellEditor fontEditor;

    private Collator collator = Collator.getInstance();

    private GUIFramework framework;

    public TableCellEditorFactory(GUIFramework framework) {
        this.engine = framework.getEngine();
        this.framework = framework;
        defaultEditor = new DefaultCellEditor(new TextField());
        editors.put(TEXT_ALIGMENT, createTextAligmentEditor());
        editors.put(BOOLEAN, createBooleanAligmentEditor());
        editors.put(PRINT_FOR, createPrintForEditor());
        editors.put(FONT_TYPE, createFontTypeEditor());
    }

    private TableCellEditor createFontTypeEditor() {
        JComboBox box = new JComboBox();
        box.addItem(null);
        box.addItem(BOLD);
        box.addItem(ITALIC);
        box.addItem(BOLD_ITALIC);

        return new DefaultCellEditor(box);
    }

    private TableCellEditor createPrintForEditor() {
        JComboBox box = new JComboBox();
        box.addItem(null);
        box.addItem(PRINT_FOR_ALL);
        box.addItem(PRINT_FOR_HAVE_CHILDS);
        box.addItem(PRINT_FOR_HAVE_NO_CHILDS);
        return new DefaultCellEditor(box);
    }

    private TableCellEditor createBooleanAligmentEditor() {
        JComboBox box = new JComboBox();
        box.addItem(null);
        box.addItem(YES);
        box.addItem(NO);

        return new DefaultCellEditor(box);
    }

    private TableCellEditor createTextAligmentEditor() {
        JComboBox box = new JComboBox();
        box.addItem(null);
        box.addItem(LEFT);
        box.addItem(CENTER);
        box.addItem(RIGHT);
        box.addItem(JUSTIFY);

        return new DefaultCellEditor(box);
    }

    private TableCellEditor createBaseQualifierEditor() {
        List<Qualifier> qualifiers = engine.getQualifiers();
        Collections.sort(qualifiers, new Comparator<Qualifier>() {
            @Override
            public int compare(Qualifier o1, Qualifier o2) {
                return collator.compare(o1.getName(), o2.getName());
            }
        });

        JComboBox box = new JComboBox();

        box.addItem(null);
        for (Qualifier qualifier : qualifiers)
            box.addItem(qualifier);

        return new DefaultCellEditor(box);
    }

    private TableCellEditor createModelEditor() {
        Qualifier qualifier = getModelTree(engine);

        Attribute nameAttribute = StandardAttributesPlugin
                .getAttributeNameAttribute(engine);
        RowSet rowSet = new RowSet(engine, qualifier,
                new Attribute[]{nameAttribute}, null, true);

        OtherElementTableCellEditor editor = new OtherElementTableCellEditor(
                rowSet, nameAttribute, framework, null) {
            /**
             *
             */
            private static final long serialVersionUID = -5820017882593141555L;

            private OtherElementListModel model;

            private void updateValue() {
                if (list.getSelectedIndex() == 0)
                    value = null;
                else if (list.getSelectedIndex() == 1)
                    value = "[ALL MODELS]";
                else {
                    if (model.checked.size() == 0) {
                        value = model.wrappers[list.getSelectedIndex() - 2].row
                                .getName();
                    } else {
                        StringBuffer sb = null;
                        for (PopupRowWrapper w : model.checked.keySet()) {
                            if (sb == null)
                                sb = new StringBuffer(w.row.getName());
                            else {
                                sb.append(com.ramussoft.report.editor.xml.Attribute.QUALIFIER_DELIMETER);
                                sb.append(w.row.getName());
                            }
                        }
                        value = sb.toString();
                    }
                }
            }

            @Override
            public Component getTableCellEditorComponent(JTable table,
                                                         Object value, boolean isSelected, int rowIndex,
                                                         int columnIndex) {
                try {
                    this.table = table;
                    if (value instanceof String) {
                        Row r = rowSet.findRow((String) value);
                        if (r != null) {
                            return super.getTableCellEditorComponent(table,
                                    value, isSelected, rowIndex, columnIndex);
                        } else {

                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    codeField.selectAll();
                                    codeField.requestFocus();
                                }
                            });

                            return component;
                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            codeField.selectAll();
                            codeField.requestFocus();
                        }
                    });
                    return super.getTableCellEditorComponent(table, value,
                            isSelected, rowIndex, columnIndex);
                } finally {
                    this.value = value;
                }
            }

            protected JPopupMenu getMenu(Attribute attribute) {
                if (menu == null) {
                    menu = new JPopupMenu(attribute.getName());
                    menu.setMaximumSize(new Dimension(600, 500));
                    JScrollPane pane = new JScrollPane();

                    List<Row> allRows = rowSet.getAllRows();

                    wrappers = new PopupRowWrapper[allRows.size()];

                    for (int i = 0; i < wrappers.length; i++) {
                        wrappers[i] = new PopupRowWrapper(allRows.get(i),
                                attribute);
                    }
                    model = new OtherElementListModel(wrappers);
                    list = new JList(model) {

                        /**
                         *
                         */
                        private static final long serialVersionUID = 3924839890645563320L;

                        CheckCellRenderer cellRenderer;

                        public ListCellRenderer getCellRenderer() {
                            if (cellRenderer == null)
                                cellRenderer = new CheckCellRenderer(model,
                                        super.getCellRenderer());
                            return cellRenderer;
                        }

                        ;
                    };

                    Dimension size = list.getPreferredSize();
                    if (size.width > 600)
                        list.setPreferredSize(new Dimension(600, size.height));

                    if (value == null)
                        list.setSelectedIndex(0);
                    else {
                        List<String> slist = new ArrayList<String>();
                        String val = ((String) value);
                        StringTokenizer st = new StringTokenizer(
                                val,
                                com.ramussoft.report.editor.xml.Attribute.QUALIFIER_DELIMETER);
                        slist.add(val);
                        while (st.hasMoreTokens())
                            slist.add(st.nextToken());

                        if ("[ALL MODELS]".equals(val))
                            list.setSelectedIndex(1);
                        else {
                            List<Integer> seleted = new ArrayList<Integer>();
                            for (int i = 0; i < wrappers.length; i++) {
                                if (slist.contains(wrappers[i].row.getName())) {
                                    model.select(wrappers[i]);
                                    seleted.add(i + 2);
                                }
                            }
                            int[] is = new int[seleted.size()];
                            for (int i = 0; i < is.length; i++)
                                is[i] = seleted.get(i);
                            if (is.length > 0)
                                list.setSelectedIndices(is);
                        }
                    }

                    list.getSelectionModel().setSelectionMode(
                            ListSelectionModel.SINGLE_SELECTION);

                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            list.ensureIndexIsVisible(list.getSelectedIndex());
                        }
                    });

                    list.getSelectionModel().addListSelectionListener(
                            new ListSelectionListener() {

                                private boolean rec = false;

                                @Override
                                public void valueChanged(ListSelectionEvent e) {
                                    if (rec)
                                        return;
                                    rec = true;
                                    int index = list.getSelectedIndex();

                                    PopupRowWrapper wrapper = null;

                                    if (index < 0)
                                        return;

                                    updateValue();

                                    if (index == 0)
                                        value = null;
                                    else if (index > 1) {
                                        wrapper = wrappers[index - 2];
                                    }
                                    if (updateFields) {
                                        codeField
                                                .setBackground(fieldDefaultBackground);
                                        valueField
                                                .setBackground(fieldDefaultBackground);
                                        if (wrapper == null) {
                                            codeField.setText("");
                                            valueField.setText("");
                                        } else {
                                            codeField.setText(wrapper.code);
                                            codeField.setCaretPosition(0);
                                            valueField.setText(wrapper.value);
                                            valueField.setCaretPosition(0);
                                        }
                                    }
                                    rec = false;
                                }
                            });

                    list.addMouseListener(new MouseAdapter() {

                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getX() <= 20 && e.getClickCount() < 2) {
                                int index = list.getSelectedIndex();
                                if (index > 1) {
                                    index -= 2;
                                    model.select(wrappers[index]);
                                    updateValue();
                                    list.repaint();
                                    return;
                                }
                            }
                            updateValue();
                            stopCellEditing();
                        }

                    });

                    InputMap inputMap = list.getInputMap();
                    KeyStroke[] allKeys = inputMap.allKeys();
                    for (Object actionName : actionsToReplace) {
                        for (KeyStroke stroke : allKeys) {
                            Object value = inputMap.get(stroke);
                            if (actionName.equals(value)) {
                                codeField.getInputMap().put(stroke, actionName);
                                valueField.getInputMap()
                                        .put(stroke, actionName);
                                final Action source = list.getActionMap().get(
                                        actionName);
                                if (source != null) {
                                    Action action = new AbstractAction() {

                                        /**
                                         *
                                         */
                                        private static final long serialVersionUID = 4806926801192964440L;

                                        @Override
                                        public void actionPerformed(
                                                ActionEvent e) {
                                            if (list != null) {
                                                updateFields = true;
                                                e.setSource(list);
                                                source.actionPerformed(e);
                                                updateFields = false;
                                            }
                                        }
                                    };
                                    valueField.getActionMap().put(actionName,
                                            action);
                                    codeField.getActionMap().put(actionName,
                                            action);
                                }
                            }
                        }
                    }

                    pane.setViewportView(list);
                    menu.add(pane);
                    menu.pack();
                }
                return menu;
            }

            @Override
            protected void edit() {
                if (menu != null) {
                    menu.setVisible(false);
                    menu = null;
                }
                final SelectableTableView view = new SelectableTableView(
                        framework, engine, framework.getAccessRules(),
                        rowSet.getQualifier()) {
                    @Override
                    public String getPropertiesPrefix() {
                        return "xml_edit";
                    }

                };
                JComponent rc = view.createComponent();
                final RowTreeTableComponent component = view.getComponent();

                component.getTable().setComponentPopupMenu(null);
                component.getTable().setTableHeader(null);

                List<String> slist = new ArrayList<String>();
                String val = ((String) value);
                if (val != null) {
                    StringTokenizer st = new StringTokenizer(
                            val,
                            com.ramussoft.report.editor.xml.Attribute.QUALIFIER_DELIMETER);
                    slist.add(val);
                    while (st.hasMoreTokens())
                        slist.add(st.nextToken());

                    for (Row row : component.getRowSet().getAllRows())
                        if (slist.contains(row.getName()))
                            component.getModel().setSelectedRow(row, true);
                }
                view.setSelectType(SelectType.CHECK);

                BaseDialog dialog = new BaseDialog(framework.getMainFrame()) {
                    /**
                     *
                     */
                    private static final long serialVersionUID = -4426170644933177805L;

                    @Override
                    protected void onOk() {
                        List<Row> rows = view.getSelectedRows();
                        if (rows.size() == 0)
                            value = null;
                        else {
                            StringBuffer sb = null;
                            for (Row w : rows) {
                                if (sb == null)
                                    sb = new StringBuffer(w.getName());
                                else {
                                    sb.append(com.ramussoft.report.editor.xml.Attribute.QUALIFIER_DELIMETER);
                                    sb.append(w.getName());
                                }
                            }
                            value = sb.toString();
                        }

                        stopCellEditing();
                        super.onOk();
                    }
                };
                dialog.setMainPane(rc);

                dialog.setTitle(ReportResourceManager
                        .getString("ReportAttribute.model"));

                dialog.setLocationRelativeTo(null);
                Options.loadOptions("IDEF0_models", dialog);

                dialog.setModal(true);
                dialog.setVisible(true);

                Options.saveOptions("IDEF0_models", dialog);
                try {
                    view.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        return editor;
    }

    public static Qualifier getModelTree(Engine engine) {
        return (Qualifier) engine.getPluginProperty("IDEF0", "F_MODEL_TREE");
    }

    public TableCellEditor getCellEditor(int type) {
        if (type == FONT) {
            if (fontEditor == null)
                fontEditor = createFontEditor();
            return fontEditor;
        }
        if (type == MODEL)
            return createModelEditor();
        if (type == BASE_QUALIFIER)
            return createBaseQualifierEditor();
        TableCellEditor editor = editors.get(type);
        if (editor == null)
            return defaultEditor;
        return editor;
    }

    protected class CheckCellRenderer implements ListCellRenderer {

        private final JCheckBox checkBox = new JCheckBox();

        private final JLabel label = new JLabel();

        private OtherElementListModel model;

        protected Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

        public CheckCellRenderer(OtherElementListModel model,
                                 ListCellRenderer defaultCellRenderer) {
            this.model = model;
        }

        public Component getListCellRendererComponent(final JList list,
                                                      final Object value, final int index, final boolean isSelected,
                                                      final boolean cellHasFocus) {
            Component res;
            if (!(value instanceof PopupRowWrapper)) {

                label.setBackground(isSelected ? list.getSelectionBackground()
                        : list.getBackground());
                label.setForeground(isSelected ? list.getSelectionForeground()
                        : list.getForeground());
                label.setEnabled(list.isEnabled());
                label.setFont(list.getFont());
                label.setBorder(isSelected ? UIManager
                        .getBorder("List.focusCellHighlightBorder")
                        : noFocusBorder);

                label.setText(String.valueOf(value));
                res = label;
            } else {
                PopupRowWrapper wrapper = (PopupRowWrapper) value;

                checkBox.setBackground(isSelected ? list
                        .getSelectionBackground() : list.getBackground());
                checkBox.setForeground(isSelected ? list
                        .getSelectionForeground() : list.getForeground());
                checkBox.setEnabled(list.isEnabled());
                checkBox.setFont(list.getFont());
                checkBox.setFocusPainted(false);
                checkBox.setBorderPainted(true);
                checkBox.setBorder(isSelected ? UIManager
                        .getBorder("List.focusCellHighlightBorder")
                        : noFocusBorder);

                checkBox.setText(wrapper.toString());
                checkBox.setSelected(model.isChecked(wrapper));
                res = checkBox;
            }
            return res;
        }
    }

    private final class OtherElementListModel extends AbstractListModel {
        /**
         *
         */
        private static final long serialVersionUID = -4200164542504671879L;

        private PopupRowWrapper[] wrappers;

        private HashMap<PopupRowWrapper, Boolean> checked = new HashMap<OtherElementTableCellEditor.PopupRowWrapper, Boolean>();

        public OtherElementListModel(PopupRowWrapper[] wrappers) {
            this.wrappers = wrappers;
        }

        public void select(PopupRowWrapper wrapper) {
            if (checked.remove(wrapper) == null)
                checked.put(wrapper, Boolean.TRUE);
        }

        public boolean isChecked(PopupRowWrapper wrapper) {
            return checked.get(wrapper) != null;
        }

        @Override
        public int getSize() {
            return wrappers.length + 2;
        }

        @Override
        public Object getElementAt(int index) {
            if (index == 0)
                return "<html><body><i>"
                        + GlobalResourcesManager.getString("EmptyValue")
                        + "</i></body></html>";
            if (index == 1)
                return "<html><body><b>"
                        + ReportResourceManager.getString("AllModels")
                        + "</b></body></html>";
            return wrappers[index - 2];
        }
    }

    private TableCellEditor createFontEditor() {
        JComboBox box = new JComboBox();
        box.addItem(null);
        for (String name : GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames())
            box.addItem(name);
        return new DefaultCellEditor(box);
    }

}

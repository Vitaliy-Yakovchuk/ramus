package com.ramussoft.gui.attribute;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.attribute.OtherElementPlugin.RowAttributeWrapper;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.qualifier.table.ElementsTable;

public class OtherElementTableCellEditor extends AbstractCellEditor implements
        TableCellEditor {

    /**
     *
     */
    private static final long serialVersionUID = -7286930117855276621L;

    protected RowSet rowSet;

    protected JPanel component;

    protected JTextField codeField;

    protected JTextField valueField;

    protected Object value;

    protected JPopupMenu menu;

    protected JList list;

    protected PopupRowWrapper[] wrappers;

    protected Color fieldDefaultBackground;

    protected GUIFramework framework;

    private Attribute attribute;

    private Element element;

    protected boolean updateFields = false;

    private Attribute nameAttribute;

    protected JTable table;

    protected Object[] actionsToReplace = {"selectLastRow",
            "selectPreviousRow", "scrollUp", "selectFirstRow", "scrollDown",
            "selectNextRow"};

    public OtherElementTableCellEditor(RowSet rowSet, Attribute aNameAttribute,
                                       GUIFramework framework, Attribute elementAttribute) {
        this.rowSet = rowSet;
        this.framework = framework;
        this.attribute = elementAttribute;
        this.nameAttribute = aNameAttribute;

        codeField = new JTextField() {
            /**
             *
             */
            private static final long serialVersionUID = 2277721022270178808L;

            @Override
            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
                                                int condition, boolean pressed) {

                if (condition == JComponent.WHEN_FOCUSED) {
                    if (((ks.getModifiers() == 0) || (ks.getModifiers() == KeyEvent.SHIFT_MASK))
                            && (ks.getKeyCode() == KeyEvent.VK_TAB) && pressed) {
                        valueField.requestFocus();
                        return true;
                    }
                }

                boolean result = super.processKeyBinding(ks, e, condition,
                        pressed);

                InputMap map = getInputMap();
                ActionMap am = getActionMap();

                if (map != null && am != null && isEnabled()) {
                    Object binding = map.get(ks);
                    Action action = (binding == null) ? null : am.get(binding);
                    if (action != null) {
                        return !("notify-field-accept".equals(binding));
                    }
                }
                return result;
            }
        };
        valueField = new JTextField() {

            /**
             *
             */
            private static final long serialVersionUID = -420427013497644591L;

            @Override
            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
                                                int condition, boolean pressed) {
                if (condition == JComponent.WHEN_FOCUSED) {
                    if (((ks.getModifiers() == 0) || (ks.getModifiers() == KeyEvent.SHIFT_MASK))
                            && (ks.getKeyCode() == KeyEvent.VK_TAB) && pressed) {
                        codeField.requestFocus();
                        return true;
                    }
                }

                boolean result = super.processKeyBinding(ks, e, condition,
                        pressed);

                InputMap map = getInputMap();
                ActionMap am = getActionMap();

                if (map != null && am != null && isEnabled()) {
                    Object binding = map.get(ks);
                    Action action = (binding == null) ? null : am.get(binding);
                    if (action != null) {
                        return !("notify-field-accept".equals(binding));
                    }
                }
                return result;
            }
        };

        codeField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                search();
            }

            private void search() {
                searchByCode();
            }
        });

        valueField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                search();
            }

            private void search() {
                searchByValue();
            }
        });

        fieldDefaultBackground = codeField.getBackground();

        codeField.setPreferredSize(new Dimension(40, codeField
                .getPreferredSize().height));

        codeField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                JPopupMenu popup = getMenu(nameAttribute);
                if ((!popup.isVisible()) && (component.isShowing())) {
                    showPopup();
                }
            }
        });

        component = new JPanel(new BorderLayout(0, 3));
        component.setFocusable(false);
        component.add(codeField, BorderLayout.WEST);
        component.add(valueField, BorderLayout.CENTER);
        component.add(createEditButton(), BorderLayout.EAST);
        component.setName("Table.editor");
    }

    protected void searchByValue() {
        if ((menu == null) || (!menu.isShowing()))
            return;
        if (updateFields)
            return;
        final String value = valueField.getText().toLowerCase();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (value.length() == 0) {
                    scrollToRow(0);
                    valueField.setBackground(fieldDefaultBackground);
                } else {
                    for (int i = 0; i < wrappers.length; i++) {
                        PopupRowWrapper wrapper = wrappers[i];
                        if (wrapper.value.toLowerCase().contains(value)) {
                            scrollToRow(i + 1);
                            valueField.setBackground(fieldDefaultBackground);
                            return;
                        }
                    }
                    valueField.setBackground(Color.red);
                }
            }
        });

    }

    protected void searchByCode() {
        if ((menu == null) || (!menu.isShowing()))
            return;
        if (updateFields)
            return;
        final String code = codeField.getText();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                if (code.length() == 0) {
                    scrollToRow(0);
                    codeField.setBackground(fieldDefaultBackground);
                } else {
                    for (int i = 0; i < wrappers.length; i++) {
                        PopupRowWrapper wrapper = wrappers[i];
                        if (wrapper.code.startsWith(code)) {
                            scrollToRow(i + 1);
                            codeField.setBackground(fieldDefaultBackground);
                            return;
                        }
                    }
                    codeField.setBackground(Color.red);
                }
            }
        });
    }

    protected void scrollToRow(int rowIndex) {
        list.setSelectedIndex(rowIndex);
        list.ensureIndexIsVisible(rowIndex);
    }

    protected void showPopup() {
        menu.show(component, 0, component.getHeight());
    }

    @Override
    public boolean stopCellEditing() {
        if ((menu != null) && (menu.isVisible())) {
            menu.setVisible(false);
            menu = null;
        }

        return super.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        if ((menu != null) && (menu.isVisible())) {
            menu.setVisible(false);
            menu = null;
        }

        super.cancelCellEditing();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int rowIndex, int columnIndex) {
        this.table = table;
        if (value instanceof String) {
            Row r = rowSet.findRow((String) value);
            if (r != null)
                value = r.getElementId();
            else
                value = null;
        }

        if (value instanceof Long) {
            value = new RowAttributeWrapper(rowSet.findRow((Long) value),
                    nameAttribute);
        }
        this.value = value;

        if (value != null)
            this.value = ((RowAttributeWrapper) value).getRow().getElementId();

        Row row = (value != null) ? ((RowAttributeWrapper) value).getRow()
                : null;

        if (row == null) {
            codeField.setText("");
            valueField.setText("");
        } else {
            codeField.setText(row.getCode());
            valueField.setText(value.toString());
            valueField.setCaretPosition(0);
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                codeField.selectAll();
                codeField.requestFocus();
            }
        });

        if (table instanceof ElementsTable) {
            element = ((ElementsTable) table).getElementForRow(rowIndex);
        }

        return component;
    }

    @Override
    public Object getCellEditorValue() {
        return value;
    }

    protected JPopupMenu getMenu(Attribute attribute) {
        if (menu == null) {
            menu = new JPopupMenu(attribute.getName());
            menu.setMaximumSize(new Dimension(600, 500));
            JScrollPane pane = new JScrollPane();

            List<Row> allRows = rowSet.getAllRows();

            wrappers = new PopupRowWrapper[allRows.size()];

            for (int i = 0; i < wrappers.length; i++) {
                wrappers[i] = new PopupRowWrapper(allRows.get(i), attribute);
            }

            list = new JList(new AbstractListModel() {

                /**
                 *
                 */
                private static final long serialVersionUID = -4200164542504671879L;

                @Override
                public int getSize() {
                    return wrappers.length + 1;
                }

                @Override
                public Object getElementAt(int index) {
                    if (index == 0)
                        return "<html><body><i>"
                                + GlobalResourcesManager
                                .getString("EmptyValue")
                                + "</i></body></html>";
                    return wrappers[index - 1];
                }
            });

            Dimension size = list.getPreferredSize();
            if (size.width > 600)
                list.setPreferredSize(new Dimension(600, size.height));

            if (value == null)
                list.setSelectedIndex(0);
            else {
                for (int i = 0; i < wrappers.length; i++) {
                    if (wrappers[i].row.getElementId() == ((Long) value)
                            .longValue()) {
                        list.setSelectedIndex(i + 1);
                        break;
                    }
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

                            if (index == 0)
                                value = null;
                            else {
                                wrapper = wrappers[index - 1];
                                value = wrapper.row.getElementId();
                            }
                            if (updateFields) {
                                codeField.setBackground(fieldDefaultBackground);
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
                        valueField.getInputMap().put(stroke, actionName);
                        final Action source = list.getActionMap().get(
                                actionName);
                        if (source != null) {
                            Action action = new AbstractAction() {

                                /**
                                 *
                                 */
                                private static final long serialVersionUID = 4806926801192964440L;

                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    if (list != null) {
                                        updateFields = true;
                                        e.setSource(list);
                                        source.actionPerformed(e);
                                        updateFields = false;
                                    }
                                }
                            };
                            valueField.getActionMap().put(actionName, action);
                            codeField.getActionMap().put(actionName, action);
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

    public class PopupRowWrapper {

        public Row row;

        public Attribute attribute;

        public String code;

        public String value;

        public PopupRowWrapper(Row row, Attribute attribute) {
            this.row = row;
            this.attribute = attribute;
            this.code = row.getCode();
            Object value = getValue();
            if (value == null)
                this.value = "";
            else
                this.value = value.toString();
        }

        @Override
        public String toString() {
            return code + "  " + value;
        }

        private Object getValue() {
            return row.getAttribute(attribute);
        }
    }

    ;

    private JButton createEditButton() {
        JButton edit = new JButton();
        edit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });
        edit.setIcon(new ImageIcon(getClass().getResource(
                "/com/ramussoft/gui/edit.png")));
        edit.setToolTipText(GlobalResourcesManager.getString("edit"));
        edit.setFocusable(false);
        edit.setPreferredSize(new Dimension(16, 16));
        return edit;
    }

    protected void edit() {
        if (menu != null) {
            menu.setVisible(false);
            menu = null;
        }
        AttributeEditorDialog dialog;
        Component c = null;
        if (table != null)
            c = SwingUtilities.getRoot(table);
        if (c instanceof JDialog) {
            dialog = new AttributeEditorDialog((JDialog) c, rowSet.getEngine(),
                    attribute, element, framework, framework.getAccessRules(),
                    value) {

                /**
                 *
                 */
                private static final long serialVersionUID = -6963390831133166460L;

                @Override
                protected void closed() {
                    cancelCellEditing();
                    // stopCellEditing();
                }

            };
        } else if (c instanceof JFrame) {
            dialog = new AttributeEditorDialog((JFrame) c, rowSet.getEngine(),
                    attribute, element, framework, framework.getAccessRules(),
                    value) {

                /**
                 *
                 */
                private static final long serialVersionUID = -6963390831133166460L;

                @Override
                protected void closed() {
                    cancelCellEditing();
                    // stopCellEditing();
                }

            };
        } else {
            dialog = new AttributeEditorDialog(framework.getMainFrame(),
                    rowSet.getEngine(), attribute, element, framework,
                    framework.getAccessRules(), value) {

                /**
                 *
                 */
                private static final long serialVersionUID = -6963390831133166460L;

                @Override
                protected void closed() {
                    cancelCellEditing();
                    // stopCellEditing();
                }

            };
        }
        dialog.setModal(true);
        dialog.setVisible(true);
        try {
            dialog.getAttributeEditor().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // cancelCellEditing();
    }
}

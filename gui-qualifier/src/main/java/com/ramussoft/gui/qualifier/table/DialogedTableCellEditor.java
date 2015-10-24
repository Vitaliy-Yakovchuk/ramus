package com.ramussoft.gui.qualifier.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.attribute.AttributeEditorDialog;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;

public class DialogedTableCellEditor extends AbstractCellEditor implements
        TableCellEditor {

    /**
     *
     */
    private static final long serialVersionUID = -8033763913408180991L;

    private Engine engine;

    private AccessRules rules;

    private Attribute attribute;

    protected Element element;

    private JPanel panel = new JPanel(new BorderLayout());

    private GUIFramework framework;

    private Object metaValue;

    private JTable table;

    protected JTextField field = new JTextField() {
        /**
         *
         */
        private static final long serialVersionUID = -7580754205699154129L;

        {
            setEditable(false);
            setName("Table.editor");
        }
    };

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
        AttributeEditorDialog dialog;
        Component c = null;
        if (table != null)
            c = SwingUtilities.getRoot(table);
        if (c instanceof JDialog) {
            dialog = new AttributeEditorDialog((JDialog) c, engine, attribute,
                    element, framework, rules, getMetaValue()) {

                /**
                 *
                 */
                private static final long serialVersionUID = -6963390831133166460L;

                @Override
                protected void closed() {
                    stopCellEditing();
                }

                @Override
                protected void onApply(Object value) {
                    super.onApply(value);
                    DialogedTableCellEditor.this.onApply(value);
                }

            };
        } else if (c instanceof JFrame) {
            dialog = new AttributeEditorDialog((JFrame) c, engine, attribute,
                    element, framework, rules, getMetaValue()) {

                /**
                 *
                 */
                private static final long serialVersionUID = -6963390831133166460L;

                @Override
                protected void closed() {
                    stopCellEditing();
                }

                @Override
                protected void onApply(Object value) {
                    super.onApply(value);
                    DialogedTableCellEditor.this.onApply(value);
                }

            };
        } else {
            dialog = new AttributeEditorDialog(framework.getMainFrame(),
                    engine, attribute, element, framework, rules,
                    getMetaValue()) {

                /**
                 *
                 */
                private static final long serialVersionUID = -6963390831133166460L;

                @Override
                protected void closed() {
                    stopCellEditing();
                }

                @Override
                protected void onApply(Object value) {
                    super.onApply(value);
                    DialogedTableCellEditor.this.onApply(value);
                }

            };
        }
        dialog.setVisible(true);
    }

    protected void onApply(Object value) {
    }

    public DialogedTableCellEditor(Engine engine, AccessRules accessRules,
                                   Attribute attribute, AttributePlugin attributePlugin,
                                   GUIFramework framework) {
        this.engine = engine;
        this.rules = accessRules;
        this.attribute = attribute;
        this.framework = framework;
        panel.add(field, BorderLayout.CENTER);
        panel.add(createEditButton(), BorderLayout.EAST);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                 boolean isSelected, int row, int column) {
        this.table = table;
        if (table instanceof ElementsTable) {
            element = ((ElementsTable) table).getElementForRow(row);
        }
        setValue(value);
        return panel;
    }

    protected void setValue(Object value) {
        if (value != null)
            field.setText(value.toString());
        else
            field.setText("");
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    /**
     * @param metaValue the metaValue to set
     */
    public void setMetaValue(Object metaValue) {
        this.metaValue = metaValue;
    }

    /**
     * @return the metaValue
     */
    public Object getMetaValue() {
        if (metaValue == null)
            return engine.getAttribute(element, attribute);
        return metaValue;
    }

}

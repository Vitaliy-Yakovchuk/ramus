package com.ramussoft.gui.qualifier;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.EventListenerList;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.DeleteStatusList;
import com.ramussoft.common.Element;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.core.history.Record;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.qualifier.table.DialogedTableCellEditor;
import com.ramussoft.gui.qualifier.table.ElementsTable;
import com.ramussoft.gui.qualifier.table.TableNode;
import com.ramussoft.gui.qualifier.table.TabledAttributePlugin;
import com.ramussoft.gui.qualifier.table.ValueGetter;
import com.ramussoft.gui.qualifier.table.event.CloseEvent;
import com.ramussoft.gui.qualifier.table.event.CloseListener;
import com.ramussoft.gui.qualifier.table.event.Closeable;

public class HistoryDialog extends JDialog implements Closeable {

    /**
     *
     */
    private static final long serialVersionUID = 3460998629777566063L;

    private EventListenerList list = new EventListenerList();

    private GUIFramework framework;

    private List<Record> records;

    public HistoryDialog(final GUIFramework framework,
                         final QualifierHistoryPlugin plugin, final Element element,
                         final Attribute attribute) {
        super(framework.getMainFrame());
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(
                getClass().getResource("/com/ramussoft/gui/table/history.png")));
        this.framework = framework;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle(GlobalResourcesManager.getString("HistoryDialog.title")
                + " | " + element.getName() + " | " + attribute.getName());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                closed(plugin, element, attribute);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                // closed(plugin, element, attribute);
            }
        });

        records = StandardAttributesPlugin.getHistory(framework.getEngine(),
                element, attribute);

        final AttributePlugin aPlugin = framework
                .findAttributePlugin(attribute);
        final ValueGetter getter;
        if (aPlugin instanceof TabledAttributePlugin) {
            getter = ((TabledAttributePlugin) aPlugin).getValueGetter(
                    attribute, framework.getEngine(), framework, this);
        } else
            getter = null;

        AbstractTableModel model = new AbstractTableModel() {

            /**
             *
             */
            private static final long serialVersionUID = -8209223417992678380L;

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                final Record record = records.get(rowIndex);
                if (columnIndex == 0)
                    return record.getDate();
                if (getter != null) {
                    TableNode node = new TableNode() {

                        @Override
                        public Object getValueAt(int index) {
                            return record.getValue();
                        }
                    };
                    return getter.getValue(node, 0);
                }
                return record.getValue();
            }

            @Override
            public int getRowCount() {
                return records.size();
            }

            @Override
            public int getColumnCount() {
                return 2;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0)
                    return Date.class;
                return super.getColumnClass(columnIndex);
            }

            @Override
            public String getColumnName(int column) {
                if (column == 0)
                    return GlobalResourcesManager
                            .getString("Attribute.HistoryTime");
                return GlobalResourcesManager
                        .getString("Attribute.HistoryValue");
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex > 0;
            }
        };

        final DefaultTableCellRenderer timeRenderer = new DefaultTableCellRenderer() {

            /**
             *
             */
            private static final long serialVersionUID = 6312764423346248484L;

            private DateFormat format = DateFormat.getDateTimeInstance();

            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value, boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component tableCellRendererComponent = super
                        .getTableCellRendererComponent(table, value,
                                isSelected, hasFocus, row, column);
                setText(format.format(value));
                return tableCellRendererComponent;
            }

        };

        JXTable table = new Table(model) {

            /**
             *
             */
            private static final long serialVersionUID = -995401651145855424L;

            @Override
            public TableCellRenderer getCellRenderer(int row, int column) {
                int c = convertColumnIndexToModel(column);
                if (c == 0)
                    return timeRenderer;
                return aPlugin.getTableCellRenderer(framework.getEngine(),
                        framework.getAccessRules(), attribute);
            }

            @Override
            public TableCellEditor getCellEditor(int row, int column) {
                int c = convertColumnIndexToModel(column);
                if (c == 0)
                    return super.getCellEditor(row, column);
                TableCellEditor editor = aPlugin.getTableCellEditor(
                        framework.getEngine(), framework.getAccessRules(),
                        attribute);
                if (editor == null) {
                    DialogedTableCellEditor tableCellEditor = new DialogedTableCellEditor(
                            framework.getEngine(), new AccessRules() {

                        @Override
                        public DeleteStatusList getElementsDeleteStatusList(
                                long[] elementIds) {
                            return null;
                        }

                        @Override
                        public boolean canUpdateStream(String path) {
                            return false;
                        }

                        @Override
                        public boolean canUpdateQualifier(
                                long qualifierId) {
                            return false;
                        }

                        @Override
                        public boolean canUpdateElement(long elementId,
                                                        long attributeId) {
                            return false;
                        }

                        @Override
                        public boolean canUpdateAttribute(
                                long qualifierId, long attributeId) {
                            return false;
                        }

                        @Override
                        public boolean canUpdateAttribute(
                                long attribueId) {
                            return false;
                        }

                        @Override
                        public boolean canReadQualifier(long qualifierId) {
                            return false;
                        }

                        @Override
                        public boolean canReadElement(long elementId,
                                                      long attributeId) {
                            return false;
                        }

                        @Override
                        public boolean canReadElement(long elementId) {
                            return false;
                        }

                        @Override
                        public boolean canReadAttribute(
                                long qualifierId, long attributeId) {
                            return false;
                        }

                        @Override
                        public boolean canDeleteQualifier(
                                long qualifierId) {
                            return false;
                        }

                        @Override
                        public boolean canDeleteElements(
                                long[] elementIds) {
                            return false;
                        }

                        @Override
                        public boolean canDeleteAttribute(
                                long attributeId) {
                            return false;
                        }

                        @Override
                        public boolean canCreateStript() {
                            return false;
                        }

                        @Override
                        public boolean canCreateQualifier() {
                            return false;
                        }

                        @Override
                        public boolean canCreateElement(long qualifierId) {
                            return false;
                        }

                        @Override
                        public boolean canCreateAttribute() {
                            return false;
                        }
                    }, attribute, aPlugin, framework);

                    return tableCellEditor;
                }
                return editor;
            }
        };
        JScrollPane pane = new JScrollPane(table);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(pane, BorderLayout.CENTER);
        setContentPane(panel);
        this.setSize(300, 100);
        this.setLocationRelativeTo(null);
        this.setMinimumSize(getSize());
        Options.loadOptions(this);
    }

    private class Table extends JXTable implements ElementsTable {

        /**
         *
         */
        private static final long serialVersionUID = -7244482207975682925L;

        public Table(TableModel model) {
            super(model);
        }

        @Override
        public Element getElementForRow(int row) {
            int r = convertRowIndexToModel(row);
            return records.get(r).getElement();
        }

    }

    ;

    private void closed(final QualifierHistoryPlugin plugin,
                        final Element element, final Attribute attribute) {
        plugin.windowClosed(framework, element, attribute);
        CloseEvent event = new CloseEvent(this);
        for (CloseListener listener : getCloseListeners())
            listener.closed(event);
        Options.saveOptions(this);
    }

    @Override
    public void addCloseListener(CloseListener listener) {
        list.add(CloseListener.class, listener);
    }

    @Override
    public CloseListener[] getCloseListeners() {
        return list.getListeners(CloseListener.class);
    }

    @Override
    public void removeCloseListener(CloseListener listener) {
        list.remove(CloseListener.class, listener);
    }
}

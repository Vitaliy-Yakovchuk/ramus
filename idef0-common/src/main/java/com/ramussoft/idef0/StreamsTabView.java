package com.ramussoft.idef0;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.jdesktop.swingx.JXTable;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.DeleteStatusList;
import com.ramussoft.common.Engine;
import com.ramussoft.database.StringCollator;
import com.ramussoft.database.common.Row;
import com.ramussoft.database.common.RowSet;
import com.ramussoft.gui.common.AbstractView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.GlobalResourcesManager;
import com.ramussoft.gui.common.TabView;
import com.ramussoft.gui.qualifier.table.StatusMessageFormat;

public class StreamsTabView extends AbstractView implements TabView {

    private RowSet streams;

    private JXTable table;

    private List<Row> data = new ArrayList<Row>();

    private AbstractTableModel model;

    private DeleteElementAction deleteElementAction = new DeleteElementAction();

    public StreamsTabView(GUIFramework framework) {
        super(framework);
        Engine engine = framework.getEngine();
        streams = new RowSet(engine,
                IDEF0Plugin.getBaseStreamQualifier(engine),
                new Attribute[]{IDEF0Plugin.getAttribute(engine,
                        IDEF0Plugin.F_STREAM_NAME)}) {
            @Override
            protected void added(Row parent, Row row, int index) {
                if (filterAdd(row)) {
                    data.add(row);
                    Collections.sort(data, new Comparator<Row>() {
                        @Override
                        public int compare(Row o1, Row o2) {
                            return StringCollator.compare(o1.getName(),
                                    o2.getName());
                        }
                    });
                    model.fireTableDataChanged();
                }
            }

            @Override
            protected void removedFromChildren(Row parent, Row row, int index1) {
                int index = data.indexOf(row);
                if (index >= 0) {
                    data.remove(index);
                    model.fireTableRowsDeleted(index, index);
                }
            }

            @Override
            protected void attributeChanged(Row row, Attribute attribute,
                                            Object newValue, Object oldValue, boolean journaled) {
                int index = data.indexOf(row);
                if (index < 0 && row.getName().trim().length() > 0)
                    added(null, row, 0);
                else
                    model.fireTableRowsUpdated(index, index);
            }
        };
    }

    @Override
    public Action[] getActions() {
        return new Action[]{deleteElementAction};
    }

    @Override
    public JComponent createComponent() {
        for (Row row : streams.getAllRows())
            if (filterAdd(row))
                data.add(row);
        Collections.sort(data, new Comparator<Row>() {
            @Override
            public int compare(Row o1, Row o2) {
                return StringCollator.compare(o1.getName(), o2.getName());
            }
        });
        table = new JXTable(model = new AbstractTableModel() {

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return data.get(rowIndex).getName();
            }

            @Override
            public int getRowCount() {
                return data.size();
            }

            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return true;
            }

            @Override
            public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
                data.get(rowIndex).setName(String.valueOf(aValue));
            }
        });

        table.setTableHeader(null);

        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        deleteElementAction.setEnabled(table.getSelectedRow() >= 0);
                    }
                });

        table.setComponentPopupMenu(createPopupMenu());

        JScrollPane jScrollPane = new JScrollPane(table);

        return jScrollPane;
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        for (Action action : getActions())
            if (action == null)
                menu.addSeparator();
            else {
                menu.add(action);
            }
        return menu;
    }

    @Override
    public String getTitle() {
        return ResourceLoader.getString("streams");
    }

    @Override
    public void close() {
        super.close();
        streams.close();
    }

    private boolean filterAdd(Row row) {
        String name = row.getName();
        if (name == null || name.trim().length() == 0)
            return false;
        return true;
    }

    protected class DeleteElementAction extends AbstractAction {

        public DeleteElementAction() {
            this.putValue(ACTION_COMMAND_KEY, "DeleteElement");
            this.putValue(NAME,
                    GlobalResourcesManager.getString("DeleteElement"));
            this.putValue(
                    SMALL_ICON,
                    new ImageIcon(getClass().getResource(
                            "/com/ramussoft/gui/table/delete.png")));
            this.putValue(ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));

            setEnabled(false);
        }

        /**
         *
         */
        private static final long serialVersionUID = -5284012805486357491L;

        @Override
        public void actionPerformed(ActionEvent e) {
            int i = table.getSelectedRow();
            if (i < 0)
                return;
            List<Row> rows = new ArrayList<Row>();

            for (int j : table.getSelectedRows()) {
                rows.add(data.get(table.convertRowIndexToModel(j)));
            }

            long[] ls = new long[rows.size()];
            for (int j = 0; j < ls.length; j++)
                ls[j] = rows.get(j).getElementId();

            DeleteStatusList list = framework.getAccessRules()
                    .getElementsDeleteStatusList(ls);
            if (list.size() > 0) {
                if (!StatusMessageFormat.deleteElements(list, null, framework))
                    return;
            } else {
                if (JOptionPane.showConfirmDialog(table, GlobalResourcesManager
                                .getString("DeleteActiveElementsDialog.Warning"),
                        GlobalResourcesManager
                                .getString("ConfirmMessage.Title"),
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
                    return;
            }

            streams.startUserTransaction();

            for (Row row : rows) {
                streams.deleteRow(row);

            }

            streams.commitUserTransaction();
        }
    }

    @Override
    public com.ramussoft.gui.common.event.ActionEvent getOpenAction() {
        return new com.ramussoft.gui.common.event.ActionEvent(
                IDEF0ViewPlugin.OPEN_STREAMS, null);
    }
}

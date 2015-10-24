package com.ramussoft.pb.idef.frames;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.utils.Options;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.RowFactory;
import com.ramussoft.pb.data.RowSetClass;
import com.ramussoft.pb.frames.components.RowFindPanel;

public class StreamsEditDialog extends JDialog {

    private static final String REMOVE = "remove";

    private static final String SELECT = "select_all_not_connecting";

    private static final long serialVersionUID = 1L;
    private JPanel jContentPane = null;
    private JToolBar jToolBar = null;
    private JButton remove = null;
    private JButton select = null;
    private JScrollPane jScrollPane = null;
    private JTable jTable = null;
    private final Action removeAction = createAction(REMOVE);
    private RowFindPanel findPanel = null;

    private DataPlugin dataPlugin;

    private Vector<Row> data = new Vector<Row>(0);

    private final AbstractTableModel model = new AbstractTableModel() {

        public int getColumnCount() {
            return 1;
        }

        public int getRowCount() {
            return data.size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return data.get(rowIndex).getName();
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (value != null)
                data.get(rowIndex).setName(value.toString());
        }

        @Override
        public String getColumnName(int column) {
            return ResourceLoader.getString("stream");
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }
    };

    /**
     * @param owner
     */
    public StreamsEditDialog(final Frame owner, DataPlugin dataPlugin) {
        super(owner, true);
        this.dataPlugin = dataPlugin;
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        this.setSize(300, 200);
        setContentPane(getJContentPane());
        setTitle(ResourceLoader.getString("Menu.Streams"));
        setLocationRelativeTo(null);
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(new BorderLayout());
            jContentPane.add(getJToolBar(), BorderLayout.NORTH);
            jContentPane.add(getJScrollPane(), BorderLayout.CENTER);
            jContentPane.add(getFindPanel(), BorderLayout.SOUTH);
        }
        return jContentPane;
    }

    /**
     * This method initializes jToolBar
     *
     * @return javax.swing.JToolBar
     */
    private JToolBar getJToolBar() {
        if (jToolBar == null) {
            jToolBar = new JToolBar();
            jToolBar.add(getRemove());
            jToolBar.add(getSelect());
        }
        return jToolBar;
    }

    /**
     * This method initializes remove
     *
     * @return javax.swing.JButton
     */
    private JButton getRemove() {
        if (remove == null) {
            remove = new JButton(removeAction);
            removeAction.setEnabled(false);
        }
        return remove;
    }

    private Action createAction(final String action) {
        return createAction(action, new ImageIcon(getClass().getResource(
                "/images/" + action + ".png")));
    }

    private Action createAction(final String action, final ImageIcon icon) {
        final Action res = new AbstractAction(null, icon) {

            public void actionPerformed(ActionEvent e) {
                performed(e.getActionCommand());
            }

        };

        res.putValue(Action.ACTION_COMMAND_KEY, action);
        res
                .putValue(Action.SHORT_DESCRIPTION, ResourceLoader
                        .getString(action));
        return res;
    }

    private void performed(final String cmd) {
        if (cmd.equals(REMOVE))
            remove();
        else if (cmd.equals(SELECT))
            select();
    }

    private void remove() {
        final int[] is = jTable.getSelectedRows();
        final Row[] rows = new Row[is.length];
        for (int i = 0; i < is.length; i++) {
            rows[i] = data.get(jTable.convertRowIndexToModel(is[i]));
        }

        for (int i = is.length - 1; i >= 0; i--) {
            final int j = jTable.convertRowIndexToModel(is[i]);
            if (dataPlugin.removeRow(data.get(j))) {
                data.remove(j);
            }
        }
        model.fireTableDataChanged();
    }

    private void select() {
        final ListSelectionModel ds = jTable.getSelectionModel();
        jTable.setSelectionModel(new DefaultListSelectionModel());
        final ListSelectionListener[] lrs = ((DefaultListSelectionModel) ds)
                .getListSelectionListeners();
        for (final ListSelectionListener lr : lrs)
            ds.removeListSelectionListener(lr);
        ds.clearSelection();
        for (int i = 0; i < data.size(); i++) {
            ds.addSelectionInterval(i, i);
        }
        for (final ListSelectionListener lr : lrs)
            ds.addListSelectionListener(lr);
        jTable.setSelectionModel(ds);
        setEnableRemove();
    }

    /**
     * This method initializes select
     *
     * @return javax.swing.JButton
     */
    private JButton getSelect() {
        if (select == null) {
            select = new JButton(createAction(SELECT, new ImageIcon(getClass()
                    .getResource("/images/sel_ather.png"))));
        }
        return select;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPane() {
        if (jScrollPane == null) {
            jScrollPane = new JScrollPane();
            jScrollPane.setViewportView(getJTable());
        }
        return jScrollPane;
    }

    /**
     * This method initializes jTable
     *
     * @return javax.swing.JTable
     */
    private JTable getJTable() {
        if (jTable == null) {
            jTable = new JTable();
            jTable.getSelectionModel().addListSelectionListener(
                    new ListSelectionListener() {

                        public void valueChanged(final ListSelectionEvent e) {
                            setEnableRemove();
                        }

                    });
            jTable.setModel(model);
        }
        return jTable;
    }

    protected void setEnableRemove() {
        if (jTable.getSelectedRowCount() > 0) {
            final int[] is = jTable.getSelectedRows();
            for (final int i : is) {
                if (!data.get(i).isRemoveable()) {
                    removeAction.setEnabled(false);
                    return;
                }
            }
            removeAction.setEnabled(true);
        } else
            removeAction.setEnabled(false);
    }

    public void showModal() {
        refresh();
        Options.loadOptions("s_e_d", this);
        setVisible(true);
        Options.saveOptions("s_e_d", this);
    }

    private void refresh() {
        data = dataPlugin.getChilds(dataPlugin.getBaseStream(), true);

        for (int i = data.size() - 1; i >= 0; i--) {
            if (((Stream) data.get(i)).isEmptyName()) {
                data.remove(i);
            }
        }

        final Stream[] rs = data.toArray(new Stream[data.size()]);
        RowFactory.sortByTitle(rs);

        data.clear();

        for (final Stream s : rs) {
            data.add(s);
        }

        model.fireTableDataChanged();
    }

    private RowFindPanel getFindPanel() {
        if (findPanel == null) {
            findPanel = new RowFindPanel() {
                @Override
                public boolean find(final String text, final boolean wordsOrder) {
                    return find(-1, text, wordsOrder);
                }

                @Override
                public boolean findNext(final String text,
                                        final boolean wordsOrder) {
                    return find(jTable.getSelectedRow(), text, wordsOrder);
                }

                private boolean find(final int selectedIndex,
                                     final String text, final boolean wordsOrder) {
                    for (int i = selectedIndex + 1; i < data.size(); i++) {
                        final Stream stream = (Stream) data.get(i);
                        if (select(text, wordsOrder, i, stream))
                            return true;
                    }
                    for (int i = 0; i < selectedIndex; i++) {
                        final Stream stream = (Stream) data.get(i);
                        if (select(text, wordsOrder, i, stream))
                            return true;
                    }
                    return false;
                }

                private boolean select(final String text,
                                       final boolean wordsOrder, final int i,
                                       final Stream stream) {
                    if (RowSetClass.isStartSame(stream, text, wordsOrder)) {
                        jTable.changeSelection(i, 0, false, false);
                        return true;
                    }
                    return false;
                }
            };
            ResourceLoader.setJComponentsText(findPanel);
        }
        return findPanel;
    }
}

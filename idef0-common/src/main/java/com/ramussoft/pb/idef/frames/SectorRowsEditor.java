package com.ramussoft.pb.idef.frames;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Element;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.table.event.SelectionEvent;
import com.ramussoft.gui.qualifier.table.event.SelectionListener;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Sector;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.RowFactory;
import com.ramussoft.pb.data.negine.NSector;
import com.ramussoft.pb.dfds.visual.DFDSRole;
import com.ramussoft.pb.frames.SelectRowDialog;
import com.ramussoft.pb.frames.SelectRowPanel;

/**
 * Панель, яка призначена для редагування списку класифкаторів, які під’єднаня
 * до сектора.
 *
 * @author ZDD
 */
public class SectorRowsEditor extends JPanel {

    private SectorNameEditor sectorNameEditor;

    private JScrollPane jScrollPane = null;

    private Stream stream = null;

    private AlternativeArrowDialog arrowRowsDialog = null;

    private JDialog dialog = null;

    private JTable jTable = null;

    private final RowsModel rowsModel = new RowsModel();

    private Row[] rows = new Row[]{};

    private String[] statuses = new String[]{};

    private JPanel jPanel4 = null;

    private JPanel jPanel8 = null;

    private JButton jButtonAdd = null;

    private JButton jButtonRemove = null;

    private SelectRowDialog rowSelectDialog = null;

    private JButton jButtonClear = null;

    private DataPlugin dataPlugin;

    private GUIFramework framework;

    private AccessRules accessRules;

    private boolean changed;

    private Sector sector;

    private SelectRowPanel selectRowPanel;

    private class RowsModel extends AbstractTableModel {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        private int columnCount = 3;

        public int getRowCount() {
            if (rows == null)
                return 0;
            return rows.length;
        }

        public int getColumnCount() {
            return columnCount;
        }

        public Object getValueAt(final int rowIndex, final int columnIndex) {
            if (columnIndex == 0)
                return rows[rowIndex].getKod() + " " + rows[rowIndex].getName();
            if (columnIndex == 1) {
                return statuses[rowIndex];
            }
            if (columnIndex == 2) {
                String name = rows[rowIndex].getQualifier().getName();
                if ("QualifiersQualifier".equals(name))
                    return ResourceLoader.getString("QualifiersQualifier");
                return name;
            }
            return ResourceLoader.getString("no_data");
        }

        public void refresh() {
            final int cc = columnCount;

            columnCount = 3;

            fireTableStructureChanged();

            if (cc != columnCount)
                fireTableStructureChanged();
            else
                fireTableDataChanged();
        }

        @Override
        public String getColumnName(final int column) {
            if (column == 0)
                return ResourceLoader.getString("added_rows");
            if (column == 1)
                return ResourceLoader.getString("status");
            return ResourceLoader.getString("clasificator");
        }

        @Override
        public boolean isCellEditable(final int rowIndex, final int columnIndex) {
            if (columnIndex == 0 || columnIndex == 1)
                return true;

            return false;
        }

        @Override
        public void setValueAt(final Object aValue, final int rowIndex,
                               final int columnIndex) {
            if (columnIndex == 0) {
                ((Journaled) dataPlugin.getEngine()).startUserTransaction();
                rows[rowIndex].setName(aValue.toString());
                ((Journaled) dataPlugin.getEngine()).commitUserTransaction();
            } else if (columnIndex == 1) {
                statuses[rowIndex] = aValue.toString();
            }
        }
    }

    public Row[] getRows() {
        for (int i = 0; i < rows.length; i++) {
            String st = statuses[i];
            if (st == null)
                st = "";
            if (rows[i] != null && sector != null)
                rows[i].setAttachedStatus(((NSector) sector).getElementId()
                        + "3|" + st);
        }
        return rows;
    }

    private AlternativeArrowDialog getArrowRowsDialog() {
        if (arrowRowsDialog == null) {
            if (dialog == null)
                arrowRowsDialog = new AlternativeArrowDialog(dataPlugin,
                        framework);
            else
                arrowRowsDialog = new AlternativeArrowDialog(dialog,
                        dataPlugin, framework);
        }
        return arrowRowsDialog;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
        setStream(sector.getStream());
        if (sector.getStream() == null) {
            rows = new Row[]{};
            statuses = new String[]{};
        } else {
            rows = sector.getStream().getAdded();
            fillStatuses();
        }

        List l = Arrays.asList(rows);
        selectRowPanel.selectRows(l);

        rowsModel.refresh();
        repaint();
        changed = false;
    }

    public void setDFDSRole(DFDSRole role) {
        setStream(role.getStream());
        if (role.getStream() == null) {
            rows = new Row[]{};
            statuses = new String[]{};
        } else {
            rows = role.getStream().getAdded();
            fillStatuses();
        }

        List l = Arrays.asList(rows);
        selectRowPanel.selectRows(l);

        rowsModel.refresh();
        repaint();
        changed = false;
    }

    private void fillStatuses() {
        statuses = new String[rows.length];
        for (int i = 0; i < statuses.length; i++) {
            String status = rows[i].getAttachedStatus();
            if (status != null) {
                int index = status.indexOf('|');
                if (index >= 0)
                    status = status.substring(index + 1);
            }
            statuses[i] = status;
        }
    }

    private void fillStatuses(int oldLength) {
        statuses = Arrays.copyOf(statuses, rows.length);
    }

    /**
     * This is the default constructor
     */
    public SectorRowsEditor(DataPlugin dataPlugin, GUIFramework framework,
                            AccessRules accessRules) {
        super();
        this.dataPlugin = dataPlugin;
        this.framework = framework;
        this.accessRules = accessRules;
        initialize();
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        setLayout(new BorderLayout());
        this.setSize(new Dimension(441, 231));
        JSplitPane splitPane = new JSplitPane();
        this.add(splitPane, java.awt.BorderLayout.CENTER);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(0.75d);
        splitPane.setLeftComponent(getSelectRowPane());
        splitPane.setRightComponent(getJPanel4());
    }

    private SelectRowPanel getSelectRowPane() {
        if (selectRowPanel == null) {
            selectRowPanel = new SelectRowPanel(framework, dataPlugin,
                    accessRules);
            selectRowPanel.addSelectionListener(new SelectionListener() {

                @Override
                public void changeSelection(SelectionEvent event) {
                    if (event.isSelected())
                        addRows(event.getRows());
                    else {
                        removeRows(event.getRows());
                    }
                }
            });
        }
        return selectRowPanel;
    }

    protected void addRows(com.ramussoft.database.common.Row[] rows) {
        List<Row> rows2 = new ArrayList<Row>();
        for (com.ramussoft.database.common.Row r : rows)
            rows2.add(dataPlugin.findRowByGlobalId(r.getElementId()));
        addRows(rows2);
    }

    protected void removeRows(com.ramussoft.database.common.Row[] rows) {
        List<Row> rows2 = new ArrayList<Row>();
        for (com.ramussoft.database.common.Row r : rows)
            rows2.add(dataPlugin.findRowByGlobalId(r.getElementId()));
        removeRows(rows2.toArray(new Row[rows2.size()]));
    }

    public void setDialog(final JDialog dialog) {
        this.dialog = dialog;
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

    public void setSectorNameEditor(SectorNameEditor sectorNameEditor) {
        this.sectorNameEditor = sectorNameEditor;
    }

    protected boolean selectRows() {
        final Stream tmp = getArrowRowsDialog().showModal();
        if (tmp == getStream() || tmp == null)
            return false;
        Stream ls = lowstream;
        setStream(tmp);
        lowstream = ls;
        if (getStream() != null) {
            rows = getStream().getAdded();
            fillStatuses();
        }
        rowsModel.refresh();
        return true;
    }

    public Stream findStreamByName(final String name) {
        if ("".equals(name))
            return null;
        final Vector<Row> streams = dataPlugin.getRecChilds(
                dataPlugin.getBaseStream(), true);
        final int l = streams.size();
        for (int i = 0; i < l; i++) {
            final Stream s = (Stream) streams.get(i);
            if (name.equals(s.getName()) && !s.isEmptyName())
                return s;
        }
        return null;
    }

    public Stream getStream() {
        if (!sectorNameEditor.jTextField.getText().equals("")) {
            final Stream r = sectorNameEditor
                    .findStreamByName(sectorNameEditor.jTextField.getText());
            if (r != null && !r.equals(stream)) {
                rows = r.getAdded();
                rowsModel.refresh();
                return r;
            }
        }
        if (stream == null) {
            if (!sectorNameEditor.jTextField.getText().equals("")) {
                final Stream r = sectorNameEditor
                        .findStreamByName(sectorNameEditor.jTextField.getText());
                if (r != null) {
                    stream = r;
                    return stream;
                }
                stream = (Stream) dataPlugin.createRow(
                        dataPlugin.getBaseStream(), true);
                stream.setRows(rows);
                // if(!stream.getTitle().equals(jTextField.getText()))
                stream.setName(sectorNameEditor.jTextField.getText());
            }
        }
        return stream;
    }

    private static final Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    private static final class NoData extends JComponent implements
            TableCellRenderer {

        public Component getTableCellRendererComponent(final JTable table,
                                                       final Object value, final boolean isSelected,
                                                       final boolean hasFocus, final int row, final int column) {
            if (isSelected) {
                super.setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            } else {
                super.setForeground(table.getForeground());
                super.setBackground(table.getBackground());
            }

            setFont(table.getFont());

            if (hasFocus) {
                Border border = null;
                if (isSelected) {
                    border = UIManager
                            .getBorder("Table.focusSelectedCellHighlightBorder");
                }
                if (border == null) {
                    border = UIManager
                            .getBorder("Table.focusCellHighlightBorder");
                }
                setBorder(border);
            } else {
                setBorder(noFocusBorder);
            }
            return this;
        }

        @Override
        public void paint(final Graphics gr) {
            super.paint(gr);
            int l = getWidth();
            if (l > getHeight())
                l = getHeight();
            l /= 2;
            final int x = (getWidth() - l) / 2;
            final int y = (getHeight() - l) / 2;
            final Graphics2D g = (Graphics2D) gr;

            g.setColor(getBackground());
            final Insets is = getBorder().getBorderInsets(this);
            g.fillRect(is.left, is.right, getWidth() - is.right - is.left,
                    getHeight() - is.bottom - is.left);
            final Color f = getForeground();
            g.setColor(new Color(255 - (255 - f.getRed()) / 2, 255 - (255 - f
                    .getGreen()) / 2, 255 - (255 - f.getBlue()) / 2));
            g.setStroke(new BasicStroke(3.0f/*
                                             * , BasicStroke.CAP_ROUND,
											 * BasicStroke.JOIN_ROUND
											 */));
            g.drawLine(x, y, x + l, y + l);
            g.drawLine(x, y + l, x + l, y);

        }

    }

    ;

    private static final NoData noData = new NoData();

    private Stream lowstream;

    /**
     * This method initializes jTable
     *
     * @return javax.swing.JTable
     */
    private JTable getJTable() {
        if (jTable == null) {
            jTable = new JTable() {
                @Override
                public Component prepareEditor(final TableCellEditor editor,
                                               final int row, final int column) {
                    if (convertColumnIndexToModel(column) < 1) {
                        final boolean isSelected = isCellSelected(row, column);
                        final Component comp = editor
                                .getTableCellEditorComponent(this,
                                        rows[row].getName(), isSelected, row,
                                        column);
                        return comp;
                    }
                    return super.prepareEditor(editor, row, column);
                }

                @Override
                public TableCellRenderer getCellRenderer(final int row,
                                                         final int column) {
                    if (convertColumnIndexToModel(column) > 2
                            && !isCellEditable(row, column))
                        return noData;
                    return super.getCellRenderer(row, column);
                }

            };
            jTable.setModel(rowsModel);
        }
        return jTable;
    }

    /**
     * This method initializes jPanel4
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel4() {
        if (jPanel4 == null) {
            jPanel4 = new JPanel();
            jPanel4.setPreferredSize(new Dimension(20, 100));
            jPanel4.setLayout(new BorderLayout());
            jPanel4.add(getJScrollPane(), java.awt.BorderLayout.CENTER);
            jPanel4.add(getJPanel8(), java.awt.BorderLayout.EAST);
        }
        return jPanel4;
    }

    /**
     * This method initializes jPanel8
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanel8() {
        if (jPanel8 == null) {
            final GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(3);
            gridLayout.setVgap(5);
            gridLayout.setColumns(1);
            JPanel jPanel = new JPanel();
            jPanel.setLayout(gridLayout);
            jPanel.add(getJButtonAdd(), null);
            jPanel.add(getJButtonRemove(), null);
            jPanel.add(getJButtonClear(), null);
            jPanel8 = new JPanel(new BorderLayout(5, 5));
            jPanel8.add(jPanel, BorderLayout.NORTH);
        }
        return jPanel8;
    }

    /**
     * This method initializes jButton1
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonAdd() {
        if (jButtonAdd == null) {
            jButtonAdd = new JButton();
            jButtonAdd.setText("add");
            jButtonAdd.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    addRows();
                    setChanged(true);
                }
            });
        }
        return jButtonAdd;
    }

    /**
     * This method initializes jButton2
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonRemove() {
        if (jButtonRemove == null) {
            jButtonRemove = new JButton();
            jButtonRemove.setText("remove");
            jButtonRemove
                    .addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(
                                final java.awt.event.ActionEvent e) {
                            removeRows();
                            setChanged(true);
                        }
                    });
        }
        return jButtonRemove;
    }

    private SelectRowDialog getRowSelectDialog() {
        if (rowSelectDialog == null) {
            rowSelectDialog = new SelectRowDialog(dialog) {

            };
            rowSelectDialog.init(framework, dataPlugin, accessRules);

            List<Element> hide = new ArrayList<Element>(1);
            hide.add(((com.ramussoft.database.common.Row) dataPlugin
                    .getBaseFunction()).getElement());
            rowSelectDialog.setQuaifierHideElements(hide);
        }
        return rowSelectDialog;
    }

    private void addRows() {
        final SelectRowDialog r = getRowSelectDialog();
        List<Row> list = r.showModal();
        if (r.isOk()) {
            addRows(list);
            List<Row> rows = selectRowPanel.getSelected();
            rows.addAll(list);
            selectRowPanel.selectRows((List) rows);
        }
    }

    private void addRows(List<Row> list) {
        int oldLength = rows.length;
        rows = RowFactory.addRows(rows, list.toArray(new Row[list.size()]));
        fillStatuses(oldLength);
        rowsModel.refresh();
        changed = true;
    }

    private Row[] getSelectedRows() {
        final int n = jTable.getSelectedRowCount();
        final int[] is = jTable.getSelectedRows();
        final Row[] rows = new Row[n];
        for (int i = 0; i < n; i++) {
            rows[i] = this.rows[is[i]];
        }
        return rows;
    }

    private void removeRows() {
        Row[] selectedRows = getSelectedRows();
        List<Row> list = Arrays.asList(selectedRows);
        List<Row> sels = selectRowPanel.getSelected();
        sels.removeAll(list);

        selectRowPanel.selectRows((List) sels);

        removeRows(selectedRows);
    }

    private void removeRows(Row[] selectedRows) {
        changed = true;
        rows = RowFactory.removeRows(rows, selectedRows);
        rowsModel.refresh();
    }

    /**
     * This method initializes jButton3
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonClear() {
        if (jButtonClear == null) {
            jButtonClear = new JButton();
            jButtonClear.setText("clear");
            jButtonClear.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(final java.awt.event.ActionEvent e) {
                    clear();
                    setChanged(true);
                }
            });
        }
        return jButtonClear;
    }

    protected void clear() {
        rows = new Row[0];
        selectRowPanel.selectRows((List) Collections.emptyList());
        rowsModel.refresh();
        changed = true;
    }

    public void setStream(final Stream stream) {
        this.stream = stream;
        this.lowstream = stream;
    }

    /**
     * @param changed the changed to set
     */
    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    /**
     * @return the changed
     */
    public boolean isChanged() {
        return changed;
    }

    public void dispose() {
        selectRowPanel.dispose();
    }

} // @jve:decl-index=0:visual-constraint="10,10"

package com.ramussoft.gui.attribute.table;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import com.ramussoft.gui.qualifier.table.ImportExport;

public class TableHeader extends JList implements ImportExport {

    /**
     *
     */
    private static final long serialVersionUID = 5532512655911032700L;

    private TableEditorTable table;

    //private PlanModel model;

    private ListCellRenderer renderer = new SimpleCellRenderer();

    private TableModelListener listener = new TableModelListener() {

        public void tableChanged(TableModelEvent e) {
            setModel(new TableHeaderModel(table));
        }

    };

    private class SimpleCellRenderer implements ListCellRenderer {

        private final JLabel label = new JLabel();

        Border a = BorderFactory.createEtchedBorder();

        Border b = BorderFactory.createRaisedBevelBorder();

        public Component getListCellRendererComponent(final JList list,
                                                      final Object value, final int index, boolean isSelected,
                                                      final boolean cellHasFocus) {
            label.setBorder(isSelected ? b : a);
            label.setText(value == null ? "" : (Integer.toString((((Integer) value) + 1))));
            return label;
        }
    }

    ;

    public TableHeader(TableEditorTable table, TableEditorModel planModel) {
        this.table = table;
        //this.model = planModel;
        setFixedCellHeight(table.getRowHeight());
        setFixedCellWidth(20);
        setCellRenderer(renderer);
        table.getModel().addTableModelListener(listener);
        setModel(new TableHeaderModel(table));
        setSelectionModel(table.getSelectionModel());
    }

    @Override
    public boolean canImport(DataFlavor[] transferFlavors) {
        return table.canImport(transferFlavors);
    }

    @Override
    public Transferable createTransferable() {
        return table.createTransferable();
    }

    @Override
    public void exportDone(Transferable data, int action) {
        table.exportDone(data, action);
    }

    @Override
    public boolean importData(Transferable t) {
        return table.importData(t, getDropLocation().getIndex());
    }

}

package com.ramussoft.gui.attribute.table;

import static com.ramussoft.gui.qualifier.table.RowTreeTable.localListFlavor;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.CellEditor;
import javax.swing.JInternalFrame;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.JXTable;

import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.attribute.AttributeEditorView.ElementAttribute;
import com.ramussoft.gui.common.AttributePlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.qualifier.Commands;
import com.ramussoft.gui.qualifier.table.DialogedTableCellEditor;
import com.ramussoft.gui.qualifier.table.ElementsTable;
import com.ramussoft.gui.qualifier.table.ImportExport;

public class TableEditorTable extends JXTable implements ElementsTable,
        ImportExport {

    /**
     *
     */
    private static final long serialVersionUID = 5228495924875559318L;

    private AttributePlugin[] plugins;

    private TableCellEditor[] cellEditors;

    private TableCellRenderer[] cellRenderers;

    private GUIFramework framework;

    private List<Attribute> attributes;

    private boolean exporting;

    public TableEditorTable(List<Attribute> attributes, GUIFramework framework) {
        this.framework = framework;
        this.attributes = attributes;
        plugins = new AttributePlugin[attributes.size()];
        cellEditors = new TableCellEditor[plugins.length];
        cellRenderers = new TableCellRenderer[plugins.length];
        setHorizontalScrollEnabled(true);
        setShowVerticalLines(true);

        getInputMap(JInternalFrame.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "RamusENTER_Action");
        getInputMap(JInternalFrame.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "RamusENTER_Action");
        AbstractAction ramusEnterAction = new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = 4745861738845278043L;

            public void actionPerformed(ActionEvent ae) {
                CellEditor editor = getCellEditor();
                if (editor != null)
                    editor.stopCellEditing();
            }
        };

        getActionMap().put("RamusENTER_Action", ramusEnterAction);
    }

    @Override
    public void setModel(TableModel dataModel) {
        super.setModel(dataModel);
        if (!(dataModel instanceof TableEditorModel))
            return;
        TableEditorModel model = (TableEditorModel) dataModel;
        Engine engine = framework.getEngine();
        AccessRules rules = framework.getAccessRules();
        for (int i = 0; i < plugins.length; i++) {
            AttributePlugin plugin = framework.findAttributePlugin(attributes
                    .get(i));
            plugins[i] = plugin;
            cellEditors[i] = plugin.getTableCellEditor(engine, rules,
                    attributes.get(i));

            if (cellEditors[i] == null) {
                cellEditors[i] = new DialogedTableCellEditor(engine, rules,
                        attributes.get(i), plugins[i], framework);
                model.setSaveValue(i, false);
            }

            cellRenderers[i] = plugin.getTableCellRenderer(engine, rules,
                    attributes.get(i));
        }
    }

    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggle,
                                boolean extend) {
        super.changeSelection(rowIndex, columnIndex, toggle, extend);
        ElementAttribute value = getElementAttribute(rowIndex, columnIndex);
        framework.propertyChanged(Commands.ACTIVATE_TABLE_ATTRIBUTE, value);
    }

    public ElementAttribute getElementAttribute(int rowIndex, int columnIndex) {
        if ((rowIndex < 0) || (columnIndex < 0))
            return null;
        int modelRow = convertRowIndexToModel(rowIndex);
        int modelColumn = convertColumnIndexToModel(columnIndex);
        TableEditorModel model = (TableEditorModel) getModel();
        ElementAttribute elementAttribute = new ElementAttribute(model
                .getElement(modelRow), model.getAttribute(modelColumn), model
                .getValueAt(modelRow, modelColumn));
        return elementAttribute;
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        if (cellEditors[column] == null)
            return super.getCellEditor(row, column);
        return cellEditors[convertColumnIndexToModel(column)];
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        if (cellRenderers[column] == null)
            return super.getCellRenderer(row, column);
        return cellRenderers[convertColumnIndexToModel(column)];
    }

    @Override
    public Element getElementForRow(int row) {
        int r = convertRowIndexToModel(row);
        return ((TableEditorModel) getModel()).getElement(r);
    }

    public ElementAttribute getElementAttribute() {
        return getElementAttribute(getSelectedRow(), getSelectedColumn());
    }

    @Override
    public boolean importData(Transferable t) {
        return importData(t, getDropLocation().getRow());
    }

    @SuppressWarnings("unchecked")
    public boolean importData(Transferable t, int index) {
        if (canImport(t.getTransferDataFlavors())) {
            try {
                if (index < 0)
                    return false;
                final ArrayList<Integer> list = (ArrayList<Integer>) t
                        .getTransferData(localListFlavor);
                if (list.size() == 0)
                    return false;

                TableEditorModel model = (TableEditorModel) getModel();

                return model.move(index, list);
            } catch (final UnsupportedFlavorException e) {
                e.printStackTrace();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public class ArrayTransferable implements Transferable {

        ArrayList<Integer> data;

        public ArrayTransferable(final ArrayList<Integer> alist) {
            this.data = alist;
        }

        public Object getTransferData(final DataFlavor flavor)
                throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return data;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{localListFlavor};
        }

        public boolean isDataFlavorSupported(final DataFlavor flavor) {
            if (localListFlavor.equals(flavor))
                return true;
            return false;
        }
    }

    @Override
    public boolean canImport(DataFlavor[] flavors) {
        if (exporting)
            for (final DataFlavor element : flavors) {
                if (localListFlavor.equals(element)) {
                    return true;
                }
            }
        return false;
    }

    @Override
    public Transferable createTransferable() {
        exporting = true;
        ArrayList<Integer> alist = new ArrayList<Integer>();
        for (int i : getSelectedRows())
            alist.add(i);
        return new ArrayTransferable(alist);
    }

    @Override
    public void exportDone(Transferable data, int action) {
        exporting = false;
    }
}

package com.ramussoft.gui.qualifier.table;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

public class RowTransferHandle extends TransferHandler {

    /**
     *
     */
    private static final long serialVersionUID = 1940929704333675541L;

    @Override
    public int getSourceActions(final JComponent c) {
        return MOVE;
    }

    @Override
    protected Transferable createTransferable(final JComponent c) {
        if (c instanceof ImportExport)
            return ((ImportExport) c).createTransferable();
        return null;
    }

    @Override
    public boolean importData(final JComponent comp, final Transferable t) {
        if (comp instanceof ImportExport)
            return ((ImportExport) comp).importData(t);
        return false;
    }

    @Override
    public boolean canImport(final JComponent comp,
                             final DataFlavor[] transferFlavors) {
        if (comp instanceof ImportExport)
            return ((ImportExport) comp).canImport(transferFlavors);
        return false;
    }

    @Override
    protected void exportDone(final JComponent source, final Transferable data,
                              final int action) {
        if (source instanceof ImportExport)
            ((ImportExport) source).exportDone(data, action);
    }
}

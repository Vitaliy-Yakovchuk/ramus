package com.ramussoft.gui.qualifier.table;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public interface ImportExport {

    Transferable createTransferable();

    boolean importData(Transferable t);

    boolean canImport(DataFlavor[] transferFlavors);

    void exportDone(Transferable data, int action);

}

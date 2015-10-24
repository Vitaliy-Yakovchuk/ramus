package com.ramussoft.pb.data;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JOptionPane;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.pb.Main;
import com.ramussoft.pb.Row;

/**
 * Клас призначений для роботи з записами через буфером обміну.
 *
 * @author ZDD
 */

public class RowClipboard {

    public static void copyToClipboard(final Row[] rows, final String text) {

    }

    private static int getTagLevel(final Row r) {
        final boolean e = r.isElement();
        if (r.getParent() == null || r.getParentRow().isElement() != e)
            return 0;
        else
            return getTagLevel(r.getParentRow()) + 1;
    }

    public static void pasteFromClipboard(final Row parent) {
        final String error = Main.dataPlugin.getRowPerrmision(parent);
        if (error != null) {
            JOptionPane.showMessageDialog(null, error, ResourceLoader
                    .getString("no_permision"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (parent != null && parent.getRowType() != Row.TYPE_ROW)
            return;
        int i = 0;
        final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        final Transferable tr = clip.getContents(null);
        byte[] bs;
        try {
            bs = (byte[]) tr.getTransferData(dataFlavor);
            if (bs == null) {
                if (parent == null)
                    return;
                final String t = (String) tr.getTransferData(DataFlavor.stringFlavor);
                if (t == null)
                    return;
                boolean tag = false;
                Row old = parent;
                int oldLevel = 0;
                Row r = null;
                if (parent != null)
                    tag = true;
                while (i < t.length()) {
                    int level = 0;
                    String name = "";
                    while (i < t.length()) {
                        if (t.charAt(i) != '\r')
                            break;
                        i++;
                    }

                    while (i < t.length()) {
                        if (t.charAt(i) == '\t')
                            level++;
                        else
                            break;
                        i++;
                    }

                    while (i < t.length()) {
                        if (t.charAt(i) == ' ')
                            break;
                        i++;
                    }

                    while (i < t.length()) {
                        if (t.charAt(i) != '\n')
                            name += t.charAt(i);
                        else
                            break;
                        i++;
                    }
                    if (!name.equals("")) {
                        if (level > oldLevel && r != null) {
                            old = r;
                            oldLevel = level;
                        } else
                            while (level < oldLevel) {
                                old = old.getParentRow();
                                oldLevel--;
                            }
                        r = Main.dataPlugin.createRow(old, tag);
                        r.setName(name);
                    }
                    i++;
                }
            } else {


            }
        } catch (final Exception e) {
        }

    }

    public static boolean cutToClipboard(final Row[] rows) {
        copyToClipboard(rows);
        return RowSetClass.remove(rows);
    }

    private static DataFlavor dataFlavor = new DataFlavor(byte[].class,
            "XML Data Rows");

    public static void copyToClipboard(final Row[] rows) {
        String copy = "";
        for (final Row r : rows) {
            for (int j = 0; j < getTagLevel(r); j++)
                copy += "\t";
            copy += r.getKod() + " " + r.getName() + "\n";
        }
        copyToClipboard(rows, copy);

    }

}

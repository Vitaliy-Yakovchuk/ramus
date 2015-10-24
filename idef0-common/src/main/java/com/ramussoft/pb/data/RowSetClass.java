package com.ramussoft.pb.data;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.pb.Function;
import com.ramussoft.pb.Main;
import com.ramussoft.pb.Row;

/**
 * Клас в якому реалізовані статичні функції для пошуку запистів в масивах за
 * певними ознаками.
 *
 * @author ZDD
 */

public class RowSetClass {

    public static boolean isNameStartFrom(final Row row, final String text) {
        final String name = row.getName();
        return name.length() >= text.length()
                && name.substring(0, text.length()).equals(text);
    }

    public static boolean isStartSame(final Row row, final String startName,
                                      final boolean wordsOrder) {
        return isStartSame(row.getName(), startName, wordsOrder);
    }

    public static boolean isStartSame(final String name, final String startName,
                                      final boolean wordsOrder) {
        if (name == null)
            return false;
        final StringTokenizer stm = new StringTokenizer(name.toLowerCase());
        final StringTokenizer st = new StringTokenizer(startName.toLowerCase());
        if (wordsOrder) {
            String mw = "";
            while (stm.hasMoreElements())
                mw += stm.nextToken();
            String w = "";
            while (st.hasMoreElements())
                w += st.nextToken();
            if (w.length() <= mw.length()
                    && mw.substring(0, w.length()).compareTo(w) == 0)
                return true;
        } else {
            final Vector<String> a = new Vector<String>();
            final Vector<String> b = new Vector<String>();

            while (stm.hasMoreElements())
                b.add(stm.nextToken());

            while (st.hasMoreElements())
                a.add(st.nextToken());

            for (int i = 0; i < a.size(); i++) {
                final String t = a.get(i);
                boolean bo = false;
                for (int j = 0; j < b.size(); j++) {
                    final String t1 = b.get(j);
                    if (t1.length() >= t.length()) {
                        if (t1.substring(0, t.length()).compareTo(t) == 0) {
                            bo = true;
                            break;
                        }
                    }
                }
                if (!bo) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static boolean isGlobalMoveable(final Row[] rs) {
        return false;
    }

    public static boolean remove(final Row[] rows) {
        for (int i = 0; i < rows.length; i++) {
            final String error = Main.dataPlugin.getRowPerrmision(rows[i]);
            if (error != null) {
                JOptionPane
                        .showMessageDialog(null, error, ResourceLoader
                                        .getString("no_permision"),
                                JOptionPane.WARNING_MESSAGE);
                return false;
            }
            if (!rows[i].isRemoveable() && rows[i] instanceof Function)
                return false;
        }
        for (final Row element : rows)
            Main.dataPlugin.removeRow(element);
        return true;
    }

}

/*
 * Created on 13/8/2005
 */
package com.dsoft.pb.idef.elements;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JOptionPane;

import com.dsoft.pb.idef.ResourceLoader;
import com.dsoft.pb.idef.elements.Readed;

/**
 * @author ZDD
 */
public class ReadedModel implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -1840456817091612645L;

    private final Vector<Readed> readeds = new Vector<Readed>();

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        return readeds.size();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return 2;
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(final int y, final int x) {
        final Readed r = readeds.get(y);
        return x == 0 ? r.getReader() : r.getDate();
    }

    /**
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
     * int, int)
     */
    public void setValueAt(final Object arg0, final int y, final int x) {
        final String s = (String) arg0;
        final Readed r = readeds.get(y);
        if (x == 0)
            r.setReader(s);
        else {
            final String oldd = r.getDate();
            try {
                r.setDate(s);
            } catch (final ParseException e) {
                try {
                    JOptionPane.showMessageDialog(null, s + " - "
                            + ResourceLoader.getString("not_a_date"));
                    r.setDate(oldd);
                } catch (final ParseException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    public String getColumnName(final int i) {
        return ResourceLoader.getString(i == 0 ? "reader" : "date");
    }

    public Readed[] getSortReaded(int count) {
        if (count > readeds.size())
            count = readeds.size();
        final Readed[] res = new Readed[count];
        final Readed[] all = getAllReaded();
        int j = 0;
        for (int i = all.length - count; i < all.length; i++) {
            res[j] = all[i];
            j++;
        }
        return res;
    }

    public Readed[] getAllReaded() {
        final Readed[] all = new Readed[readeds.size()];
        for (int i = 0; i < all.length; i++)
            all[i] = readeds.get(i);
        Arrays.sort(all);
        return all;
    }

    public Readed addReaded() {
        final Readed res = new Readed();
        readeds.add(res);
        return res;
    }

    public void removeReaded(final int i) {
        readeds.remove(i);
    }

    public void clearReadeds() {
        readeds.clear();
    }

}

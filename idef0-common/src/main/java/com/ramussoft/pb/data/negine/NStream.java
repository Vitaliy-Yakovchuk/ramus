package com.ramussoft.pb.data.negine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.dsoft.pb.idef.ResourceLoader;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.idef0.attribute.AnyToAnyPersistent;
import com.ramussoft.pb.Row;
import com.ramussoft.pb.Stream;
import com.ramussoft.pb.data.RowFactory;

public class NStream extends NRow implements Stream {

    public NStream(NDataPlugin dataPlugin, Element element,
                   com.ramussoft.database.common.RowSet rowSet,
                   Attribute[] attributes, Object[] objects) {
        super(dataPlugin, element, rowSet, attributes, objects);
        rowType = TYPE_STREAM;
        if (element != null)
            setIdProperties();
    }

    /**
     * Масив класифікаторів, пов’язаних з даним.
     */

    Row[] rows = new Row[]{};

    String[] rowStatuses = new String[]{};

    public boolean isEmptyName() {
        return super.getName().trim().equals("");
    }

    public void setEmptyName(final boolean emptyName) {
        if (emptyName)
            super.setName("");
    }

    @Override
    public String getName() {
        if (isEmptyName())
            return getTitle();
        else
            return super.getName();
    }

    /**
     * Метод, який повертає назву стрілки, якщо назва - пустий рядок, то
     * повертається набір назв пов’язаних класифікаторів розділених крапкою з
     * комою і пробілом, теж саме що викликати метод getTitle("; ").
     *
     * @return Назва сектора.
     */

    public static String getTitle(final Row[] rows) {
        StringBuilder sb = new StringBuilder();
        final String div = ";\n";
        boolean first = true;
        for (Row row : rows) {
            if (first)
                first = false;
            else
                sb.append(div);
            sb.append(row.getName());
            String status = row.getAttachedStatus();
            if (status != null) {
                int i = status.indexOf('|');
                if (i >= 0) {
                    status = status.substring(i + 1);
                    if (status.trim().length() == 0)
                        status = null;
                }
            }
            if (status != null) {
                sb.append(" (");
                sb.append(status);
                sb.append(')');
            }

        }
        return sb.toString();
    }

    /**
     * Повертає назву чи набір назв пов’язаних класифікаторів (якщо назва пустий
     * рядок).
     *
     * @param div Розділювач назв пов’язаних класифікаторів.
     * @return Назва чи набір пов’язаних класифікаторів.
     */

    public String getTitle() {
        final String res = super.getName();
        if (res != null && !res.equals(""))
            return res;
        checkRows();
        return getTitle(getARows());
    }

    private void checkRows() {
        boolean ok = true;
        for (final Row row : getARows())
            if (row == null) {
                ok = false;
                break;
            }
        if (!ok) {
            System.err.println("NULL IN ADDED ROWS");
            final Vector<Row> tmp = new Vector<Row>();
            for (final Row row : getARows())
                if (row != null) {
                    tmp.add(row);
                }
            setARows(tmp.toArray(new Row[tmp.size()]));
            for (int i = 0; i < getARows().length; i++)
                getARows()[i] = tmp.get(i);
        }
    }

    @Override
    public void setIdProperties() {
        List<AnyToAnyPersistent> list = (List<AnyToAnyPersistent>) this
                .getAttribute(((NDataPlugin) dataPlugin).addedRows);
        if (list == null)
            return;
        rows = new Row[list.size()];
        rowStatuses = new String[list.size()];
        for (int i = 0; i < rows.length; i++) {
            Row row = ((NDataPlugin) dataPlugin).findRowByGlobalId(list.get(i)
                    .getOtherElement());
            getARows()[i] = row;
            rowStatuses[i] = list.get(i).getElementStatus();
        }
    }

    @Override
    public void updateObject(int i, Object newValue) {
        super.updateObject(i, newValue);
        if (rowSet.getAttributesWithH()[i]
                .equals(((NDataPlugin) dataPlugin).addedRows))
            setIdProperties();
    }

    /**
     * Додає масив рядків, до під’єднаних до потока.
     *
     * @param rows Масив, що буде доданий.
     */

    public void addRows(final Row[] rows) {
        Row[] rows2 = this.getARows();
        String[] st = new String[rows2.length];
        for (int i = 0; i < st.length; i++) {
            st[i] = rows2[i].getAttachedStatus();
        }
        this.setARows(RowFactory.addRows(this.getARows(), rows));
        Row[] aRows = this.getARows();
        String[] st2 = new String[aRows.length];
        for (int i = 0; i < st2.length; i++) {
            st2[i] = aRows[i].getAttachedStatus();
        }
        if (Arrays.equals(rows2, aRows) && Arrays.equals(st, st2)) {
            return;
        }
        saveAdded();
    }

    /**
     * Видаляє масив рядків, з під’єднаних.
     *
     * @param rows Масив елементів, що буде видалений.
     */

    public void removeRows(final Row[] rows) {
        this.setARows(RowFactory.removeRows(this.getARows(), rows));
        saveAdded();
    }

    public void saveAdded() {
        Row[] aRows = getARows();
        List<AnyToAnyPersistent> list = new ArrayList<AnyToAnyPersistent>(
                aRows.length);
        int i = 0;
        for (Row row : aRows) {
            if (row != null) {
                AnyToAnyPersistent e = new AnyToAnyPersistent(
                        ((com.ramussoft.database.common.Row) row)
                                .getElementId());
                if (row.getAttachedStatus() != null
                        && row.getAttachedStatus().trim().length() > 0)
                    e.setElementStatus(row.getAttachedStatus());
                list.add(e);
                rowStatuses[i] = aRows[i].getAttachedStatus();
            }
            i++;
        }
        this.setAttribute(((NDataPlugin) dataPlugin).addedRows, list);

    }

    /**
     * Повертає масив рядків, які під’єднанані до потоку.
     *
     * @return Масив елементів класифікатора, під’єднані до потоку.
     */

    public Row[] getAdded() {
        for (Row row : getARows()) {
            if (row == null) {
                ArrayList<Row> list = new ArrayList<Row>(getARows().length);
                for (Row row2 : getARows()) {
                    if (row2 != null)
                        list.add(row2);
                }
                setARows(list.toArray(new Row[list.size()]));
                break;
            }
        }
        Row[] rows = getARows();
        for (int i = 0; i < rows.length; i++)
            rows[i].setAttachedStatus(rowStatuses[i]);
        Arrays.sort(getARows());
        return getARows();
    }

    /**
     * Перевіряє, чи співпадає назва з назвою класифікатора потоків.
     *
     * @param name Назва, яку необхідно перевірити на відповідність.
     * @return <code>true</code>, якщо назва співпадає, <code>false</code>, якщо
     * назва не співпадає.
     */

    public boolean equilsName(final String name) {
        if (name.equals(getTitle()))
            return true;
        return false;
    }

    /**
     * Метод прив’язує набір елементів до класифікатора потоків.
     *
     * @param rows Набір елементів, які будуть прив’язані.
     */

    public void setRows(Row[] rows) {
        try {
            Arrays.sort(rows);
        } catch (NullPointerException exception) {
            exception.printStackTrace();
        }
        Row[] rows2 = this.getARows();
        this.setARows(rows);
        rows = this.getARows();
        if (Arrays.equals(rows2, rows)) {
            boolean ret = true;
            for (int i = rows2.length - 1; i >= 0; --i) {
                if (rows2[i].getAttachedStatus() != null
                        && rows[i].getAttachedStatus() != null) {
                    if (!rows2[i].getAttachedStatus().equals(
                            rows[i].getAttachedStatus())) {
                        ret = false;
                        break;
                    }
                }
            }
            if (ret)
                return;
        }
        saveAdded();
    }

    /**
     * Повертає getTitle, якщо getTitle пустий рядок то повертається
     * локалізоване слово Arrow.
     */

    @Override
    public String toString() {
        final String res = getTitle();
        if (res.equals(""))
            return ResourceLoader.getString("Arrow");
        return res;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public boolean isMoveable() {
        return false;
    }

    @Override
    public boolean isRemoveable() {
        if (!super.isRemoveable())
            return false;
        /*
         * else { List<Element> list = engine.findElements(IDEF0Plugin
		 * .getBaseStreamQualifier(engine).getId(), IDEF0Plugin
		 * .getSectorStreamAttribute(engine), getElementId()); if (list.size() >
		 * 0) return false; }
		 */
        return true;
    }

    @Override
    public String getToolTipText(final boolean big) {
        if (getARows() == null || getARows().length == 0)
            return super.getToolTipText(big);
        String r = "<table><tr><td><b>" + ResourceLoader.getString("added_row")
                + "</b></td>\n" + "<td><b>"
                + ResourceLoader.getString("clasificator") + "</b></td></tr>";
        String t1 = new String(r);
        for (int i = 0; i < getARows().length; i++) {
            final String row = "<tr><td>" + getARows()[i].getKod() + " "
                    + getARows()[i].getName() + "</td><td>"
                    + getARows()[i].getRecParent().getName() + "</td></tr>";
            if (i % 2 == 0)
                r += row;
            else
                t1 += row;
        }
        r += "</table>";
        t1 += "</table>";
        t1 = "<table><tr><td>" + r + "</td><td>" + t1 + "</td></tr>\n"
                + "</table>";
        r = "<hr>" + "<h4><center>" + ResourceLoader.getString("added_rows")
                + "</center></h4>" + t1 + "<hr>";
        r = "<html><body><h3><b><center>\n" + super.getName()
                + "</center><b></h3>" + r + "</body></html>\n";
        return r;
    }

	/*
	 * public boolean equals(Object obj) { if (obj instanceof Stream) { if
	 * (super.equals(obj)) return true; FileStream stream = (FileStream) obj; if
	 * (!stream.name.equals(name)) return false; if (stream.rows.length !=
	 * rows.length) return false; for (int i = 0; i < rows.length; i++) if
	 * ((rows[i] == null) || (!rows[i].equals(stream.rows[i]))) return false;
	 * return true; } return super.equals(obj); }
	 */

    @Override
    public boolean isCanHaveChilds() {
        return false;
    }

    @Override
    protected boolean isSameName(final Row row, final String name) {
        if (row instanceof Stream) {
            if (((Stream) row).isEmptyName())
                return false;
        }
        return super.isSameName(row, name);
    }

    /**
     * @param rows the rows to set
     */
    private void setARows(Row[] rows) {
        this.rows = rows;
        this.rowStatuses = new String[rows.length];
        for (int i = 0; i < rows.length; i++)
            rowStatuses[i] = rows[i].getAttachedStatus();
    }

    /**
     * @return the rows
     */
    private Row[] getARows() {
        if (rows == null)
            setIdProperties();
        return rows;
    }
}

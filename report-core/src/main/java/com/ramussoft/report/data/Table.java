package com.ramussoft.report.data;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public class Table {

    private List<Row[]> rows = new ArrayList<Row[]>();

    private int columnCount;

    public Table(int columnCount) {
        this.columnCount = columnCount;
    }

    public void addRows(Row[] rows) {
        this.rows.add(rows);
    }

    public int getColumnCount() {
        return columnCount;
    }

    public int getRowCount() {
        return rows.size();
    }

    public Row getCell(int row, int column) {
        return getCellAt(row, column);
    }

    public Row getCellAt(int row, int column) {
        return rows.get(row)[column];
    }

    public void removeDublicates() {
        HashSet<RowArrayHolder> arrayHolders = new HashSet<RowArrayHolder>();

        List<Row[]> newRows = new ArrayList<Row[]>();

        for (Row[] rows : this.rows) {
            RowArrayHolder holder = new RowArrayHolder(rows);
            if (!arrayHolders.contains(holder)) {
                arrayHolders.add(holder);
                newRows.add(rows);
            }
        }
        rows = newRows;
    }

    private class RowArrayHolder {
        Row[] rows;

        public RowArrayHolder(Row[] rows) {
            this.rows = rows;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(rows);
            return result;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof RowArrayHolder))
                return false;
            RowArrayHolder other = (RowArrayHolder) obj;
            if (!Arrays.equals(rows, other.rows))
                return false;
            return true;
        }

    }

    public void sort(final int column) {
        Collections.sort(rows, new Comparator<Row[]>() {
            @Override
            public int compare(Row[] o1, Row[] o2) {
                return o1[column].compareTo(o2[column]);
            }
        });
    }

    public void sort(final int column, final String attribute) {
        Collections.sort(rows, new Comparator<Row[]>() {

            Collator collator = Collator.getInstance();

            @SuppressWarnings("unchecked")
            @Override
            public int compare(Row[] o1, Row[] o2) {
                Row row1 = o1[column];
                Row row2 = o2[column];
                Object object1 = row1.getAttribute(attribute);
                Object object2 = row2.getAttribute(attribute);

                if (object1 == null) {
                    if (object2 == null)
                        return 0;
                    else
                        return 1;
                } else if (object2 == null)
                    return -1;
                if (!(object1 instanceof String))
                    if ((object1 instanceof Comparable)
                            && (object2 instanceof Comparable)) {

                        return ((Comparable) object1).compareTo(object2);
                    }

                return collator.compare(object1.toString(), object2.toString());
            }
        });
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("<table>\n");
        for (Row[] rows : this.rows) {
            sb.append("<tr>\n");
            for (Row row : rows) {
                sb.append("<td>");
                sb.append(row);
                sb.append("<td>\n");
            }
            sb.append("<tr>\n");
        }
        sb.append("</table>\n");
        return sb.toString();
    }
}

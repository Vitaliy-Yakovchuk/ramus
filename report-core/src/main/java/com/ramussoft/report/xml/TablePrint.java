package com.ramussoft.report.xml;

import java.text.Collator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import com.ramussoft.report.SerialNumber;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.DataException;
import com.ramussoft.report.data.Out;
import com.ramussoft.report.data.Rows;

public abstract class TablePrint extends ElementPrint {

    private String query;

    private Rows rows;

    private List<ColumnHeader> columnHeaders;

    private List<ColumnBody> columnBodies;

    private Collator collator = Collator.getInstance();

    public TablePrint(Hashtable<String, String> attributes,
                      List<ColumnHeader> columnHeaders, List<ColumnBody> columnBodies,
                      Data data) {
        super(attributes);
        String query = attributes.get("Quary");
        if ((query == null) || (query.length() == 0))
            throw new DataException("Error.tableQueryEmpty",
                    "Table's query is empty, please set the query first!!!");
        this.columnHeaders = columnHeaders;
        this.columnBodies = columnBodies;
        if ((query.startsWith("[")) && (query.endsWith("]")))
            this.query = query.substring(1, query.length() - 1);
        else
            this.query = query;
        rows = data.getBaseRowsByQuery(this.query);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void print(Out out, Data data) {
        if (!data.isPrintFor(attributes.get("printFor")))
            return;
        if (rows.next() != null) {
            onNext();

            String style = attributes.get("Style");
            if (style == null)
                style = "";
            if (style.length() > 0)
                style = " style=\"" + style + "\"";
            String border;
            if ((border = attributes.get("Border")) != null)
                out.println("<table width =\"100%\" border=\"" + border + "\""
                        + style + ">");
            else
                out.println("<table width =\"100%\"" + style + ">");
            out.println("<tr>");
            for (ColumnHeader header : columnHeaders) {
                header.print(out, data);
            }
            out.println("</tr>");

            HashSet<TableRow> tableRows = new HashSet<TableRow>();
            List<TableRow> tableRowsList = new ArrayList<TableRow>();
            List<Index> orders = new ArrayList<Index>(columnHeaders.size());
            int index = 0;
            for (ColumnHeader header : columnHeaders) {
                String order = header.attributes.get("ColumnOrderNumber");
                if ((order != null) && (order.length() > 0)) {
                    try {
                        orders.add(new Index(index, Integer.parseInt(order)));
                    } catch (Exception e) {
                        throw new DataException(
                                "Error.wrongNumberFormatForOrderNumber", e
                                .getLocalizedMessage(), order);
                    }
                }
                index++;
            }

            Collections.sort(orders);

            int[] indexes = null;
            if (orders.size() > 0) {
                indexes = new int[orders.size()];
                for (int i = 0; i < indexes.length; i++)
                    indexes[i] = orders.get(i).index;
            }

            do {
                List[] lists = new List[columnBodies.size()];
                for (int i = 0; i < lists.length; i++) {
                    lists[i] = new ArrayList(4);
                    ColumnBody body = columnBodies.get(i);
                    body.setTablePrint(this);
                    body.print(lists[i], data);
                }
                TableRow holder = new TableRow(lists, indexes);
                if (!tableRows.contains(holder)) {
                    tableRows.add(holder);
                    tableRowsList.add(holder);
                }

            } while (rows.next() != null);
            TableRow[] tableRows2;
            if (indexes != null) {
                tableRows2 = new TableRow[tableRows.size()];
                Iterator<TableRow> iterator = tableRows.iterator();
                int j = 0;
                while (iterator.hasNext()) {
                    tableRows2[j] = iterator.next();
                    j++;
                }

                Arrays.sort(tableRows2);
            } else {
                tableRows2 = tableRowsList.toArray(new TableRow[tableRowsList
                        .size()]);
            }

            int number = 1;

            for (TableRow tableRow : tableRows2) {
                out.println("<tr>");
                List[] lists = tableRow.lists;
                for (int i = 0; i < lists.length; i++) {
                    out.print(columnBodies.get(i).getTdStartTag());
                    for (Object object : lists[i]) {
                        if (object instanceof SerialNumber)
                            ((SerialNumber) object).setNumber(number);
                        out.print(object);
                    }
                    out.println("</td>");
                }
                out.println("</tr>");
                number++;
            }

            out.println("</table>");
        }
    }

    private class Index implements Comparable<Index> {

        int index;
        int number;

        Index(int index, int number) {
            this.index = index;
            this.number = number;
        }

        @Override
        public int compareTo(Index o) {
            if (number < o.number)
                return -1;
            if (number > o.number)
                return 1;
            return 0;
        }
    }

    private class TableRow implements Comparable<TableRow> {

        private String value;

        @SuppressWarnings("unchecked")
        private List[] lists;

        @SuppressWarnings("unchecked")
        public TableRow(List[] lists, int[] indexes) {
            this.lists = lists;
            if (indexes != null) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < indexes.length; i++) {
                    List<Object> list = lists[indexes[i]];
                    for (Object object : list) {
                        if ((object != null)
                                && (!(object instanceof SerialNumber))) {
                            sb.append(object.toString());
                            sb.append('!');// For correct order
                        }
                    }
                }
                this.value = sb.toString();
            }
        }

        @Override
        public int compareTo(TableRow o) {
            return collator.compare(value, o.value);
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            if (value == null)
                return Arrays.hashCode(lists);
            final int prime = 31;
            int result = 1;
            result = prime * result + ((value == null) ? 0 : value.hashCode());
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
            if (!(obj instanceof TableRow))
                return false;
            TableRow other = (TableRow) obj;
            if (value == null)
                return Arrays.equals(lists, other.lists);

            if (value == null) {
                if (other.value != null)
                    return false;
            } else if (!value.equals(other.value))
                return false;
            return true;
        }
    }

    public String getQuery() {
        return query;
    }

    public Rows getRows() {
        return rows;
    }

    public abstract void onNext();

}

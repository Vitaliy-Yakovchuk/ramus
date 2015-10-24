package com.ramussoft.report.xml;

import java.util.ArrayList;

import java.util.List;

import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.DataException;
import com.ramussoft.report.data.Out;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.Rows;

public class TableBodyQuery extends ElementPrint {

    private String query;

    private TablePrint tablePrint;

    public TableBodyQuery(String query, TablePrint tablePrint) {
        super(null);
        this.query = query;
        this.tablePrint = tablePrint;
    }

    @Override
    public void print(Out out, Data data) {
        throw new RuntimeException(
                "call void print(List<Object> buffer, Data data) instead of this");
    }

    @Override
    public void print(List<Object> buffer, Data data) {
        String tableQuary = tablePrint.getQuery();
        String[] words = query.split("\\.");
        String[] tableWords = tableQuary.split("\\.");
        int i = 0;
        while (words[i].equals(tableWords[i])) {
            if (i + 1 >= tableWords.length) {
                i++;
                break;
            }
            if (i + 2 >= words.length) {
                i++;
                break;
            }
            i++;
        }

        if (i == 0) {
            String name1 = "Unknown";
            String name2 = "Unknown";
            if (words.length > 0)
                name2 = words[0];
            if (tableWords.length > 0)
                name1 = tableWords[0];
            throw new DataException("Error.differentBaseQualifiers",
                    "Report contains diffetents base qualifiers in queries",
                    name1, name2);
        }

        Rows rows = tablePrint.getRows();
        int diff = tableWords.length - i;
        while (diff > 0) {
            diff--;
            rows = rows.getParent();
        }

        if (rows == null)
            rows = data.getBaseRows();

        if (i + 1 < words.length) {
            StringBuffer sb = new StringBuffer();
            boolean first = true;
            for (int j = i; j < words.length - 1; j++) {
                if (first)
                    first = false;
                else
                    sb.append('.');
                sb.append(words[j]);
            }
            rows = data.getRowsByQuery(rows.getCurrent(), sb.toString());
            first = true;
            List<Row> list = new ArrayList<Row>();
            Row row;
            while ((row = rows.next()) != null) {
                if (list.indexOf(row) < 0)
                    list.add(row);
                else
                    continue;
                if (first)
                    first = false;
                else
                    buffer.add("; ");
                buffer.add(rows.getAttribute(words[words.length - 1]));
            }
        } else {
            buffer.add(rows.getAttribute(words[words.length - 1]));
        }
    }
}

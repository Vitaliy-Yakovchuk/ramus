package com.ramussoft.report.xml;

import java.util.ArrayList;
import java.util.List;

import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Out;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.Rows;

public abstract class QueryPrint extends ElementPrint {

    private String query;

    private Rows rows;

    public QueryPrint(String query, Data data) {
        super(null);
        this.query = query;
        int begin = 0;
        for (int i = query.length() - 1; i >= 0; i--) {
            if (query.charAt(i) == '.') {
                begin = i;
                break;
            }
        }

        rows = data.getBaseRowsByQuery(query.substring(0, begin));
    }

    @Override
    public void print(Out out, Data data) {
        int begin = 0;
        for (int i = query.length() - 1; i >= 0; i--) {
            if (query.charAt(i) == '.') {
                begin = i;
                break;
            }
        }

        String name = "";
        if (begin + 1 < query.length())
            name = query.substring(begin + 1);
        boolean first = true;
        Row row;
        List<Row> list = new ArrayList<Row>();
        while ((row = rows.next()) != null) {
            if (list.indexOf(row) < 0)
                list.add(row);
            else
                continue;
            if (!first)
                out.print("; ");
            else {
                first = false;
                beforePrint(out, data);
            }
            out.print(row.getAttribute(name, rows));
        }
        if (!first) {
            afterPrint(out, data);
        }
    }

    protected abstract void afterPrint(Out out, Data data);

    protected abstract void beforePrint(Out out, Data data);

}

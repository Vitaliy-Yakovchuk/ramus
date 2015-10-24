package com.ramussoft.report.data;

import java.util.ArrayList;
import java.util.List;

public class RowsSum extends Rows {

    /**
     *
     */
    private static final long serialVersionUID = -6839455884046643009L;

    private List<Rows> contaion = new ArrayList<Rows>();

    private int cpos = -1;

    private int sum = 0;

    public RowsSum(Data data) {
        super(null, data, false);
    }

    public void addRows(Rows rows) {
        contaion.add(rows);
        addAll(rows);
    }

    @Override
    public Row next() {
        Row next = super.next();
        if (next != null) {
            sum++;
            if (cpos < 0) {
                parent.next();
                cpos++;
                sum = 0;
                int size = contaion.get(cpos).size();
                while (size == 0) {
                    parent.next();
                    cpos++;
                    size = contaion.get(cpos).size();
                }
            } else if (sum >= contaion.get(cpos).size()) {
                parent.next();
                cpos++;
                sum = 0;
                int size = contaion.get(cpos).size();
                while (size == 0) {
                    parent.next();
                    cpos++;
                    size = contaion.get(cpos).size();
                }
            }
        }
        return next;
    }

    @Override
    public void first() {
        super.first();
        if (parent != null)
            parent.first();
        cpos = -1;
        sum = 0;
    }
}

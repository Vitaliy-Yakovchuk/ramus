package com.dsoft.pb.idef.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.idef0.IDEF0Plugin;
import com.ramussoft.idef0.attribute.AnyToAnyPersistent;
import com.ramussoft.report.XMLReportEngine;
import com.ramussoft.report.data.Data;
import com.ramussoft.report.data.Row;
import com.ramussoft.report.data.RowSet;
import com.ramussoft.report.data.Rows;

public class Stream extends Row {

    private List<Row> addedRows = null;

    private String rName;

    public Stream(Element element, RowSet rowSet, Attribute[] attributes,
                  Object[] objects, Data data) {
        super(element, rowSet, attributes, objects, data);
    }

    @Override
    public String getName() {
        if (element.getName().length() > 0)
            return element.getName();
        getAddedRows();
        return rName;
    }

    @Override
    public Object getAttribute(String name, Rows rows) {
        if (XMLReportEngine.KEYWORDS.hasName("Name", name)) {
            return getAddedRows();
        }
        return super.getAttribute(name, rows);
    }

    @SuppressWarnings("unchecked")
    public List<Row> getAddedRows() {
        if (addedRows == null) {
            Engine engine = data.getEngine();
            Attribute attribute = IDEF0Plugin.getStreamAddedAttribute(engine);
            List<AnyToAnyPersistent> list = (List) engine.getAttribute(element,
                    attribute);
            addedRows = new ArrayList<Row>(list.size());
            for (AnyToAnyPersistent p : list) {
                Row row = data.findRow(p.getOtherElement());
                if (row != null) {
                    row = row.createCopy();
                    row.setElementStatus(p.getElementStatus());
                    addedRows.add(row);
                }
            }
            Collections.sort(addedRows);
            StringBuffer buff = new StringBuffer();
            boolean first = true;
            for (Row row : addedRows) {
                if (first)
                    first = false;
                else
                    buff.append("; ");
                buff.append(row.getName());
            }
            rName = buff.toString();
        }
        return addedRows;
    }

    public String getRName() {
        if (addedRows == null)
            getAddedRows();
        return rName;
    }
}

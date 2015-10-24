package com.ramussoft.pb.print.xml;

import java.io.InputStream;

import javax.xml.namespace.QName;

import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.types.GlobalId;

public class ReportGenerator {

    public static final QName QUERY = new QName("query");

    public static final QName ELEMENT = new QName("element");

    public static final QName TYPE = new QName("type");

    public static ReportGenerator getReportGenerator(DataPlugin dataPlugin,
                                                     GlobalId patternId) {
        ReportGenerator res = new ReportGenerator(dataPlugin
                .getNamedData("reports/patterrns/" + patternId.toString()));

        return res;
    }


    public ReportGenerator(InputStream namedData) {
    }
}

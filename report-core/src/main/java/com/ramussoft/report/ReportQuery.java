package com.ramussoft.report;

import java.util.HashMap;
import java.util.List;

import com.ramussoft.common.Element;
import com.ramussoft.common.Qualifier;

public interface ReportQuery {

    String getHTMLReport(Element element, HashMap<String, Object> map);

    List<Element> getHTMLReports();

    Qualifier getHTMLReportQuery(Element element);

}

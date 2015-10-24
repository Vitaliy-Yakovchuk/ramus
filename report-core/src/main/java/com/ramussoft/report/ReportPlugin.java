package com.ramussoft.report;

import com.ramussoft.common.AbstractPlugin;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.IEngine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.simple.HierarchicalPlugin;

public class ReportPlugin extends AbstractPlugin {

    private static final String REPORT_CORE_PLUGIN = "ReportCorePlugin";

    private static final String PLUGIN = "Plugin";

    private static final String REPORTS_QUALIFIER = "F_REPORTS_QUALIFIER";

    private static final String REPORT_NAME = "F_REPORT_NAME";

    private static final String REPORT_QUALIFIER = "F_REPORT_QUALIFIER";

    private static final String REPORT_TYPE = "F_REPORT_TYPE";

    public static final String TYPE_XML = "XML";

    public static final String TYPE_JSSP = "JSSP";

    public static final String TYPE_JSSP_DOC_BOOK = "DOC_BOOK";

    private Qualifier reports;

    private Attribute reportName;

    private Attribute reportQualifier;

    private Attribute reportType;

    @Override
    public String getName() {
        return REPORT_CORE_PLUGIN;
    }

    @Override
    public void init(Engine engine, AccessRules rules) {
        super.init(engine, rules);

        engine.setPluginProperty(getName(), PLUGIN, this);

        reports = engine.getSystemQualifier(REPORTS_QUALIFIER);
        if (reports == null) {
            reports = engine.createSystemQualifier();
            reportName = createAttribute(REPORT_NAME, new AttributeType("Core",
                    "Text", true));

            reportQualifier = createAttribute(REPORT_QUALIFIER,
                    new AttributeType("Core", "Long", true));

            reports.setName(REPORTS_QUALIFIER);
            reports.getAttributes().add(reportName);
            reports.getSystemAttributes().add(reportQualifier);
            reports.setAttributeForName(reportName.getId());
            Attribute hierarchical = (Attribute) engine.getPluginProperty(
                    "Core", HierarchicalPlugin.HIERARHICAL_ATTRIBUTE);
            reports.getSystemAttributes().add(hierarchical);

            engine.updateQualifier(reports);
        } else {

            reportName = engine.getSystemAttribute(REPORT_NAME);

            reportQualifier = engine.getSystemAttribute(REPORT_QUALIFIER);

        }

        reportType = engine.getSystemAttribute(REPORT_TYPE);
        if (reportType == null) {
            reportType = createAttribute(REPORT_TYPE, new AttributeType("Core",
                    "Text", true));
            reports.getSystemAttributes().add(reportType);
            engine.updateQualifier(reports);
        }

    }

    private Attribute createAttribute(String attributeName,
                                      AttributeType attributeType) {
        Attribute attribute = engine.createSystemAttribute(attributeType);
        attribute.setName(attributeName);
        engine.updateAttribute(attribute);
        return attribute;
    }

    public static ReportPlugin getReportPlugin(Engine engine) {
        return (ReportPlugin) engine.getPluginProperty(REPORT_CORE_PLUGIN,
                PLUGIN);
    }

    public static Qualifier getReportsQualifier(Engine engine) {
        return getReportPlugin(engine).reports;
    }

    public static void setReportName(Engine engine, Element element, String name) {
        Attribute attribute = getReportPlugin(engine).reportName;
        engine.setAttribute(element, attribute, name);
    }

    public static Attribute getReportNameAttribute(Engine engine) {
        return getReportPlugin(engine).reportName;
    }

    public static Attribute getReportQualifierElementIdAttribute(Engine engine) {
        return getReportPlugin(engine).reportQualifier;
    }

    public static Attribute getReportTypeAttribute(Engine engine) {
        return getReportPlugin(engine).reportType;
    }

    public static long getReportQualifierElementId(Engine engine,
                                                   Element element) {
        Attribute attribute = getReportQualifierElementIdAttribute(engine);
        Long res = (Long) engine.getAttribute(element, attribute);
        return (res == null) ? -1l : res.longValue();
    }

    public static void setReportQualifierElementId(Engine engine,
                                                   Element element, long id) {
        Attribute attribute = getReportQualifierElementIdAttribute(engine);
        engine.setAttribute(element, attribute, id);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class getFunctionalInterface() {
        return ReportQuery.class;
    }

    @Override
    public Object createFunctionalInterfaceObject(Engine engine, IEngine iEngine) {
        return new ReportQueryImpl(engine);
    }

    @Override
    public boolean isCriticatToOpenFile() {
        return false;
    }

}

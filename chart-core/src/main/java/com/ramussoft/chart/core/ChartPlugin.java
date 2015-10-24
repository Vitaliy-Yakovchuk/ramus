package com.ramussoft.chart.core;

import java.util.ArrayList;
import java.util.List;

import com.ramussoft.common.AbstractPlugin;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;

public class ChartPlugin extends AbstractPlugin {

    public static final String PLUGIN_NAME = "Chart";

    public static final String QUALIFIER_CHARTS = "QUALIFIER_CHARTS";

    public static final String QUALIFIER_CHART_SETS = "QUALIFIER_CHART_SETS";

    public static final String QUALIFIER_CHART_LINKS = "QUALIFIER_CHART_LINKS";

    public static final String ATTRIBUTE_CHART_IN_SET_X = "ATTRIBUTE_CHART_SET_X";

    public static final String ATTIRBUTE_CHART_IN_SET_Y = "ATTRIBUTE_CHART_SET_Y";

    public static final String ATTRIBUTE_CHART_IN_SET_WIDTH = "ATTRIBUTE_CHART_SET_WIDTH";

    public static final String ATTRIBUTE_CHART_IN_SET_HEIGHT = "ATTRIBUTE_CHART_SET_HEIGHT";

    public static final String ATTRIBUTE_CHART = "ATTRIBUTE_CHART";

    public static final String ATTRIBUTE_CHART_SET = "ATTRIBUTE_CHART_SET";

    private Qualifier charts;

    private Qualifier chartSets;

    private Qualifier chartLinks;

    private Attribute chartInSetX;

    private Attribute chartInSetY;

    private Attribute chartInSetWidth;

    private Attribute chartInSetHeight;

    private Attribute chart;

    private Attribute chartSet;

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public void init(Engine engine, AccessRules rules) {
        super.init(engine, rules);
        engine.setPluginProperty(PLUGIN_NAME, "This", this);
        charts = engine.getSystemQualifier(QUALIFIER_CHARTS);
        if (charts == null) {
            chartInSetX = createDouble(ATTRIBUTE_CHART_IN_SET_X);
            chartInSetY = createDouble(ATTIRBUTE_CHART_IN_SET_Y);
            chartInSetWidth = createDouble(ATTRIBUTE_CHART_IN_SET_WIDTH);
            chartInSetHeight = createDouble(ATTRIBUTE_CHART_IN_SET_HEIGHT);

            chart = createLong(ATTRIBUTE_CHART);
            chartSet = createLong(ATTRIBUTE_CHART_SET);

            charts = createCharts();
            chartSets = createChartSets();
            chartLinks = createChartLinks();

        } else {
            chartSets = engine.getSystemQualifier(QUALIFIER_CHART_SETS);

            chartInSetX = getAttribute(ATTRIBUTE_CHART_IN_SET_X);
            chartInSetY = getAttribute(ATTIRBUTE_CHART_IN_SET_Y);
            chartInSetWidth = getAttribute(ATTRIBUTE_CHART_IN_SET_WIDTH);
            chartInSetHeight = getAttribute(ATTRIBUTE_CHART_IN_SET_HEIGHT);

            chart = getAttribute(ATTRIBUTE_CHART);
            chartSet = getAttribute(ATTRIBUTE_CHART_SET);

            chartLinks = engine.getSystemQualifier(QUALIFIER_CHART_LINKS);
        }
    }

    private Attribute createLong(String attributeName) {
        Attribute attribute = engine.createSystemAttribute(new AttributeType(
                "Core", "Long"));
        attribute.setName(attributeName);
        engine.updateAttribute(attribute);
        return attribute;
    }

    private Qualifier createChartLinks() {
        Qualifier qualifier = engine.createSystemQualifier();
        qualifier.setName(QUALIFIER_CHART_LINKS);

        qualifier.getSystemAttributes().add(chartInSetX);
        qualifier.getSystemAttributes().add(chartInSetY);
        qualifier.getSystemAttributes().add(chartInSetWidth);
        qualifier.getSystemAttributes().add(chartInSetHeight);
        qualifier.getSystemAttributes().add(chartSet);
        qualifier.getSystemAttributes().add(chart);

        engine.updateQualifier(qualifier);
        return qualifier;
    }

    private Attribute getAttribute(String attibuteName) {
        return engine.getSystemAttribute(attibuteName);
    }

    private Attribute createDouble(String attributeName) {
        Attribute attribute = engine.createSystemAttribute(new AttributeType(
                "Core", "Double"));
        attribute.setName(attributeName);
        engine.updateAttribute(attribute);
        return attribute;
    }

    private Qualifier createCharts() {
        Qualifier qualifier = engine.createSystemQualifier();
        qualifier.setName(QUALIFIER_CHARTS);
        Attribute attribute = StandardAttributesPlugin
                .getAttributeNameAttribute(engine);
        qualifier.getAttributes().add(attribute);
        qualifier.getSystemAttributes().add(
                StandardAttributesPlugin.getHierarchicalAttribute(engine));
        qualifier.setAttributeForName(attribute.getId());
        engine.updateQualifier(qualifier);
        return qualifier;
    }

    private Qualifier createChartSets() {
        Qualifier qualifier = engine.createSystemQualifier();
        qualifier.setName(QUALIFIER_CHART_SETS);
        Attribute attribute = StandardAttributesPlugin
                .getAttributeNameAttribute(engine);
        qualifier.getAttributes().add(attribute);
        qualifier.getSystemAttributes().add(
                StandardAttributesPlugin.getHierarchicalAttribute(engine));
        qualifier.setAttributeForName(attribute.getId());
        engine.updateQualifier(qualifier);
        return qualifier;
    }

    public static Qualifier getCharts(Engine engine) {
        return getChartPlugin(engine).charts;
    }

    public static Qualifier getChartSets(Engine engine) {
        return getChartPlugin(engine).chartSets;
    }

    public static ChartPlugin getChartPlugin(Engine engine) {
        return (ChartPlugin) engine.getPluginProperty(PLUGIN_NAME, "This");
    }

    public static Element createChartElement(Engine engine) {
        ChartPlugin plugin = getChartPlugin(engine);
        return engine.createElement(plugin.charts.getId());
    }

    public static Attribute getChartInSetX(Engine engine) {
        return getChartPlugin(engine).chartInSetX;
    }

    public static Attribute getChartInSetY(Engine engine) {
        return getChartPlugin(engine).chartInSetY;
    }

    public static Attribute getChartInSetWidth(Engine engine) {
        return getChartPlugin(engine).chartInSetWidth;
    }

    public static Attribute getChartInSetHeight(Engine engine) {
        return getChartPlugin(engine).chartInSetHeight;
    }

    public static Qualifier getChartLinks(Engine engine) {
        return getChartPlugin(engine).chartLinks;
    }

    public static Attribute getChart(Engine engine) {
        return getChartPlugin(engine).chart;
    }

    public static Attribute getChartSet(Engine engine) {
        return getChartPlugin(engine).chartSet;
    }

    public static List<Element> getChartLinks(Engine engine, Element chartSet) {
        ChartPlugin plugin = getChartPlugin(engine);
        return engine.findElements(plugin.chartLinks.getId(), plugin.chartSet,
                chartSet.getId());
    }

    public static Element createChartLink(Engine engine) {
        ChartPlugin plugin = getChartPlugin(engine);
        Element element = engine.createElement(plugin.chartLinks.getId());
        return element;
    }

    public static Element getChartLink(Engine engine, Element chartSet,
                                       Element chart) {
        ChartPlugin plugin = getChartPlugin(engine);
        Long id = chart.getId();
        for (Element element : engine.findElements(plugin.chartLinks.getId(),
                plugin.chartSet, chartSet.getId())) {
            if (id.equals(engine.getAttribute(element, plugin.chart))) {
                return element;
            }
        }
        return null;
    }

    public static Element addChartLink(Engine engine, Element chartSet,
                                       Element chart, double x, double y, double width, double height) {
        Element element = createChartLink(engine);
        engine.setAttribute(element, getChartInSetX(engine), x);
        engine.setAttribute(element, getChartInSetY(engine), y);
        engine.setAttribute(element, getChartInSetWidth(engine), width);
        engine.setAttribute(element, getChartInSetHeight(engine), height);
        engine.setAttribute(element, getChart(engine), chart.getId());
        engine.setAttribute(element, getChartSet(engine), chartSet.getId());

        return element;
    }

    public static void setChartLinkLocation(Engine engine, Element link,
                                            double x, double y) {
        engine.setAttribute(link, getChartInSetX(engine), x);
        engine.setAttribute(link, getChartInSetY(engine), y);

    }

    public static void setChartLinkSize(Engine engine, Element link,
                                        double width, double height) {
        engine.setAttribute(link, getChartInSetWidth(engine), width);
        engine.setAttribute(link, getChartInSetHeight(engine), height);
    }

    public static List<ChartBounds> getChartBounds(Engine engine,
                                                   Element chartSet) {
        ChartPlugin plugin = getChartPlugin(engine);
        List<ChartBounds> chartBounds = new ArrayList<ChartBounds>();
        for (Element element : getChartLinks(engine, chartSet)) {
            ChartBounds bounds = new ChartBounds(getDouble(engine, element,
                    plugin.chartInSetX), getDouble(engine, element,
                    plugin.chartInSetY), getDouble(engine, element,
                    plugin.chartInSetWidth), getDouble(engine, element,
                    plugin.chartInSetHeight), engine.getElement((Long) engine
                    .getAttribute(element, plugin.chart)), element);
            chartBounds.add(bounds);
        }
        return chartBounds;
    }

    private static double getDouble(Engine engine, Element element,
                                    Attribute attribute) {
        return (Double) engine.getAttribute(element, attribute);
    }

    public static void deleteChartSet(Engine engine, Element chartSet) {
        for (Element link : getChartLinks(engine, chartSet))
            engine.deleteElement(link.getId());
        engine.deleteElement(chartSet.getId());
    }

    public static void deleteChart(Engine engine, Element chart) {
        ChartPlugin plugin = getChartPlugin(engine);
        for (Element link : engine.findElements(plugin.chartLinks.getId(),
                plugin.chart, chart.getId()))
            engine.deleteElement(link.getId());
        engine.deleteElement(chart.getId());
    }
}

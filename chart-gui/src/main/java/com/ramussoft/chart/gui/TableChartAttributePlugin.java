package com.ramussoft.chart.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.table.TableCellEditor;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import com.ramussoft.chart.ChartDataFramework;
import com.ramussoft.chart.ChartDataPlugin;
import com.ramussoft.chart.ChartSource;
import com.ramussoft.chart.ElementSource;
import com.ramussoft.chart.FilterSource;
import com.ramussoft.chart.QualifierSource;
import com.ramussoft.chart.core.ChartPlugin;
import com.ramussoft.chart.core.TableChartPersistent;
import com.ramussoft.common.AccessRules;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.Qualifier;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.AbstractAttributeEditor;
import com.ramussoft.gui.common.AbstractAttributePlugin;
import com.ramussoft.gui.common.AttributeEditor;
import com.ramussoft.gui.common.AttributePreferenciesEditor;
import com.ramussoft.gui.qualifier.table.RowRootCreater;
import com.ramussoft.gui.qualifier.table.RowTreeTableComponent;
import com.ramussoft.gui.qualifier.table.SelectType;

public class TableChartAttributePlugin extends AbstractAttributePlugin {

    @Override
    public TableCellEditor getTableCellEditor(Engine engine, AccessRules rules,
                                              Attribute attribute) {
        return null;
    }

    @Override
    public AttributeEditor getAttributeEditor(final Engine engine,
                                              final AccessRules rules, final Element element,
                                              final Attribute attribute, AttributeEditor oldAttributeEditor) {
        return new AbstractAttributeEditor() {

            @Override
            public Object setValue(Object value) {
                return null;
            }

            @Override
            public Object getValue() {
                return null;
            }

            @Override
            public JComponent getComponent() {
                ChartDataFramework chartDataFramework = ChartDataFramework
                        .getChartDataFramework(engine);
                ChartSource source = new ChartSource(engine) {
                    @Override
                    public List<Element> getElements() {

                        List<Element> res = new ArrayList<Element>();
                        for (QualifierSource source : qualifierSources) {
                            if (QualifierSource.ELEMENTS_LOAD_TYPE_ALL
                                    .equals(source.getElementsLoadType())) {
                                FilterSource filterSource = source
                                        .getFilterSources().get(0);
                                Qualifier qualifier = source.getQualifier();
                                if (StandardAttributesPlugin
                                        .isTableQualifier(qualifier)
                                        && filterSource
                                        .getAttribute()
                                        .equals(StandardAttributesPlugin
                                                .getTableElementIdAttribute(engine))) {
                                    res.addAll(StandardAttributesPlugin
                                            .getOrderedTableElements(
                                                    engine,
                                                    StandardAttributesPlugin
                                                            .getAttributeForTable(
                                                                    engine,
                                                                    qualifier),
                                                    element));
                                } else
                                    res.addAll(engine.findElements(qualifier
                                            .getId(), filterSource
                                            .getAttribute(), Long
                                            .parseLong(filterSource.getValue())));
                            } else
                                for (ElementSource elementSource : source
                                        .getElementSources())
                                    res.add(elementSource.getElement());
                        }
                        return res;
                    }
                };

                TableChartPersistent tcp = (TableChartPersistent) engine
                        .getAttribute(null, attribute);

                Element chartElement = engine.getElement(tcp
                        .getOtherElementId());

                source.load(chartElement);
                ChartDataPlugin chartDataPlugin = chartDataFramework
                        .getChartDataPlugin(source.getChartType());
                JFreeChart freeChart = chartDataPlugin.createChart(element,
                        source);
                return new ChartPanel(freeChart);
            }
        };
    }

    @Override
    public AttributeType getAttributeType() {
        return new AttributeType("Chart", "TableChart");
    }

    @Override
    public String getName() {
        return "Chart";
    }

    @Override
    public String getString(String key) {
        return ChartResourceManager.getString(key);
    }

    @Override
    public AttributePreferenciesEditor getAttributePreferenciesEditor() {
        return new AttributePreferenciesEditor() {

            private RowTreeTableComponent component;

            @Override
            public JComponent createComponent(Attribute attribute,
                                              Engine engine, AccessRules accessRules) {
                component = new RowTreeTableComponent(engine,
                        ChartPlugin.getCharts(engine), accessRules,
                        new RowRootCreater(),
                        new Attribute[]{StandardAttributesPlugin
                                .getAttributeNameAttribute(engine)}, framework);
                component.setSelectType(SelectType.RADIO);

                return component;
            }

            @Override
            public boolean canApply() {
                return component.getModel().getSelectedRows().size() > 0;
            }

            @Override
            public void apply(Attribute attribute, Engine engine,
                              AccessRules accessRules) {
                Row row = component.getModel().getSelectedRows().get(0);
                TableChartPersistent tcp = new TableChartPersistent();
                tcp.setOtherElementId(row.getElementId());
                engine.setAttribute(null, attribute, tcp);
            }
        };
    }

}

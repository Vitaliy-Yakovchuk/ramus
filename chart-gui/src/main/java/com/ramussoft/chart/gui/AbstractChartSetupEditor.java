package com.ramussoft.chart.gui;

import java.io.ByteArrayOutputStream;

import javax.swing.JComponent;

import com.ramussoft.chart.ChartSource;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.gui.common.GUIFramework;

import static com.ramussoft.chart.ChartDataFramework.getPreferencesPath;

public abstract class AbstractChartSetupEditor implements ChartSetupEditor {

    protected ChartSourceSelectPanel component;

    protected GUIFramework framework;

    protected String type;

    public AbstractChartSetupEditor(String type) {
        this.type = type;
    }

    @Override
    public void close() {
        component.close();
    }

    ;

    @Override
    public JComponent createComponent(GUIFramework framework, Element element) {
        this.framework = framework;
        ChartSource chartSource = new ChartSource(framework.getEngine());
        if (element != null) {
            Engine engine = framework.getEngine();
            chartSource.load(engine
                    .getInputStream(getPreferencesPath(element,
                            StandardAttributesPlugin
                                    .getAttributeNameAttribute(engine))));
        }
        component = new ChartSourceSelectPanel(framework, chartSource);
        return component;
    }

    @Override
    public void save(Element element) {
        component.save();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ChartSource chartSource = getChartSource();
        chartSource.setChartType(type);
        chartSource.save(stream);
        framework.getEngine().setUndoableStream(
                getPreferencesPath(element, StandardAttributesPlugin
                        .getAttributeNameAttribute(framework.getEngine())),
                stream.toByteArray());
    }

    public ChartSource getChartSource() {
        return component.getChartSource();
    }

}

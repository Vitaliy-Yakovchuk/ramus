package com.ramussoft.chart;

import org.jfree.chart.ChartFactory;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import com.ramussoft.chart.exception.ChartNotSetupedException;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;

public class PieChartDataPlugin extends AbstractChartDataPlugin implements
        ChartConstants {

    public PieChartDataPlugin(Engine engine) {
        super(engine);
    }

    @Override
    public String getType() {
        return "Pie";
    }

    @Override
    public JFreeChart createChart(Element element, ChartSource source) {
        Attribute key = source.getAttributeProperty(PIE_ATTRIBUTE_KEY);
        Attribute value = source.getAttributeProperty(PIE_ATTRIBUTE_VALUE);
        if ((key == null) || (value == null))
            throw new ChartNotSetupedException();
        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Element element2 : source.getElements()) {
            Object v1 = engine.getAttribute(element2, key);
            Object v2 = engine.getAttribute(element2, value);
            if ((v1 != null) && (v2 != null))
                dataset.setValue(toString(v1), toDouble(v2));
        }
        return ChartFactory.createPieChart(element.getName(), dataset, true,
                true, false);
    }

}

package com.ramussoft.chart;

import java.awt.Color;
import java.awt.GradientPaint;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.data.category.DefaultCategoryDataset;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.eval.EObject;

public class BarChartDataPlugin extends AbstractChartDataPlugin implements
        ChartConstants {

    public BarChartDataPlugin(Engine engine) {
        super(engine);
    }

    @Override
    public JFreeChart createChart(Element element, ChartSource source) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Attribute key = source.getAttributeProperty(BAR_ATTRIBUTE_KEY);

        List<Attribute> attributes = source
                .getPropertyAttributes(BAR_ATTRIBUTE_VALUE_PREFIX);

        for (Element e : source.getElements()) {
            Object keyValue = engine.getAttribute(e, key);
            if (keyValue != null) {
                for (Attribute attribute : attributes) {
                    Object value = engine.getAttribute(e, attribute);
                    if (value != null) {
                        double v = new EObject(value).doubleValue();
                        dataset.addValue(v, attribute.getName(), new EObject(
                                keyValue).stringValue());
                    }
                }
            }
        }

        JFreeChart chart = ChartFactory
                .createBarChart3D(
                        element.getName(),
                        source.getProperty(BAR_CATEGORY_AXIS_LABEL),
                        source.getProperty(BAR_VALUE_AXIS_LABEL),
                        dataset,
                        (BAR_ORIENTATION_HORIZONTAL.equals(source
                                .getProperty(BAR_ORIENTATION))) ? PlotOrientation.HORIZONTAL
                                : PlotOrientation.VERTICAL, true, true, true);

        GradientPaint gradientpaint0 = new GradientPaint(0.0F, 0.0F, new Color(
                0, 0, 250), 0.0F, 0.0F, new Color(136, 136, 255));

        BarRenderer3D r = (BarRenderer3D) chart.getCategoryPlot().getRenderer();
        r.setSeriesPaint(0, gradientpaint0);

        return chart;
    }

    @Override
    public String getType() {
        return "Bar";
    }

}

package com.ramussoft.chart.gui.charts;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ramussoft.chart.ChartConstants;
import com.ramussoft.chart.ChartSource;
import com.ramussoft.chart.gui.AbstractChartSetupEditor;
import com.ramussoft.chart.gui.ChartResourceManager;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.gui.attribute.AttributesSelectPanel;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.TextField;

public class BarChartSetupEditor extends AbstractChartSetupEditor implements
        ChartConstants {

    private TextField categoryAxis = new TextField();

    private TextField valueAxis = new TextField();

    private JComboBox key = new JComboBox();

    private JComboBox orientation = new JComboBox();

    private String verticalTitle = ChartResourceManager
            .getString("Orientation.vertical");

    private String horizontalTitle = ChartResourceManager
            .getString("Orientation.horizontal");

    private AttributesSelectPanel attributesSelectPanel;

    public BarChartSetupEditor(String type) {
        super(type);
    }

    @Override
    public JComponent createComponent(GUIFramework framework, Element element) {
        JComponent component = super.createComponent(framework, element);

        initAttributes(framework.getEngine());

        ChartSource source = getChartSource();

        key.setSelectedItem(source.getAttributeProperty(BAR_ATTRIBUTE_KEY));

        double[][] size = {
                {5, TableLayout.MINIMUM, TableLayout.FILL,
                        TableLayout.MINIMUM, TableLayout.FILL, 5},
                {5, TableLayout.FILL, TableLayout.FILL, TableLayout.FILL, 5}};

        TableLayout layout = new TableLayout(size);

        layout.setHGap(5);
        layout.setVGap(5);

        JPanel panel = new JPanel(layout);

        panel.add(new JLabel(ChartResourceManager
                .getString("Label.categoryAxis")), "1,1");
        panel.add(categoryAxis, "2,1");
        panel.add(
                new JLabel(ChartResourceManager.getString("Label.valueAxis")),
                "3,1");
        panel.add(valueAxis, "4,1");

        panel.add(new JLabel(ChartResourceManager.getString("Chart.key")),
                "1,2");
        panel.add(key, "2,2");

        panel.add(new JLabel(ChartResourceManager.getString("Orientation")),
                "3,2");
        panel.add(orientation, "4,2");

        panel.add(new JLabel(ChartResourceManager.getString("Chart.values")),
                "1,3,3,3");

        attributesSelectPanel = new AttributesSelectPanel(framework, source
                .getPropertyAttributes(BAR_ATTRIBUTE_VALUE_PREFIX));

        attributesSelectPanel.setPreferredSize(new Dimension(300, 150));

        JPanel panel3 = new JPanel(new BorderLayout());

        panel3.add(panel, BorderLayout.CENTER);

        panel3.add(attributesSelectPanel, BorderLayout.SOUTH);

        categoryAxis.setText(source.getProperty(BAR_CATEGORY_AXIS_LABEL));
        valueAxis.setText(source.getProperty(BAR_VALUE_AXIS_LABEL));

        String o = source.getProperty(BAR_ORIENTATION);

        if (BAR_ORIENTATION_HORIZONTAL.equals(o))
            orientation.setSelectedIndex(1);

        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.add(component, BorderLayout.CENTER);
        panel2.add(panel3, BorderLayout.SOUTH);

        return panel2;
    }

    private void initAttributes(Engine engine) {
        key.removeAllItems();
        for (Attribute attribute : engine.getAttributes())
            key.addItem(attribute);
        orientation.addItem(verticalTitle);
        orientation.addItem(horizontalTitle);
    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public void save(Element element) {
        ChartSource source = getChartSource();
        source.setAttributeProperty(BAR_ATTRIBUTE_KEY, ((Attribute) key
                .getSelectedItem()));
        source.setPropertyAttributes(BAR_ATTRIBUTE_VALUE_PREFIX,
                attributesSelectPanel.getAttributes());
        source.setProperty(BAR_CATEGORY_AXIS_LABEL, categoryAxis.getText());
        source.setProperty(BAR_VALUE_AXIS_LABEL, valueAxis.getText());
        source
                .setProperty(
                        BAR_ORIENTATION,
                        (orientation.getSelectedIndex() == 0) ? BAR_ORIENTATION_VERTICAL
                                : BAR_ORIENTATION_HORIZONTAL);
        super.save(element);
    }
}

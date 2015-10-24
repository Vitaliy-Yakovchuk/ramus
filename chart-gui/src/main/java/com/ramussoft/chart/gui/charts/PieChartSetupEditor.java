package com.ramussoft.chart.gui.charts;

import java.awt.BorderLayout;

import info.clearthought.layout.TableLayout;

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
import com.ramussoft.gui.common.GUIFramework;

public class PieChartSetupEditor extends AbstractChartSetupEditor implements
        ChartConstants {

    private JComboBox key = new JComboBox();

    private JComboBox value = new JComboBox();

    public PieChartSetupEditor(String type) {
        super(type);
    }

    @Override
    public JComponent createComponent(GUIFramework framework, Element element) {
        JComponent component = super.createComponent(framework, element);
        final ChartSource source = getChartSource();
        initAttributes(framework.getEngine());
        key.setSelectedItem(source.getAttributeProperty(PIE_ATTRIBUTE_KEY));
        value.setSelectedItem(source.getAttributeProperty(PIE_ATTRIBUTE_VALUE));

        double[][] size = {
                {5, TableLayout.MINIMUM, TableLayout.FILL,
                        TableLayout.MINIMUM, TableLayout.FILL, 5},
                {5, TableLayout.FILL, 5}};

        TableLayout layout = new TableLayout(size);

        layout.setHGap(5);
        layout.setVGap(5);

        JPanel panel = new JPanel(layout);

        panel.add(new JLabel(ChartResourceManager.getString("Chart.key")),
                "1,1");
        panel.add(key, "2,1");
        panel.add(new JLabel(ChartResourceManager.getString("Chart.value")),
                "3,1");
        panel.add(value, "4,1");

        JPanel panel2 = new JPanel(new BorderLayout());

        panel2.add(component, BorderLayout.CENTER);
        panel2.add(panel, BorderLayout.SOUTH);

        return panel2;
    }

    private void initAttributes(Engine engine) {
        key.removeAllItems();
        value.removeAllItems();
        for (Attribute attribute : engine.getAttributes()) {
            key.addItem(attribute);
            value.addItem(attribute);
        }
    }

    @Override
    public void save(Element element) {
        ChartSource source = getChartSource();
        source.setAttributeProperty(PIE_ATTRIBUTE_KEY, ((Attribute) key
                .getSelectedItem()));
        source.setAttributeProperty(PIE_ATTRIBUTE_VALUE, ((Attribute) value
                .getSelectedItem()));
        super.save(element);
    }
}

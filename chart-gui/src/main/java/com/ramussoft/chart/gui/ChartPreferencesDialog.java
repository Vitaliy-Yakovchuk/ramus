package com.ramussoft.chart.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import info.clearthought.layout.TableLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.ramussoft.chart.ChartSource;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.TextField;
import com.ramussoft.gui.common.prefrence.Options;

import static com.ramussoft.chart.ChartDataFramework.getPreferencesPath;

public class ChartPreferencesDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = 4075476934331766715L;

    private GUIFramework framework;

    private Row chart;

    private TextField name = new TextField();

    private ChartPlugin chartPlugin;

    private ChartSetupEditor editor;

    public ChartPreferencesDialog(GUIFramework framework, Row chart) {
        super(framework.getMainFrame(), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle(ChartResourceManager.getString("Action.ChartPreferences"));
        this.framework = framework;
        this.chart = chart;
        name.setText(chart.getName());

        Engine engine = framework.getEngine();
        ChartSource chartSource = new ChartSource(engine);
        chartSource.load(engine.getInputStream(getPreferencesPath(chart
                .getElement(), StandardAttributesPlugin
                .getAttributeNameAttribute(engine))));
        ChartGUIFramework chartFramework = ChartGUIFramework
                .getFramework(framework);
        chartPlugin = chartFramework.getChartPlugin(chartSource.getChartType());
        editor = chartPlugin.createChartSetupEditor();

        double[][] size = {{5, TableLayout.MINIMUM, TableLayout.FILL, 5},
                {5, TableLayout.FILL, 5}};

        TableLayout layout = new TableLayout(size);
        layout.setHGap(5);
        layout.setVGap(5);

        JPanel top = new JPanel(layout);

        top.add(new JLabel(ChartResourceManager.getString("Chart.name")),
                "1, 1");
        top.add(name, "2, 1");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(top, BorderLayout.NORTH);

        panel.add(editor.createComponent(framework, chart.getElement()),
                BorderLayout.CENTER);

        setMainPane(panel);
        setMinimumSize(new Dimension(600, 420));
        pack();
        centerDialog();
        Options.loadOptions(this);
    }

    @Override
    protected void onOk() {
        Engine engine = framework.getEngine();
        try {
            ((Journaled) engine).startUserTransaction();
            chart.setName(name.getText());
            Element element = chart.getElement();
            editor.save(element);
            ((Journaled) engine).commitUserTransaction();
        } catch (Exception e) {
            ((Journaled) engine).rollbackUserTransaction();
            e.printStackTrace();
            JOptionPane.showMessageDialog(framework.getMainFrame(), e
                    .getLocalizedMessage());
        }
        super.onOk();
    }

    public void close() {
        editor.close();
    }
}

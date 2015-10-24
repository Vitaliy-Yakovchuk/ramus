package com.ramussoft.chart.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import info.clearthought.layout.TableLayout;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.gui.common.BaseDialog;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.TextField;
import com.ramussoft.gui.common.prefrence.Options;

public class CreateChartDialog extends BaseDialog {

    /**
     *
     */
    private static final long serialVersionUID = -6775366770030655612L;

    private TextField name = new TextField();

    private JComboBox types = new JComboBox();

    private ChartGUIFramework chartFramework;

    private JPanel centerPanel = new JPanel(new BorderLayout());

    private ChartSetupEditor editor;

    private GUIFramework framework;

    private ChartsView chartsView;

    public CreateChartDialog(GUIFramework framework, ChartsView chartsView) {
        super(framework.getMainFrame(), true);

        setTitle(ChartResourceManager.getString("Action.CreateChart"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.framework = framework;
        this.chartsView = chartsView;

        chartFramework = ChartGUIFramework.getFramework(framework);
        for (ChartPlugin chartPlugin : chartFramework.getChartPlugins()) {
            types.addItem(chartPlugin);
        }

        double[][] size = {
                {5, TableLayout.MINIMUM, TableLayout.FILL,
                        TableLayout.MINIMUM, TableLayout.FILL, 5},
                {5, TableLayout.FILL, 5}};

        TableLayout layout = new TableLayout(size);
        layout.setHGap(5);
        layout.setVGap(5);

        JPanel top = new JPanel(layout);

        top.add(new JLabel(ChartResourceManager.getString("Chart.type")),
                "1, 1");
        top.add(types, "2, 1");
        top.add(new JLabel(ChartResourceManager.getString("Chart.name")),
                "3, 1");
        top.add(name, "4, 1");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(top, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        setPlugin((ChartPlugin) types.getSelectedItem());
        types.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                setPlugin((ChartPlugin) types.getSelectedItem());
            }
        });
        setMinimumSize(new Dimension(600, 420));
        setMainPane(panel);
        pack();
        centerDialog();
        Options.loadOptions(this);
    }

    private void setPlugin(ChartPlugin plugin) {
        if (editor != null)
            editor.close();
        editor = null;
        editor = plugin.createChartSetupEditor();
        centerPanel.removeAll();
        centerPanel.add(editor.createComponent(framework, null),
                BorderLayout.CENTER);
        centerPanel.revalidate();
        centerPanel.repaint();
    }

    public void close() {
        if (editor != null) {
            editor.close();
            editor = null;
        }
    }

    @Override
    protected void onOk() {
        Engine engine = framework.getEngine();
        try {
            ((Journaled) engine).startUserTransaction();

            Element element = chartsView.createChartElement(name.getText());
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
}

package com.ramussoft.chart.gui;

import java.awt.print.PrinterException;

import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javax.swing.JComponent;

import org.jfree.chart.ChartPanel;

import com.ramussoft.chart.ChartDataFramework;
import com.ramussoft.chart.ChartDataPlugin;
import com.ramussoft.chart.ChartSource;
import com.ramussoft.chart.core.ChartPlugin;
import com.ramussoft.chart.exception.ChartNotSetupedException;
import com.ramussoft.chart.gui.event.ChartSourceAdapter;
import com.ramussoft.chart.gui.event.ChartSourceEvent;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.StreamAdapter;
import com.ramussoft.common.event.StreamEvent;
import com.ramussoft.common.event.StreamListener;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.gui.StandardFilePlugin;
import com.ramussoft.gui.common.AbstractView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.TabView;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ViewTitleEvent;

public class ChartView extends AbstractView implements TabView {

    private Element element;

    private ChartSource chartSource;

    private ChartDataFramework chartDataFramework;

    private String path;

    private ElementAttributeListener diagramNameListener = new ElementAttributeListener() {

        @Override
        public void attributeChanged(AttributeEvent event) {
            if ((event.getElement().equals(element))
                    && (StandardAttributesPlugin
                    .getAttributeNameAttribute(framework.getEngine())
                    .equals(event.getAttribute()))) {
                element = event.getElement();
                String newTitle = (String) event.getNewValue();
                titleChanged(newTitle);
            }
        }
    };

    private StreamListener streamListener = new StreamAdapter() {
        @Override
        public void streamUpdated(StreamEvent event) {
            if (path.equals(event.getPath())) {
                chartSource = new ChartSource(framework.getEngine());
                chartSource.load(element);

                hook.reinit(chartSource);

                reload();
            }
        }
    };

    private ChartPanel chartPanel;

    private ChartChangeHook hook;

    public ChartView(GUIFramework framework, Element element) {
        super(framework);

        Engine engine = framework.getEngine();

        this.path = ChartDataFramework.getPreferencesPath(element,
                StandardAttributesPlugin.getAttributeNameAttribute(engine));

        this.chartDataFramework = ChartDataFramework
                .getChartDataFramework(engine);
        this.element = element;

        chartSource = new ChartSource(engine);
        chartSource.load(element);

        engine.addElementAttributeListener(ChartPlugin.getCharts(engine),
                diagramNameListener);
        engine.addStreamListener(streamListener);

        ChartGUIFramework framework2 = ChartGUIFramework
                .getFramework(framework);

        hook = framework2.getChartPlugin(chartSource.getChartType())
                .createChartChangeHook(engine, element, chartSource);
        hook.addChartSourceListener(new ChartSourceAdapter() {
            @Override
            public void sourceChanged(ChartSourceEvent event) {
                reload();
            }

            @Override
            public void chartRemoved(ChartSourceEvent event) {
                close();
            }
        });
    }

    protected void reload() {
        try {
            ChartDataPlugin plugin = chartDataFramework
                    .getChartDataPlugin(chartSource.getChartType());
            chartPanel.setChart(plugin.createChart(element));
        } catch (ChartNotSetupedException e) {
            close();
            JOptionPane.showMessageDialog(framework.getMainFrame(),
                    ChartResourceManager.getString("Error.chartNotSetuped"));
        }
    }

    @Override
    public JComponent createComponent() {
        ChartDataPlugin plugin = chartDataFramework
                .getChartDataPlugin(chartSource.getChartType());
        try {
            chartPanel = new ChartPanel(plugin.createChart(element));
            chartPanel.setPopupMenu(null);
        } catch (ChartNotSetupedException e) {
            JOptionPane.showMessageDialog(framework.getMainFrame(),
                    ChartResourceManager.getString("Error.chartNotSetuped"));
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    close();
                }
            });
            return new JPanel();
        }
        return chartPanel;
    }

    @Override
    public Action[] getActions() {
        return new Action[]{};
    }

    @Override
    public String getTitle() {
        return element.getName();
    }

    @Override
    public ActionEvent getOpenAction() {
        return new ActionEvent(ChartGUIPlugin.OPEN_CHART, element);
    }

    @Override
    public void close() {
        super.close();
        Engine engine = framework.getEngine();
        engine.removeElementAttributeListener(ChartPlugin.getCharts(engine),
                diagramNameListener);
        engine.removeStreamListener(streamListener);
        hook.close();
    }

    protected void titleChanged(String newTitle) {
        ViewTitleEvent event2 = new ViewTitleEvent(ChartView.this, newTitle);
        titleChanged(event2);
        reload();
    }

    public Element getElement() {
        return element;
    }

    @Override
    public String[] getGlobalActions() {
        return new String[]{StandardFilePlugin.ACTION_PRINT,
                StandardFilePlugin.ACTION_PAGE_SETUP,
                StandardFilePlugin.ACTION_PRINT_PREVIEW};
    }

    @Override
    public void onAction(ActionEvent event) {
        if (event.getKey().equals(StandardFilePlugin.ACTION_PAGE_SETUP))
            new ChartPrintable(chartPanel).pageSetup(framework);
        else if (event.getKey().equals(StandardFilePlugin.ACTION_PRINT)) {
            ChartPrintable printable = new ChartPrintable(chartPanel);
            try {
                printable.print(framework);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(framework.getMainFrame(), e
                        .getLocalizedMessage());
                e.printStackTrace();
            }
        } else if (event.getKey().equals(
                StandardFilePlugin.ACTION_PRINT_PREVIEW)) {
            ChartPrintable printable = new ChartPrintable(chartPanel);
            framework.printPreview(printable);
        }
    }

    public ChartPanel getChartPanel() {
        return chartPanel;
    }

    public ChartSource getChartSource() {
        return chartSource;
    }
}

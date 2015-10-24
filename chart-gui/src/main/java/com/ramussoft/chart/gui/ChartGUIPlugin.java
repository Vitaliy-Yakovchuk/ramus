package com.ramussoft.chart.gui;

import com.ramussoft.common.Element;

import com.ramussoft.gui.common.AbstractViewPlugin;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.UniqueView;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ActionListener;
import com.ramussoft.gui.common.event.TabbedEvent;
import com.ramussoft.gui.qualifier.table.TabbedTableView;

public class ChartGUIPlugin extends AbstractViewPlugin {

    public static final String OPEN_CHART = "OpenChart";

    public static final String OPEN_CHART_SET = "OpenChartSet";

    @Override
    public String getName() {
        return "Chart";
    }

    @Override
    public UniqueView[] getUniqueViews() {
        return new UniqueView[]{new ChartSetsView(framework),
                new ChartsView(framework)};
    }

    @Override
    public String getString(String key) {
        return ChartResourceManager.getString(key);
    }

    @Override
    public void setFramework(GUIFramework aFramework) {
        super.setFramework(aFramework);

        framework.addActionListener(OPEN_CHART, new ActionListener() {

            @Override
            public void onAction(ActionEvent event) {
                if (framework.openView(event))
                    return;
                Element element = (Element) event.getValue();
                ChartView view = new ChartView(framework, element) {
                    @Override
                    public void close() {
                        super.close();
                        TabbedEvent tEvent = new TabbedEvent("TabbedTableView",
                                this);
                        tabRemoved(tEvent);
                    }
                };
                TabbedEvent tEvent = new TabbedEvent(
                        TabbedTableView.MAIN_TABBED_VIEW, view);
                tabCreated(tEvent);
            }
        });

        framework.addActionListener(OPEN_CHART_SET, new ActionListener() {

            @Override
            public void onAction(ActionEvent event) {
                if (framework.openView(event))
                    return;
                Element element = (Element) event.getValue();
                ChartSetView view = new ChartSetView(framework, element) {
                    @Override
                    public void close() {
                        super.close();
                        TabbedEvent tEvent = new TabbedEvent("TabbedTableView",
                                this);
                        tabRemoved(tEvent);
                    }
                };
                TabbedEvent tEvent = new TabbedEvent(
                        TabbedTableView.MAIN_TABBED_VIEW, view);
                tabCreated(tEvent);
            }
        });
    }

}

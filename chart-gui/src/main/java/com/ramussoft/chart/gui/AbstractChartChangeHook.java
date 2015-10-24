package com.ramussoft.chart.gui;

import java.util.List;

import javax.swing.event.EventListenerList;

import com.ramussoft.chart.ChartSource;
import com.ramussoft.chart.core.ChartPlugin;
import com.ramussoft.chart.gui.event.ChartSourceEvent;
import com.ramussoft.chart.gui.event.ChartSourceListener;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementListener;

public abstract class AbstractChartChangeHook implements ChartChangeHook {

    protected EventListenerList list = new EventListenerList();

    protected Engine engine;

    protected List<Element> elements;

    protected Element element;

    private ElementAttributeListener listener = new ElementAttributeListener() {

        @Override
        public void attributeChanged(AttributeEvent event) {
            if (elements.indexOf(event.getElement()) >= 0) {
                ChartSourceEvent event2 = new ChartSourceEvent();
                for (ChartSourceListener listener : getChartSourceListeners())
                    listener.sourceChanged(event2);
            }
        }
    };

    private ElementListener elementListener = new ElementAdapter() {
        public void elementDeleted(com.ramussoft.common.event.ElementEvent event) {
            if (element.equals(event.getOldElement())) {
                ChartSourceEvent event2 = new ChartSourceEvent();
                for (ChartSourceListener listener : getChartSourceListeners())
                    listener.chartRemoved(event2);
            }
        }

        ;
    };

    public AbstractChartChangeHook(Engine engine, Element element,
                                   ChartSource chartSource) {
        this.engine = engine;
        this.elements = chartSource.getElements();
        this.element = element;
        engine.addElementAttributeListener(null, listener);
        engine.addElementListener(ChartPlugin.getCharts(engine),
                elementListener);
    }

    @Override
    public void addChartSourceListener(ChartSourceListener listener) {
        list.add(ChartSourceListener.class, listener);
    }

    @Override
    public void close() {
        engine.removeElementAttributeListener(null, listener);
        engine.removeElementListener(ChartPlugin.getCharts(engine),
                elementListener);
    }

    @Override
    public ChartSourceListener[] getChartSourceListeners() {
        return list.getListeners(ChartSourceListener.class);
    }

    @Override
    public void removeChartSourceListener(ChartSourceListener listener) {
        list.remove(ChartSourceListener.class, listener);
    }

    @Override
    public void reinit(ChartSource chartSource) {
        elements = chartSource.getElements();
    }
}

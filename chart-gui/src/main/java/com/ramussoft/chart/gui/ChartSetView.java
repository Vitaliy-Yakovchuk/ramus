package com.ramussoft.chart.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.print.PrinterException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.ramussoft.chart.core.ChartBounds;
import com.ramussoft.chart.core.ChartPlugin;
import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.common.event.AttributeEvent;
import com.ramussoft.common.event.ElementAdapter;
import com.ramussoft.common.event.ElementAttributeListener;
import com.ramussoft.common.event.ElementListener;
import com.ramussoft.common.journal.Journaled;
import com.ramussoft.core.attribute.standard.StandardAttributesPlugin;
import com.ramussoft.database.common.Row;
import com.ramussoft.gui.StandardFilePlugin;
import com.ramussoft.gui.common.AbstractView;
import com.ramussoft.gui.common.GUIFramework;
import com.ramussoft.gui.common.TabView;
import com.ramussoft.gui.common.event.ActionEvent;
import com.ramussoft.gui.common.event.ViewTitleEvent;
import com.ramussoft.gui.qualifier.table.RowTreeTable;
import com.ramussoft.gui.qualifier.table.Rows;

public class ChartSetView extends AbstractView implements TabView {

    private Element element;

    private Engine engine;

    private ElementAttributeListener listener = new ElementAttributeListener() {

        @Override
        public void attributeChanged(AttributeEvent event) {
            if (event.getElement().equals(element)) {
                element = event.getElement();
                if (event.getAttribute().equals(
                        StandardAttributesPlugin
                                .getAttributeNameAttribute(engine))) {
                    ViewTitleEvent event2 = new ViewTitleEvent(
                            ChartSetView.this, (String) event.getNewValue());
                    titleChanged(event2);
                }
            }
        }
    };

    private ElementAttributeListener boundsListener = new ElementAttributeListener() {

        @Override
        public void attributeChanged(AttributeEvent event) {
            if (event.getNewValue() == null)
                return;
            for (ChartHolder holder : holders) {
                if (holder.link.equals(event.getElement())) {
                    Attribute attribute = event.getAttribute();
                    if (attribute.equals(ChartPlugin.getChartInSetX(engine))) {
                        holder.setX((Double) event.getNewValue());
                    } else if (attribute.equals(ChartPlugin
                            .getChartInSetY(engine))) {
                        holder.setY((Double) event.getNewValue());
                    } else if (attribute.equals(ChartPlugin
                            .getChartInSetWidth(engine))) {
                        holder.setWidth((Double) event.getNewValue());
                    } else if (attribute.equals(ChartPlugin
                            .getChartInSetHeight(engine))) {
                        holder.setHeight((Double) event.getNewValue());
                    }
                    return;
                }
            }

            final Element link = event.getElement();

            Long chartSetId = (Long) engine.getAttribute(link, ChartPlugin
                    .getChartSet(engine));
            if (chartSetId == null)
                return;

            if (chartSetId.longValue() != element.getId())
                return;

            Long chartId = (Long) engine.getAttribute(link, ChartPlugin
                    .getChart(engine));
            if (chartId == null)
                return;
            Element chart = engine.getElement(chartId);
            if (chart == null)
                return;
            Double x = (Double) engine.getAttribute(link, ChartPlugin
                    .getChartInSetX(engine));
            if (x == null)
                return;
            Double y = (Double) engine.getAttribute(link, ChartPlugin
                    .getChartInSetY(engine));
            if (y == null)
                return;
            Double width = (Double) engine.getAttribute(link, ChartPlugin
                    .getChartInSetWidth(engine));
            if (width == null)
                return;
            Double height = (Double) engine.getAttribute(link, ChartPlugin
                    .getChartInSetHeight(engine));
            if (height == null)
                return;
            final JInternalFrame frame = new JInternalFrame(chart.getName(),
                    true, true, true, true);
            final ChartView chartView = new ChartView(framework, chart) {
                protected void titleChanged(String newTitle) {
                    frame.setTitle(newTitle);
                    reload();
                }

                ;
            };
            frame.setSize(width.intValue(), height.intValue());

            Point position = new Point(x.intValue(), y.intValue());
            frame.setLocation(position);
            frame.setContentPane(chartView.createComponent());

            desktop.add(frame);
            frame.setVisible(true);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    ChartHolder holder = new ChartHolder(chartView, frame, link);
                    holders.add(holder);
                }
            });
        }
    };

    private ElementListener linksListener = new ElementAdapter() {

        public void elementDeleted(com.ramussoft.common.event.ElementEvent event) {
            ChartHolder holderToRemove = null;
            for (ChartHolder holder : holders)
                if (holder.link.equals(event.getOldElement())) {
                    holderToRemove = holder;
                    break;
                }
            if (holderToRemove != null)
                holderToRemove.close();
        }

        ;

    };

    private ElementListener chartSetElementListener = new ElementAdapter() {

        public void elementDeleted(com.ramussoft.common.event.ElementEvent event) {
            if (event.getOldElement().equals(element))
                close();
        }

        ;

    };

    private List<ChartHolder> holders = new ArrayList<ChartHolder>();

    private JDesktopPane desktop;

    public ChartSetView(GUIFramework framework, Element element) {
        super(framework);
        this.element = element;
        engine = framework.getEngine();
        engine.addElementAttributeListener(ChartPlugin.getChartSets(engine),
                listener);
        engine.addElementAttributeListener(ChartPlugin.getChartLinks(engine),
                boundsListener);
        engine.addElementListener(ChartPlugin.getChartLinks(engine),
                linksListener);
        engine.addElementListener(ChartPlugin.getChartSets(engine),
                chartSetElementListener);
    }

    @Override
    public String getTitle() {
        return element.getName();
    }

    @Override
    public JComponent createComponent() {
        final JLabel label = new JLabel(ChartResourceManager
                .getString("Message.MoveChartToChartSet"));
        desktop = new JDesktopPane();

        desktop.setTransferHandler(new TransferHandler("") {
            /**
             *
             */
            private static final long serialVersionUID = 4967256166603971141L;

            @Override
            public boolean canImport(JComponent comp,
                                     DataFlavor[] transferFlavors) {
                for (DataFlavor flavor : transferFlavors) {
                    if (flavor.equals(RowTreeTable.rowsListFlavor)) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean importData(JComponent comp, Transferable t) {
                try {
                    Rows rows = (Rows) t
                            .getTransferData(RowTreeTable.rowsListFlavor);
                    if (rows.size() > 0) {
                        ((Journaled) engine).startUserTransaction();
                        for (Row row : rows) {

                            Point position = comp.getMousePosition();
                            if (position == null)
                                position = new Point(10, 10);

                            ChartPlugin.addChartLink(engine, element, row
                                    .getElement(), position.getX(), position
                                    .getY(), 340, 300);
                        }
                    }
                    ((Journaled) engine).commitUserTransaction();
                    return rows.size() > 0;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        label.setBackground(desktop.getBackground());
        desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        List<ChartBounds> bounds = ChartPlugin.getChartBounds(engine, element);
        for (ChartBounds cb : bounds) {
            final JInternalFrame frame = new JInternalFrame(cb.getChart()
                    .getName(), true, true, true, true);
            final ChartView chartView = new ChartView(framework, cb.getChart()) {
                protected void titleChanged(String newTitle) {
                    frame.setTitle(newTitle);
                    reload();
                }

                ;
            };

            final ChartBounds b = cb;
            frame.setSize((int) cb.getWidth(), (int) cb.getHeight());

            frame.setLocation((int) cb.getX(), (int) cb.getY());
            frame.setContentPane(chartView.createComponent());

            desktop.add(frame);
            frame.setVisible(true);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    ChartHolder holder = new ChartHolder(chartView, frame, b
                            .getLink());
                    holders.add(holder);
                }
            });
        }

        return desktop;
    }

    @Override
    public Action[] getActions() {
        return new Action[]{};
    }

    @Override
    public ActionEvent getOpenAction() {
        return new ActionEvent(ChartGUIPlugin.OPEN_CHART_SET, element);
    }

    @Override
    public void close() {
        super.close();
        engine.removeElementAttributeListener(ChartPlugin.getChartSets(engine),
                listener);
        engine.removeElementAttributeListener(
                ChartPlugin.getChartLinks(engine), boundsListener);
        engine.removeElementListener(ChartPlugin.getChartLinks(engine),
                linksListener);
        engine.removeElementListener(ChartPlugin.getChartSets(engine),
                chartSetElementListener);
        for (ChartHolder chartHolder : holders) {
            chartHolder.chartView.close();
        }
    }

    public class ChartHolder {

        ChartView chartView;

        JInternalFrame frame;

        Element link;

        ComponentAdapter componentListener;

        boolean blocked = false;

        ChartHolder(ChartView aChartView, JInternalFrame aFrame, Element aLink) {
            this.chartView = aChartView;
            this.frame = aFrame;
            this.link = aLink;
            frame.addInternalFrameListener(new InternalFrameAdapter() {
                @Override
                public void internalFrameClosed(InternalFrameEvent e) {
                    close();
                    ((Journaled) engine).startUserTransaction();
                    engine.deleteElement(link.getId());
                    ((Journaled) engine).commitUserTransaction();
                }
            });

            componentListener = new ComponentAdapter() {
                @Override
                public void componentMoved(ComponentEvent e) {
                    blocked = true;
                    ((Journaled) engine).startUserTransaction();
                    Point point = frame.getLocation();
                    ChartPlugin.setChartLinkLocation(engine, link,
                            point.getX(), point.getY());
                    ((Journaled) engine).commitUserTransaction();
                    blocked = false;
                }

                @Override
                public void componentResized(ComponentEvent e) {
                    blocked = true;
                    ((Journaled) engine).startUserTransaction();
                    Dimension size = frame.getSize();
                    ChartPlugin.setChartLinkSize(engine, link, size.getWidth(),
                            size.getHeight());
                    ((Journaled) engine).commitUserTransaction();
                    blocked = false;
                }
            };
            frame.addComponentListener(componentListener);
        }

        private void close() {
            chartView.close();
            holders.remove(this);
            desktop.remove(frame);
            desktop.repaint();
        }

        void setX(double x) {
            if (blocked)
                return;
            frame.removeComponentListener(componentListener);
            frame.setLocation((int) x, frame.getLocation().y);
            frame.addComponentListener(componentListener);
        }

        void setY(double y) {
            if (blocked)
                return;
            frame.removeComponentListener(componentListener);
            frame.setLocation(frame.getLocation().x, (int) y);
            frame.addComponentListener(componentListener);
        }

        void setWidth(double width) {
            if (blocked)
                return;
            frame.removeComponentListener(componentListener);
            frame.setSize((int) width, frame.getSize().height);
            frame.addComponentListener(componentListener);
        }

        void setHeight(double height) {
            if (blocked)
                return;
            frame.removeComponentListener(componentListener);
            frame.setSize(frame.getSize().width, (int) height);
            frame.addComponentListener(componentListener);
        }

        public JInternalFrame getFrame() {
            return frame;
        }

        public ChartView getChartView() {
            return chartView;
        }
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
            new ChartSetPrintable(holders).pageSetup(framework);
        else if (event.getKey().equals(StandardFilePlugin.ACTION_PRINT)) {
            ChartSetPrintable printable = new ChartSetPrintable(holders);
            try {
                printable.print(framework);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(framework.getMainFrame(), e
                        .getLocalizedMessage());
                e.printStackTrace();
            }
        } else if (event.getKey().equals(
                StandardFilePlugin.ACTION_PRINT_PREVIEW)) {
            ChartSetPrintable printable = new ChartSetPrintable(holders);
            framework.printPreview(printable);
        }
    }

}

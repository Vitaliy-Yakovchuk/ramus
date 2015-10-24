package com.ramussoft.chart.gui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.util.List;

import org.jfree.chart.JFreeChart;

import com.ramussoft.chart.gui.ChartSetView.ChartHolder;
import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.common.print.AbstractRamusPrintable;

public class ChartSetPrintable extends AbstractRamusPrintable {

    private final List<ChartHolder> holders;

    public ChartSetPrintable(List<ChartHolder> holders) {
        this.holders = holders;
    }

    @Override
    public int getPageCount() {
        return 1;
    }

    @Override
    public void print(Graphics2D g, int pageIndex) throws PrinterException {
        if (holders.size() > 0) {
            ChartHolder sHolder = holders.get(0);
            double minX = sHolder.getFrame().getLocation().getX();
            double maxX = minX + sHolder.getFrame().getSize().getWidth();
            double minY = sHolder.getFrame().getLocation().getY();
            double maxY = minY + sHolder.getFrame().getSize().getHeight();
            for (int i = 1; i < holders.size(); i++) {
                ChartHolder holder = holders.get(i);
                Point p = holder.getFrame().getLocation();
                Dimension d = holder.getFrame().getSize();
                if (minX > p.getX())
                    minX = p.getX();
                if (minY > p.getY())
                    minY = p.getY();
                if (maxX < p.getX() + d.getWidth())
                    maxX = p.getX() + d.getWidth();
                if (maxY < p.getY() + d.getHeight())
                    maxY = p.getY() + d.getHeight();
            }

            PageFormat pf = getPageFormat(getPageFormat(), pageIndex);
            double x = pf.getImageableX();
            double y = pf.getImageableY();
            double w = pf.getImageableWidth();
            double h = pf.getImageableHeight();

            double xZoom = w / (maxX - minX);
            double yZoom = h / (maxY - minY);

            double zoom = Math.min(xZoom, yZoom);
            zoom *= 2;
            // x += (w - (maxX - minX) * zoom) / 2d;

            // y += (h - (maxY - minY) * zoom) / 2d;

            g.translate(x, y);
            g.scale(0.5, 0.5);

            for (ChartHolder holder : holders) {
                Point p = holder.getFrame().getLocation();
                Dimension d = holder.getFrame().getSize();
                JFreeChart chart = holder.getChartView().getChartPanel()
                        .getChart();
                chart.draw(g, new Rectangle2D.Double(p.getX() * zoom - minX
                        * zoom, p.getY() * zoom - minY * zoom, d.getWidth()
                        * zoom, d.getHeight() * zoom));
            }
            g.scale(2d, 2d);
            g.translate(-x, -y);
        }

    }

    @Override
    public PageFormat getPageFormat() {
        if (pageFormat == null) {
            pageFormat = Options.getPageFormat("chart-set-page-format.xml",
                    new PageFormat());
        }
        return pageFormat;
    }

    @Override
    public void setPageFormat(PageFormat pageFormat) {
        super.setPageFormat(pageFormat);
        Options.setPageFormat("chart-set-page-format.xml", pageFormat);
    }
}

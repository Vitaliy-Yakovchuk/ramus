package com.ramussoft.chart.gui;

import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;

import org.jfree.chart.ChartPanel;

import com.ramussoft.gui.common.prefrence.Options;
import com.ramussoft.gui.common.print.AbstractRamusPrintable;

public class ChartPrintable extends AbstractRamusPrintable {

    private final ChartPanel chartPanel;

    public ChartPrintable(ChartPanel chartPanel) {
        this.chartPanel = chartPanel;
    }

    @Override
    public int getPageCount() {
        return 1;
    }

    @Override
    public void print(Graphics2D g, int pageIndex) throws PrinterException {
        chartPanel.print(g, getPageFormat(getPageFormat(), pageIndex),
                pageIndex);
    }

    @Override
    public PageFormat getPageFormat() {
        if (pageFormat == null) {
            pageFormat = Options.getPageFormat("chart-page-format.xml",
                    new PageFormat());
        }
        return pageFormat;
    }

    @Override
    public void setPageFormat(PageFormat pageFormat) {
        super.setPageFormat(pageFormat);
        Options.setPageFormat("chart-page-format.xml", pageFormat);
    }
}

package com.ramussoft.gui.common.print;

import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.print.PrintService;
import javax.swing.Action;

import com.ramussoft.gui.common.GUIFramework;

public abstract class AbstractRamusPrintable implements RamusPrintable {

    protected PageFormat pageFormat;

    protected PrintService printService;

    @Override
    public PageFormat getPageFormat(PageFormat pageFormat, int pageIndex) {
        return getPageFormat();
    }

    @Override
    public PageFormat getPageFormat() {
        if (pageFormat == null) {
            pageFormat = new PageFormat();
        }
        return pageFormat;
    }

    public void setPageFormat(PageFormat pageFormat) {
        this.pageFormat = pageFormat;
    }

    @Override
    public String getJobName() {
        return "Ramus printing";
    }

    @Override
    public void pageSetup(GUIFramework framework) {
        PrinterJob job = framework.getPrinterJob(getJobKey());
        setPageFormat(job.pageDialog(getPageFormat()));
    }

    protected String getJobKey() {
        return "default";
    }

    @Override
    public void print(GUIFramework framework) throws PrinterException {
        final PrinterJob pj = framework.getPrinterJob(getJobKey());
        final Printable printable = createPrintable();
        pj.setPrintable(printable, getPageFormat());

        if (pj.printDialog()) {
            pj.setJobName(getJobName());
            pj.print();
            setPageFormat(getPageFormat());
        }
    }

    public Printable createPrintable() {
        return new PrintableImpl(this);
    }

    @Override
    public PrintService getPrintService() {
        if (printService == null) {
            PrintService[] lookupPrintServices = PrinterJob
                    .lookupPrintServices();
            if (lookupPrintServices.length > 0)
                printService = lookupPrintServices[0];
        }
        return printService;
    }

    @Override
    public void setPrintService(PrintService printService) {
        this.printService = printService;
    }

    @Override
    public Action[] getActions(GUIFramework framework) {
        return new Action[]{};
    }

}

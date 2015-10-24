package com.ramussoft.gui.common.print;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

public class PrintableImpl implements Printable {

    private RamusPrintable ramusPrintable;

    public PrintableImpl(RamusPrintable ramusPrintable) {
        this.ramusPrintable = ramusPrintable;
    }

    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
            throws PrinterException {
        ramusPrintable.setPageFormat(pageFormat);
        if (ramusPrintable.getPageCount() > pageIndex) {
            ramusPrintable.print((Graphics2D) graphics, pageIndex);
            return PAGE_EXISTS;
        }
        return NO_SUCH_PAGE;
    }

}

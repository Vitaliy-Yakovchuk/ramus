package com.ramussoft.pb.print;

import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

/**
 * КласЮ який друкує лише певний набір сторінок.
 *
 * @author ZDD
 */

public class IndexPrintable implements Printable {

    private final int[] indexes;

    private final Printable printable;

    public IndexPrintable(final Printable printable, final int[] indexes) {
        super();
        this.indexes = indexes;
        this.printable = printable;
    }

    public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex)
            throws PrinterException {
        if (pageIndex >= indexes.length)
            return NO_SUCH_PAGE;
        printable.print(graphics, pageFormat, indexes[pageIndex]);
        return PAGE_EXISTS;
    }

}

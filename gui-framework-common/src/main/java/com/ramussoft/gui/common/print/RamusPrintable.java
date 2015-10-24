package com.ramussoft.gui.common.print;

import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;

import javax.print.PrintService;
import javax.swing.Action;

import com.ramussoft.gui.common.GUIFramework;

public interface RamusPrintable {

    int getPageCount();

    PageFormat getPageFormat(PageFormat pageFormat, int pageIndex);

    void print(Graphics2D g, int pageIndex) throws PrinterException;

    String getJobName();

    void pageSetup(GUIFramework framework);

    void print(GUIFramework framework) throws PrinterException;

    PageFormat getPageFormat();

    PrintService getPrintService();

    void setPageFormat(PageFormat pageFormat);

    void setPrintService(PrintService printService);

    Action[] getActions(GUIFramework framework);
}

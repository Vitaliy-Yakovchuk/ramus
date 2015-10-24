package com.ramussoft.pb.print.web;

import java.io.IOException;
import java.text.NumberFormat;

import com.ramussoft.localefix.DecimalFormatWithFix;
import com.ramussoft.web.HTTPParser.PrintStream;

public class NumberAttributeViewer extends SimpleAttributeViewer {

    private NumberFormat format = new DecimalFormatWithFix();

    @Override
    protected void printObject(PrintStream printStream, Object object)
            throws IOException {
        super.printObject(printStream, format.format(object));
    }
}

package com.ramussoft.pb.print.web;

import java.io.IOException;
import java.text.DateFormat;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.HTTPParser.PrintStream;

public class DateAttributeViewer extends AbstractAttributeViewer {

    private DateFormat format = DateFormat.getDateInstance();

    @Override
    public void printAttribute(PrintStream printStream, DataPlugin dataPlugin,
                               Element element, Attribute attribute, HTTPParser parser,
                               AttributeViewerCallback callback) throws IOException {
        Object object = dataPlugin.getEngine().getAttribute(element, attribute);
        if (object != null) {
            callback.beforePrint(printStream);
            printStream.print(format.format(object));
            callback.afterPrint(printStream);
        }

    }

}

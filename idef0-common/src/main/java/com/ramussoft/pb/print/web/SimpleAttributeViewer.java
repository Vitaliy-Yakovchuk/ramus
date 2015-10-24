package com.ramussoft.pb.print.web;

import java.io.IOException;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.common.Engine;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.HTTPParser.PrintStream;

public class SimpleAttributeViewer extends AbstractAttributeViewer {

    @Override
    public void printAttribute(PrintStream printStream, DataPlugin dataPlugin,
                               Element element, Attribute attribute, HTTPParser parser,
                               AttributeViewerCallback callback) throws IOException {
        Engine engine = dataPlugin.getEngine();
        Object object = engine.getAttribute(element, attribute);
        if (object != null) {
            callback.beforePrint(printStream);
            printObject(printStream, object);
            callback.afterPrint(printStream);
        }
    }

    protected void printObject(PrintStream printStream, Object object)
            throws IOException {
        printStream.println(object.toString());
    }

}

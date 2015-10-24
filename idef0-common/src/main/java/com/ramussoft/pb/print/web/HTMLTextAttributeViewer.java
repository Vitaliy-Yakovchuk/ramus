package com.ramussoft.pb.print.web;

import java.io.IOException;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.core.attribute.simple.HTMLPage;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.HTTPParser.PrintStream;

public class HTMLTextAttributeViewer extends AbstractAttributeViewer {

    @Override
    public void printAttribute(PrintStream printStream, DataPlugin dataPlugin,
                               Element element, Attribute attribute, HTTPParser parser,
                               AttributeViewerCallback callback) throws IOException {
        HTMLPage page = (HTMLPage) dataPlugin.getEngine().getAttribute(element,
                attribute);
        if (page != null) {
            printStream.print(page);
        }

    }

}

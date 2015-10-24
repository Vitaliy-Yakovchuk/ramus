package com.ramussoft.pb.print.web;

import java.io.IOException;

import com.ramussoft.common.Element;
import com.ramussoft.pb.print.AttributeViewer;
import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.HTTPParser.PrintStream;

public abstract class AbstractAttributeViewer implements AttributeViewer {

    protected void printLinkToElement(Element element, Object object,
                                      HTTPParser parser, PrintStream printStream) throws IOException {
        if (object != null) {
            parser.printStartATeg("rows/index.html?id=" + element.getId());
            printStream.print(object.toString());
            parser.printEndATeg();
        }
    }

    protected void printLinkToElement(Element element, HTTPParser parser,
                                      PrintStream printStream) throws IOException {
        printLinkToElement(element, element.getName(), parser, printStream);
    }

}

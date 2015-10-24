package com.ramussoft.pb.print.web;

import java.io.IOException;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.Element;
import com.ramussoft.core.attribute.simple.FilePersistent;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.HTTPParser.PrintStream;

public class FileAttributeViewer extends AbstractAttributeViewer {

    @Override
    public void printAttribute(PrintStream printStream, DataPlugin dataPlugin,
                               Element element, Attribute attribute, HTTPParser parser,
                               AttributeViewerCallback callback) throws IOException {
        FilePersistent value = (FilePersistent) dataPlugin.getEngine()
                .getAttribute(element, attribute);
        if (value != null) {
            callback.beforePrint(printStream);

            String href = "files/file_getter.html?id=" + element.getId()
                    + "&attr=" + attribute.getId();
            parser.printStartATeg(href);

            printStream.print(value.getName());
            parser.printEndATeg();

            if (isImage(value.getName())) {
                printStream.println("<br>");
                parser.printStartATeg(href + "&prev=true");
                printStream.println("<img class=\"faimage\" src=\"" + parser.getFromLink() + href
                        + "\">");
                parser.printEndATeg();
            }

            callback.afterPrint(printStream);
        }
    }

    private boolean isImage(String fileName) {
        fileName = fileName.toLowerCase();
        return fileName.endsWith(".png") || fileName.endsWith(".bmp")
                || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg");
    }

}

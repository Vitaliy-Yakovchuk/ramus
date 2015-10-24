package com.ramussoft.pb.print.web;

import java.io.IOException;
import java.util.Hashtable;

import com.ramussoft.common.Attribute;
import com.ramussoft.common.AttributeType;
import com.ramussoft.common.Element;
import com.ramussoft.pb.DataPlugin;
import com.ramussoft.pb.print.AttributeViewer;
import com.ramussoft.web.HTTPParser;
import com.ramussoft.web.HTTPParser.PrintStream;

public class AttributeViewerFactory {

    private static AttributeViewerFactory factory;

    public static AttributeViewerFactory getAttributeViewverFactory() {
        if (factory == null) {
            factory = new AttributeViewerFactory();
        }
        return factory;
    }

    private Hashtable<AttributeType, AttributeViewer> hash = new Hashtable<AttributeType, AttributeViewer>();

    public AttributeViewerFactory() {
        addAttributeViewer(new AttributeType("Core", "Text", true),
                new SimpleAttributeViewer());
        addAttributeViewer(new AttributeType("Core", "Variant", true),
                new VariantAttributeViewer());
        addAttributeViewer(new AttributeType("Core", "Long", true),
                new NumberAttributeViewer());
        addAttributeViewer(new AttributeType("Core", "Double", true),
                new NumberAttributeViewer());
        addAttributeViewer(new AttributeType("Core", "Date", true),
                new SimpleAttributeViewer());
        addAttributeViewer(new AttributeType("Core", "OtherElement", true),
                new OtherElementAttributeViewer());

        addAttributeViewer(new AttributeType("Core", "ElementList", true),
                new ElementListAttributeViewer());
        addAttributeViewer(new AttributeType("Core", "Table", true),
                new TableAttributeViewer());
        addAttributeViewer(new AttributeType("Core", "Date", true),
                new DateAttributeViewer());
        addAttributeViewer(new AttributeType("Core", "HTMLText", true),
                new HTMLTextAttributeViewer());
        addAttributeViewer(new AttributeType("Core", "File", true),
                new FileAttributeViewer());
    }

    public void addAttributeViewer(AttributeType attributeType,
                                   AttributeViewer viewer) {
        hash.put(attributeType, viewer);
    }

    public AttributeViewer getAttributeViewer(Attribute attribute) {
        return hash.get(attribute.getAttributeType());
    }

    public void printAttribute(HTTPParser.PrintStream printStream,
                               DataPlugin dataPlugin, Element element, HTTPParser parser,
                               final Attribute attribute) throws IOException {
        AttributeViewer viewer = getAttributeViewer(attribute);
        if (viewer != null) {
            viewer.printAttribute(printStream, dataPlugin, element, attribute,
                    parser, new AttributeViewerCallback() {
                        @Override
                        public void beforePrint(PrintStream printStream)
                                throws IOException {
                            printStream.println("<hr>");
                            printStream.println("<b>" + attribute.getName()
                                    + "</b>");
                        }

                        @Override
                        public void afterPrint(PrintStream printStream)
                                throws IOException {
                            printStream.println("<br>");
                        }
                    });
        }
    }
}
